package net.mcft.copy.backpacks.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.SettingInteger;

@SideOnly(Side.CLIENT)
public class EntrySlider extends EntryButton<Integer> {
	
	public final Slider slider;
	
	public EntrySlider(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<Integer> setting) {
		super(owningScreen, owningEntryList, setting, new Slider(
			((SettingInteger)setting).getMinValue(),
			((SettingInteger)setting).getMaxValue()));
		slider = (Slider)button;
	}
	
	@Override
	public void onValueChanged() { slider.setValue(value); }
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight,
	                      int mouseX, int mouseY, boolean isSelected) {
		super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);
		value = slider.getValueInt();
	}
	
	/** Custom GuiSlider class which snaps to integer values. */
	public static class Slider extends GuiSlider {
		
		public Slider(int min, int max) { super(0, 0, 0, 300, 18, "", "", min, max, min, false, false); }
		
		@Override public void updateSlider() { super.updateSlider(); setValue(getValueInt()); }
		
		@Override
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			if (!visible) return;
			if (dragging) {
				sliderValue = (mouseX - (xPosition + 4)) / (float)(width - 8);
				updateSlider();
			}
			
			int x = xPosition + (int)(sliderValue * (width - 8));
			float v = (enabled ? 1.0F : 0.5F);
			GlStateManager.color(v, v, v);
			drawTexturedModalRect(x, yPosition, 0, 66, 4, 20);
			drawTexturedModalRect(x + 4, yPosition, 196, 66, 4, 20);
		}
		
	}
	
}
