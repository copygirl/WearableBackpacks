package net.mcft.copy.backpacks.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.SettingDouble;

@SideOnly(Side.CLIENT)
public class EntryEffectOpacity extends EntrySliderPercent {
	
	public EntryEffectOpacity(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<Double> setting) {
		super(owningScreen, owningEntryList, setting, new Slider(
			((SettingDouble)setting).getMinValue(),
			((SettingDouble)setting).getMaxValue()));
	}
	
	public static class Slider extends EntrySliderPercent.Slider {
		
		public Slider(double min, double max) { super(min, max); }
		
		@Override
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			renderEffect(xPosition + 1, yPosition + 1, width - 2, height - 2, (float)getValue());
			mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
			super.mouseDragged(mc, mouseX, mouseY);
		}
		
	}
	
	private static final ResourceLocation ENCHANTED_ITEM_GLINT =
		new ResourceLocation("textures/misc/enchanted_item_glint.png");
	private static void renderEffect(int x, int y, int width, int height, float opacity) {
		if (opacity <= 0) return;
		Minecraft mc = Minecraft.getMinecraft();
		float animProgress = Minecraft.getSystemTime() / 400.0F;
		
		int color = 0x8040FF;
		float r = (color >> 16 & 0xFF) / 255.0F;
		float g = (color >> 8 & 0xFF) / 255.0F;
		float b = (color & 0xFF) / 255.0F;
		GlStateManager.color(r, g, b, opacity * 0.6F);
		
		mc.getTextureManager().bindTexture(ENCHANTED_ITEM_GLINT);
		
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0.0F);
		
		for (int i = 0; i < 2; ++i) {
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.loadIdentity();
			GlStateManager.rotate(30.0F - i * 60.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.translate(0.0F, animProgress * (0.001F + i * 0.003F) * 20.0F, 0.0F);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);
			GuiUtils.drawTexturedModalRect(x, y, 0, 0, width, height, 0);
		}
		
		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableAlpha();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableBlend();
	}
	
}
