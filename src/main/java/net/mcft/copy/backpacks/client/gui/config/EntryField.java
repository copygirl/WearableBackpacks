package net.mcft.copy.backpacks.client.gui.config;

import java.util.Optional;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.control.GuiField;
import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.SettingSingleValue;

@SideOnly(Side.CLIENT)
public abstract class EntryField<T> extends BaseEntrySetting<T> {
	
	public EntryField(Setting<T> setting)
		{ this(setting, new GuiField()); }
	public EntryField(Setting<T> setting, GuiField field) {
		super(setting, field);
		field.setChangedAction(this::onFieldChanged);
		field.setCharValidator(chr -> isCharValid(field.getText(), field.getCursorPosition(), chr));
	}
	
	public GuiField getField() { return (GuiField)control; }
	public String getFieldText() { return getField().getText().trim(); }
	public Optional<T> getFieldValue() {
		try { return Optional.of(((SettingSingleValue<T>)setting).parse(getFieldText())); }
		catch (Throwable ex) { return Optional.empty(); }
	}
	
	@Override
	protected void onChanged() {
		if (!getValue().equals(getFieldValue()))
			getField().setText(getValue().map(Object::toString).orElse(""));
	}
	
	/** Called when the field's value changes from player input. */
	protected void onFieldChanged() { setValue(getFieldValue()); }
	
	protected boolean isCharValid(String text, int cursorPosition, char chr)
		{ return true; }
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		getField().setTextAndBorderColor(COLOR_TEXT_INVALID, !isValid());
		super.draw(mouseX, mouseY, partialTicks);
	}
	
	
	public static class Number extends EntryField<Integer> {
		public Number(Setting<Integer> setting)
			{ super(setting); }
		@Override
		protected boolean isCharValid(String text, int cursorPosition, char chr) {
			String validChars = "0123456789";
			return (validChars.contains(String.valueOf(chr)) ||
			        ((chr == '-') && (cursorPosition == 0) && !text.startsWith("-")));
		}
	}
	
	public static class Decimal extends EntryField<Double> {
		public Decimal(Setting<Double> setting)
			{ super(setting); }
		@Override
		protected boolean isCharValid(String text, int cursorPosition, char chr) {
			String validChars = "0123456789";
			return (validChars.contains(String.valueOf(chr)) ||
			        ((chr == '-') && (cursorPosition == 0) && !text.startsWith("-")) ||
			        ((chr == '.') && !text.contains(".")));
		}
	}
	
}
