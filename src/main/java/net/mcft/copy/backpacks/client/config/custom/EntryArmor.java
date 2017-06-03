package net.mcft.copy.backpacks.client.config.custom;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.config.EntrySlider;
import net.mcft.copy.backpacks.config.Setting;

@SideOnly(Side.CLIENT)
public class EntryArmor extends EntrySlider {
	
	public EntryArmor(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<Integer> setting) {
		super(owningScreen, owningEntryList, setting);
		slider.drawString = false;
	}
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight,
	                      int mouseX, int mouseY, boolean isSelected) {
		super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);
		// Draw visual armor bar.
		float v = (enabled() ? 1.0F : 0.5F);
		GlStateManager.color(v, v, v);
		mc.getTextureManager().bindTexture(Gui.ICONS);
		int xx = slider.xPosition + slider.width / 2 - 5 * 8;
		int yy = slider.yPosition + slider.height / 2 - 8 / 2;
		for (int i = 0; i < 10; i++, xx += 8) {
			if (i * 2 + 1 < value) slider.drawTexturedModalRect(xx, yy, 34, 9, 9, 9);
			else if (i * 2 + 1 == value) slider.drawTexturedModalRect(xx, yy, 25, 9, 9, 9);
			else slider.drawTexturedModalRect(xx, yy, 16, 9, 9, 9);
		}
	}
	
}
