package net.mcft.copy.backpacks;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.BackpacksContent;
import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.BackpackRegistry;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.ProxyCommon;
import net.mcft.copy.backpacks.block.entity.TileEntityBackpack;
import net.mcft.copy.backpacks.client.BakedModelDefaultTexture;
import net.mcft.copy.backpacks.client.KeyBindingHandler;
import net.mcft.copy.backpacks.client.RendererBackpack;
import net.mcft.copy.backpacks.item.ItemBackpack;
import net.mcft.copy.backpacks.misc.util.MiscUtils;
import net.mcft.copy.backpacks.misc.util.NbtUtils;
import net.mcft.copy.backpacks.misc.util.ReflectUtils;

@SideOnly(Side.CLIENT)
public class ProxyClient extends ProxyCommon {
	
	public static IBakedModel MODEL_BACKPACK;
	public static IBakedModel MODEL_BACKPACK_TOP;
	public static IBakedModel MODEL_BACKPACK_STRAPS;
	
	public static IBakedModel MODEL_BACKPACK_ENCH;
	public static IBakedModel MODEL_BACKPACK_ENCH_TOP;
	
	@Override
	public void preInit() {
		super.preInit();
		MinecraftForge.EVENT_BUS.register(new KeyBindingHandler());
	}
	
	@Override
	public void init() {
		super.init();
		Minecraft mc = Minecraft.getMinecraft();
		
		if (BackpacksContent.BACKPACK != null) {
			mc.getBlockColors().registerBlockColorHandler(BLOCK_COLOR, MiscUtils.getBlockFromItem(BackpacksContent.BACKPACK));
			mc.getItemColors().registerItemColorHandler(ITEM_COLOR, BackpacksContent.BACKPACK);
			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBackpack.class, new RendererBackpack.TileEntity());
		}
		
		RenderManager manager = mc.getRenderManager();
		Map<String, RenderPlayer> skinMap = manager.getSkinMap();
		skinMap.get("default").addLayer(new RendererBackpack.Layer());
		skinMap.get("slim").addLayer(new RendererBackpack.Layer());
		
		for (Class<? extends EntityLivingBase> entityClass : BackpackRegistry.entities.keySet()) {
			Render<?> render = manager.getEntityClassRenderObject(entityClass);
			if (!(render instanceof RenderBiped)) continue;
			((RenderBiped<?>)render).addLayer(new RendererBackpack.Layer());
		}
	}
	
	
	// Model related
	
	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		MODEL_BACKPACK = bakeBlockModel("wearablebackpacks:block/backpack");
		MODEL_BACKPACK_TOP = bakeBlockModel("wearablebackpacks:block/backpack_top");
		MODEL_BACKPACK_STRAPS = bakeBlockModel("wearablebackpacks:block/backpack_straps");
		
		MODEL_BACKPACK_ENCH = new BakedModelDefaultTexture(MODEL_BACKPACK);
		MODEL_BACKPACK_ENCH_TOP = new BakedModelDefaultTexture(MODEL_BACKPACK_TOP);
	}
	private static IBakedModel bakeBlockModel(String location) {
		IModel model = getModel(new ResourceLocation(location));
		return model.bake(model.getDefaultState(), DefaultVertexFormats.BLOCK, loc ->
			Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(loc.toString()));
	}
	private static IModel getModel(ResourceLocation location) {
		try {
			IModel model = ModelLoaderRegistry.getModel(location);
			if (model == null)
				WearableBackpacks.LOG.error("Model " + location + " is missing! THIS WILL CAUSE A CRASH!");
			return model;
		} catch (Exception e) { e.printStackTrace(); return null; }
	}
	
	@SubscribeEvent
	public void onRegisterModels(ModelRegistryEvent event) {
		if (BackpacksContent.BACKPACK != null) {
			ModelLoader.setCustomModelResourceLocation(BackpacksContent.BACKPACK, 0,
				new ModelResourceLocation("wearablebackpacks:backpack", "inventory"));
		}
	}
	
	
	// IItemColor and IBlockColor implementations
	
	// TODO: Make this work for different default colors / non-dyeable backpacks.
	public static final IItemColor ITEM_COLOR = (stack, tintIndex) ->
		NbtUtils.get(stack, ItemBackpack.DEFAULT_COLOR, "display", "color");
	
	public static final IBlockColor BLOCK_COLOR = (state, world, pos, tintIndex) -> {
		ItemStack stack = null;
		if ((world != null) && (pos != null)) {
			IBackpack backpack = BackpackHelper.getBackpack(world.getTileEntity(pos));
			if (backpack != null) stack = backpack.getStack();
		}
		return ITEM_COLOR.getColorFromItemstack(stack, tintIndex);
	};
	
	
	// Lots of code just to disable capes when backpacks are equipped!
	
	private static DataParameter<Byte> PLAYER_MODEL_FLAG =
		ReflectUtils.get(EntityPlayer.class, "PLAYER_MODEL_FLAG", "field_184827_bp");
	
	/** Sets whether the player is currently wearing the specified model part.
	 *  This is the opposite of the {@link EntityPlayer#isWearing} method. */
	private static void setWearing(EntityPlayer player, EnumPlayerModelParts part, boolean value) {
		byte current = player.getDataManager().get(PLAYER_MODEL_FLAG).byteValue();
		if (value) current |= part.getPartMask();
		else current &= ~part.getPartMask();
		player.getDataManager().set(PLAYER_MODEL_FLAG, current);
	}
	
	private boolean _disabledCape = false;
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
		EntityPlayer player = event.getEntityPlayer();
		if ((BackpackHelper.getBackpack(player) == null) ||
		    !player.isWearing(EnumPlayerModelParts.CAPE)) return;
		// Disable player rendering for players with equipped backpacks
		// by temporarily setting whether they are wearing it to false.
		setWearing(player, EnumPlayerModelParts.CAPE, false);
		_disabledCape = true;
	}
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
		if (!_disabledCape) return;
		// Reenable cape rendering if it was disabled in the
		EntityPlayer player = event.getEntityPlayer();
		setWearing(player, EnumPlayerModelParts.CAPE, true);
		_disabledCape = false;
	}
	
}
