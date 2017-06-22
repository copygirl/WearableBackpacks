package net.mcft.copy.backpacks.client.config;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.SettingDouble;

@SideOnly(Side.CLIENT)
public class EntrySliderPercent extends EntryButton<Double> {
	
	private static final DecimalFormat _df = new DecimalFormat("0.##");
	
	public final Slider slider;
	
	public EntrySliderPercent(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<Double> setting) {
		this(owningScreen, owningEntryList, setting, new Slider(
			((SettingDouble)setting).getMinValue(),
			((SettingDouble)setting).getMaxValue()));
	}
	public EntrySliderPercent(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<Double> setting, GuiSlider slider) {
		super(owningScreen, owningEntryList, setting, slider);
		this.slider = (Slider)slider;
	}
	
	@Override
	public void onValueChanged() { slider.setValue(value); }
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight,
	                      int mouseX, int mouseY, boolean isSelected, float partialTicks) {
		slider.displayString = _df.format(getValue() * 100) + "%";
		super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
		value = slider.getValue();
	}
	
	// TODO: Don't repeat yourself. Somehow merge with EntrySlider?
	public static class Slider extends GuiSlider {
		
		public Slider(double min, double max) { super(0, 0, 0, 300, 18, "", "", min, max, min, false, false); }
		
		@Override
		public void updateSlider() {
			super.updateSlider();
			setValue(Math.round(getValue() * 100) / 100.0F);
		}
		
		@Override
		protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
			if (dragging) {
				sliderValue = (mouseX - (x + 4)) / (float)(width - 8);
				updateSlider();
			}
			
			int xx = x + (int)(sliderValue * (width - 8));
			float v = (enabled ? 1.0F : 0.5F);
			GlStateManager.color(v, v, v);
			drawTexturedModalRect(xx, y, 0, 66, 4, 20);
			drawTexturedModalRect(xx + 4, y, 196, 66, 4, 20);
		}
		
	}
	
}
