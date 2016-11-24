package net.mcft.copy.backpacks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import net.mcft.copy.backpacks.ProxyClient;
import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackType;
import net.mcft.copy.backpacks.block.entity.TileEntityBackpack;

public class RendererBackpack {
	
	private RendererBackpack() {  }
	
	private static void render(IBackpack backpack, float partialTicks, boolean renderStraps) {
		
		BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		
		ItemStack stack = backpack.getStack();
		int color = ProxyClient.ITEM_COLOR.getColorFromItemstack(stack, 0);
		float r = (color >> 16 & 0xFF) / 255.0F;
		float g = (color >> 8 & 0xFF) / 255.0F;
		float b = (color & 0xFF) / 255.0F;
		
		renderer.renderModelBrightnessColor(ProxyClient.MODEL_BACKPACK, 1.0f, r, g, b);
		if (renderStraps)
			renderer.renderModelBrightnessColor(ProxyClient.MODEL_BACKPACK_STRAPS, 1.0f, r, g, b);
		
		float lidAngle = 0.0F;
		IBackpackType type = backpack.getType();
		if (type != null) {
			int lidTicks = backpack.getLidTicks();
			int prevLidTicks = backpack.getLidTicks();
			lidAngle = type.getLidAngle(lidTicks, prevLidTicks, partialTicks);
		}
		
		float lidYOffset = 9 / 16.0F;
		float lidZOffset = 5 / 16.0F;
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, lidYOffset, lidZOffset);
		GlStateManager.rotate(lidAngle, -1.0F, 0.0F, 0.0F);
		GlStateManager.translate(0.0F, -lidYOffset, -lidZOffset);
		renderer.renderModelBrightnessColor(ProxyClient.MODEL_BACKPACK_TOP, 1.0f, r, g, b);
		GlStateManager.popMatrix();
		
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
			render(backpack, partialTicks, true);
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
			if (entity.isSneaking()) {
				// FIXME: Can be sneaking while flying with the elytra, then backpack is misaligned..?
				GlStateManager.translate(0.0F, 0.2F, 0.0F);
				GlStateManager.rotate(90.0F / (float)Math.PI, 1.0F, 0.0F, 0.0F);
			}
			GlStateManager.scale(0.8F, 0.8F, 0.8F);
			GlStateManager.translate(0.5F, 0.5F, 0.5F);
			GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.translate(0, -HEIGHT_OFFSET, -DEPTH_OFFSET);
			render(backpack, partialTicks, false);
			GlStateManager.popMatrix();
		}
		
	}
	
}
