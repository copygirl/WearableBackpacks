package net.mcft.copy.backpacks.client.gui.config;

import java.util.Objects;
import java.util.Optional;

import net.mcft.copy.backpacks.client.gui.control.GuiField;
import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.SettingSingleValue;

public abstract class EntryField<T> extends BaseEntrySetting<T> {
	
	public EntryField(BackpacksConfigScreen owningScreen, Setting<T> setting)
		{ this(owningScreen, setting, new GuiField()); }
	public EntryField(BackpacksConfigScreen owningScreen, Setting<T> setting, GuiField field) {
		super(owningScreen, setting, field);
		field.setChangedAction(this::onFieldChanged);
		field.setCharValidator(chr -> isCharValid(field.getText(), field.getCursorPosition(), chr));
	}
	
	public GuiField getField() { return (GuiField)control; }
	public String getFieldText() { return getField().getText().trim(); }
	public Optional<T> getFieldValue() {
		try { return Optional.ofNullable(((SettingSingleValue<T>)setting).parse(getFieldText())); }
		catch (Throwable ex) { return Optional.empty(); }
	}
	
	@Override
	public boolean isValid() { return getFieldValue().isPresent(); }
	
	@Override
	protected void onChanged() {
		if (!Optional.ofNullable(getValue()).equals(getFieldValue()))
			getField().setText(Objects.toString(getValue()));
		super.onChanged();
	}
	
	/** Called when the field's value changes from player input. */
	protected void onFieldChanged()
		{ getFieldValue().ifPresent(this::setValue); }
	
	protected boolean isCharValid(String text, int cursorPosition, char chr)
		{ return true; }
	
	
	public static class Number extends EntryField<Integer> {
		public Number(BackpacksConfigScreen owningScreen, Setting<Integer> setting)
			{ super(owningScreen, setting); }
		@Override
		protected boolean isCharValid(String text, int cursorPosition, char chr) {
			String validChars = "0123456789";
			return (validChars.contains(String.valueOf(chr)) ||
			        ((chr == '-') && (cursorPosition == 0) && !text.startsWith("-")));
		}
	}
	
	public static class Decimal extends EntryField<Double> {
		public Decimal(BackpacksConfigScreen owningScreen, Setting<Double> setting)
			{ super(owningScreen, setting); }
		@Override
		protected boolean isCharValid(String text, int cursorPosition, char chr) {
			String validChars = "0123456789";
			return (validChars.contains(String.valueOf(chr)) ||
			        ((chr == '-') && (cursorPosition == 0) && !text.startsWith("-")) ||
			        ((chr == '.') && !text.contains(".")));
		}
	}
	
}
