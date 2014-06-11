package net.mcft.copy.backpacks.proxy;

import java.util.ArrayList;
import java.util.List;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.block.tileentity.TileEntityBackpack;
import net.mcft.copy.backpacks.client.BackpackResources;
import net.mcft.copy.backpacks.client.model.ModelBackpack;
import net.mcft.copy.backpacks.client.model.ModelRendererBackpack;
import net.mcft.copy.backpacks.content.BackpackBlocks;
import net.mcft.copy.core.client.renderer.ItemRendererTileEntity;
import net.mcft.copy.core.client.renderer.TileEntityRenderer;
import net.mcft.copy.core.util.RegistryUtils;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	
	@Override
	public void init() {
		
		super.init();
		
		registerTileEntityRenderer(
				BackpackBlocks.backpack, TileEntityBackpack.class,
				new TileEntityRenderer(BackpackResources.textureBackpack,
				                       ModelBackpack.getModel(BackpackResources.modelBackpack)))
				.setScale(1.5F).setOffset(-0.08F).setInventoryRotation(70.0F)
				                                 .setThirdPersonRotation(-45.0F);
		
	}
	
	/** Register tile entity and item renderers for this block / tile entity.
	 *  Returns the item renderer for setting attitional options. */
	private ItemRendererTileEntity registerTileEntityRenderer(
			Block block, Class<? extends TileEntity> tileEntityClass,
			TileEntitySpecialRenderer tileEntityRenderer) {
		ItemRendererTileEntity itemRenderer =
				new ItemRendererTileEntity(tileEntityClass, tileEntityRenderer);
		// Only register renderers if block is enabled.
		if (RegistryUtils.isEnabled(block)) {
			ClientRegistry.bindTileEntitySpecialRenderer(tileEntityClass, tileEntityRenderer);
			MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(block), itemRenderer);
		}
		return itemRenderer;
	}
	
	@SubscribeEvent
	public void onRenderLivingSpecialsPre(RenderLivingEvent.Pre event) {
		
		// Render backpacks on entities
		
		EntityLivingBase entity = event.entity;
		ItemStack backpack = BackpackHelper.getEquippedBackpack(entity);
		if (backpack == null) return;
		
		ModelBiped model = getModelBipedFromRenderer(event.renderer);
		if ((model == null) || (model.bipedBody == null)) return;
		
		ModelRendererBackpack backpackRenderer = null;
		// Check if the model already contains a backpack renderer.
		if (model.bipedBody.childModels == null)
			model.bipedBody.childModels = new ArrayList<ModelRenderer>();
		else for (ModelRenderer renderer : (List<ModelRenderer>)model.bipedBody.childModels)
			if (renderer instanceof ModelRendererBackpack) {
				backpackRenderer = (ModelRendererBackpack)renderer;
				break;
			}
		// If it doesn't have one, add it.
		if (backpackRenderer == null) {
			backpackRenderer = new ModelRendererBackpack(model);
			model.bipedBody.childModels.add(backpackRenderer);
		}
		backpackRenderer.setEntityAndBackpack(entity, backpack);
	}
	
	private static ModelBiped getModelBipedFromRenderer(RendererLivingEntity renderer) {
		ModelBase model = ReflectionHelper.getPrivateValue(
				RendererLivingEntity.class, renderer,
				"field_77045_g", "mainModel");
		return ((model instanceof ModelBiped) ? (ModelBiped)model : null);
	}
	
	@SubscribeEvent
	public void onRenderPlayerSpecialsPre(RenderPlayerEvent.Specials.Pre event) {
		// If the player has a backpack equipped, don't render the cape.
		if (BackpackHelper.getEquippedBackpack(event.entityPlayer) != null)
			event.renderCape = false;
	}
	
}
