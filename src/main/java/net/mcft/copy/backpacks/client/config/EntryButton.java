package net.mcft.copy.backpacks.client.config;

import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.config.Setting;

@SideOnly(Side.CLIENT)
public abstract class EntryButton<T> extends EntrySetting<T> {
	
	public final GuiButtonExt button;
	
	public EntryButton(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<?> setting)
		{ this(owningScreen, owningEntryList, setting, new GuiButtonExt(0, 0, 0, 300, 18, "")); }
	public EntryButton(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<?> setting, GuiButtonExt button) {
		super(owningScreen, owningEntryList, setting);
		this.button = button;
	}
	
	/** Called when the button was pressed. */
	public void buttonPressed() {  }
	
	@Override
	public void onValueChanged() { button.displayString = value.toString(); }
	
	@Override
	public void keyTyped(char eventChar, int eventKey) {  }
	@Override
	public void updateCursorCounter() {  }
	@Override
	public void mouseClicked(int x, int y, int mouseEvent) {  }
	
	@Override
	public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		if (!button.mousePressed(mc, x, y))
			return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
		button.playPressSound(mc.getSoundHandler());
		buttonPressed();
		return true;
	}
	
	@Override
	public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		super.mouseReleased(index, x, y, mouseEvent, relativeX, relativeY);
		button.mouseReleased(x, y);
	}
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight,
	                      int mouseX, int mouseY, boolean isSelected, float partialTicks) {
		super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
		button.width = owningEntryList.controlWidth;
		button.x = owningScreen.entryList.controlX;
		button.y = y;
		button.enabled = enabled();
		button.drawButton(mc, mouseX, mouseY, partialTicks);
	}
	
	public static class Switch extends EntryButton<Boolean> {
		
		public Switch(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<?> setting)
			{ super(owningScreen, owningEntryList, setting); }
		
		@Override
		public void onValueChanged() {
			super.onValueChanged();
			button.packedFGColour = GuiUtils.getColorCode(value ? '2' : '4', true);
		}
		
		@Override
		public void buttonPressed() { setValue(!value); }
		
	}
	
}
