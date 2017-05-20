package net.mcft.copy.backpacks.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import net.mcft.copy.backpacks.ProxyClient;
import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackType;
import net.mcft.copy.backpacks.block.entity.TileEntityBackpack;

/** Contains methods and nested classes which handle rendering
    of backpacks as tile entity and layer on regular entities. */
public final class RendererBackpack {
	
	private RendererBackpack() {  }
	
	private static void render(IBackpack backpack, float ticks, boolean renderStraps) {
		ItemStack stack = backpack.getStack();
		int color = ProxyClient.ITEM_COLOR.getColorFromItemstack(stack, 0);
		
		BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		
		float r = (color >> 16 & 0xFF) / 255.0F;
		float g = (color >> 8 & 0xFF) / 255.0F;
		float b = (color & 0xFF) / 255.0F;
		renderModel(backpack, renderer, ticks, renderStraps, r, g, b, false);
		
		if (stack.isItemEnchanted())
			renderEnchanted(backpack, renderer, ticks, renderStraps);
	}
	
	// TODO: See if this can be changed back to FastTESR?
	//       Forge apparently has an animation API? asie says to ask fry.
	public static class TileEntity extends TileEntitySpecialRenderer<TileEntityBackpack> {
		
		@Override
		public void renderTileEntityAt(TileEntityBackpack entity, double x, double y, double z,
		                               float partialTicks, int breakStage) {
			IBackpack backpack = BackpackHelper.getBackpack(entity);
			if (backpack == null) return;
			float angle = entity.facing.getHorizontalAngle();
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
			GlStateManager.rotate(angle, 0.0F, -1.0F, 0.0F);
			GlStateManager.translate(-0.5, -0.5, -0.5);
			render(backpack, entity.getAge() + partialTicks, true);
			GlStateManager.popMatrix();
		}
		
	}
	
	public static class Layer implements LayerRenderer<EntityLivingBase> {
		
		// TODO: Allow this to be changed for backpack models that are visually bigger.
		private static final float HEIGHT_OFFSET = 4.0F / 16.0F;
		private static final float DEPTH_OFFSET = 10.5F / 16.0F;
		
		public boolean shouldCombineTextures() { return false; }
		
		public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount,
		                          float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			
			IBackpack backpack = BackpackHelper.getBackpack(entity);
			if (backpack == null) return;
			
			GlStateManager.pushMatrix();
			
			// Make backpack swing with body as players swing their arms.
			float swingProgress = entity.getSwingProgress(partialTicks);
			float swingAngle = MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2.0F)) * 0.2F;
			if ((entity.swingingHand == EnumHand.OFF_HAND) ^
			    (entity.getPrimaryHand() == EnumHandSide.LEFT)) swingAngle *= -1;
			if (swingAngle != 0) GlStateManager.rotate((float)Math.toDegrees(swingAngle), 0.0F, 1.0F, 0.0F);
			
			// Rotate backpack if entity is sneaking.
			if (entity.isSneaking()) {
				// FIXME: Can be sneaking while flying with the elytra, then backpack is misaligned..?
				GlStateManager.translate(0.0F, 0.2F, 0.0F);
				GlStateManager.rotate(90.0F / (float)Math.PI, 1.0F, 0.0F, 0.0F);
			}
			
			GlStateManager.scale(0.8F, 0.8F, 0.8F);
			GlStateManager.translate(0.5F, 0.5F, 0.5F);
			GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.translate(0, -HEIGHT_OFFSET, -DEPTH_OFFSET);
			
			render(backpack, entity.ticksExisted + partialTicks, false);
			
			GlStateManager.popMatrix();
			
		}
		
	}
	
	private static void renderModel(IBackpack backpack, BlockModelRenderer renderer,
	                                float ticks, boolean renderStraps,
									float r, float g, float b, boolean useEnch) {
		
		IBakedModel baseModel = (useEnch ? ProxyClient.MODEL_BACKPACK_ENCH : ProxyClient.MODEL_BACKPACK);
		renderer.renderModelBrightnessColor(baseModel, 1.0F, r, g, b);
		if (renderStraps)
			renderer.renderModelBrightnessColor(ProxyClient.MODEL_BACKPACK_STRAPS, 1.0F, r, g, b);
		
		float lidAngle = 0.0F;
		IBackpackType type = backpack.getType();
		if (type != null) {
			int lidTicks = backpack.getLidTicks();
			int prevLidTicks = backpack.getPrevLidTicks();
			lidAngle = type.getLidAngle(lidTicks, prevLidTicks, ticks % 1);
		}
		
		// FIXME: Allow for custom, adjustable lid position.
		float lidYOffset = 9 / 16.0F;
		float lidZOffset = 5 / 16.0F;
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, lidYOffset, lidZOffset);
		GlStateManager.rotate(lidAngle, -1.0F, 0.0F, 0.0F);
		GlStateManager.translate(0.0F, -lidYOffset, -lidZOffset);
		IBakedModel topModel = (useEnch ? ProxyClient.MODEL_BACKPACK_ENCH_TOP : ProxyClient.MODEL_BACKPACK_TOP);
		renderer.renderModelBrightnessColor(topModel, 1.0F, r, g, b);
		GlStateManager.popMatrix();
		
	}
	
	private static final ResourceLocation ENCHANTED_ITEM_GLINT =
		new ResourceLocation("textures/misc/enchanted_item_glint.png");
	// Based on LayerArmorBase.renderEnchantedGlint.
	private static void renderEnchanted(IBackpack backpack, BlockModelRenderer renderer,
	                                    float ticks, boolean renderStraps) {
		
		float glintStrength = 0.8F;
		if (glintStrength <= 0) return;
		float glintScale = 0.5F;
		float animProgress = ticks / 10;
		
		int color = 0x8040FF; // TODO: Support Quark's colored runes.
		float r = (color >> 16 & 0xFF) / 255.0F * glintStrength;
		float g = (color >> 8 & 0xFF) / 255.0F * glintStrength;
		float b = (color & 0xFF) / 255.0F * glintStrength;
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(ENCHANTED_ITEM_GLINT);
		Minecraft.getMinecraft().entityRenderer.func_191514_d(true); // Disables fog?
		
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);
		GlStateManager.depthFunc(GL11.GL_EQUAL);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
		
		for (int i = 0; i < 2; ++i) {
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.loadIdentity();
			GlStateManager.scale(glintScale, glintScale, glintScale);
			GlStateManager.rotate(30.0F - i * 60.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.translate(0.0F, animProgress * (0.001F + i * 0.003F) * 20.0F, 0.0F);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			renderModel(backpack, renderer, ticks, renderStraps, r, g, b, true);
		}
		
		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		
		GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableBlend();
		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.depthMask(true);
		GlStateManager.enableLighting();
		
		Minecraft.getMinecraft().entityRenderer.func_191514_d(false); // Enables fog?
		
	}
	
}
