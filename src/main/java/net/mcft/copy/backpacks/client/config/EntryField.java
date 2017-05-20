package net.mcft.copy.backpacks.client.config;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiTextField;

import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.config.Setting;

@SideOnly(Side.CLIENT)
public abstract class EntryField<T> extends EntrySetting<T> {
	
	public final GuiTextField field;
	
	public EntryField(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<?> setting)
		{ this(owningScreen, owningEntryList, setting, new GuiTextField(0, owningScreen.mc.fontRendererObj, 0, 0, 300, 16)); }
	public EntryField(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<?> setting, GuiTextField field) {
		super(owningScreen, owningEntryList, setting);
		this.field = field;
		field.setMaxStringLength(10000);
	}
	
	@Override
	public void onValueChanged() { field.setText(value.toString()); }
	
	@Override
	public void keyTyped(char eventChar, int eventKey) {
		if (!enabled() && (eventKey != Keyboard.KEY_LEFT) && (eventKey != Keyboard.KEY_RIGHT) &&
		                  (eventKey != Keyboard.KEY_HOME) && (eventKey != Keyboard.KEY_END)) return;
		
		field.textboxKeyTyped((enabled() ? eventChar : Keyboard.CHAR_NONE), eventKey);
		try { value = setting.parse(field.getText().trim()); isValidValue = true; }
		catch (Throwable ex) { isValidValue = false; }
	}
	
	@Override
	public void updateCursorCounter() { field.updateCursorCounter(); }
	
	@Override
	public void mouseClicked(int x, int y, int mouseEvent) { field.mouseClicked(x, y, mouseEvent); }
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
		super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);
		field.width = owningEntryList.controlWidth - 4;
		field.xPosition = owningScreen.entryList.controlX + 2;
		field.yPosition = y + 1;
		field.setEnabled(enabled());
		field.drawTextBox();
	}
	
	public static class Number extends EntryField<Integer> {
		
		public Number(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<?> setting)
			{ super(owningScreen, owningEntryList, setting); }
		
		@Override
		public void keyTyped(char eventChar, int eventKey) {
			String validChars = "0123456789";
			String before = field.getText();
			if (!validChars.contains(String.valueOf(eventChar)) &&
			    (before.startsWith("-") || (field.getCursorPosition() > 0) || (eventChar != '-')) &&
			    (eventKey != Keyboard.KEY_BACK) && (eventKey != Keyboard.KEY_DELETE) &&
			    (eventKey != Keyboard.KEY_LEFT) && (eventKey != Keyboard.KEY_RIGHT) &&
			    (eventKey != Keyboard.KEY_HOME) && (eventKey != Keyboard.KEY_END)) return;
			super.keyTyped(eventChar, eventKey);
		}
		
	}
	
	public static class Decimal extends EntryField<Double> {
		
		public Decimal(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<?> setting)
			{ super(owningScreen, owningEntryList, setting); }
		
		@Override
		public void keyTyped(char eventChar, int eventKey) {
			String validChars = "0123456789";
			String before = field.getText();
			if (!validChars.contains(String.valueOf(eventChar)) &&
			    (before.startsWith("-") || (field.getCursorPosition() > 0) || (eventChar != '-')) &&
			    (before.contains(".") || (eventChar != '.')) &&
			    (eventKey != Keyboard.KEY_BACK) && (eventKey != Keyboard.KEY_DELETE) &&
			    (eventKey != Keyboard.KEY_LEFT) && (eventKey != Keyboard.KEY_RIGHT) &&
			    (eventKey != Keyboard.KEY_HOME) && (eventKey != Keyboard.KEY_END)) return;
			super.keyTyped(eventChar, eventKey);
		}
		
	}
	
}
