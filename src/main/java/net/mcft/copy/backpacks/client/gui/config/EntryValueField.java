package net.mcft.copy.backpacks.client.gui.config;

import java.util.List;
import java.util.Optional;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.control.GuiField;
import net.mcft.copy.backpacks.config.Status;
import net.mcft.copy.backpacks.config.Status.Severity;

@SideOnly(Side.CLIENT)
public abstract class EntryValueField<T> extends GuiField
	implements IConfigValue<T>, IConfigValue.ShowsStatus {
	
	public EntryValueField() { setFillVertical(); }
	
	@Override
	public Optional<T> getValue() {
		try { return Optional.of(parseValue(getText())); }
		catch (Exception ex) { return Optional.empty(); }
	}
	@Override
	public void setValue(T value) {
		// Only change text if value differs from what the field currently represents.
		if (!getValue().equals(Optional.of(value)))
			setText(stringifyValue(value));
	}
	
	@Override
	public void setStatus(List<Status> value) {
		Severity severity = Status.getSeverity(value);
		boolean isFine    = (Severity.HINT.compareTo(severity) >= 0);
		setTextColor(  !isFine ? severity.foregroundColor : COLOR_TEXT_DEFAULT);
		setBorderColor(!isFine ? severity.foregroundColor : COLOR_BORDER_DEFAULT);
	}
	
	protected abstract T parseValue(String text);
	protected String stringifyValue(T value) { return value.toString(); }
	
	
	public static class Text extends EntryValueField<String> {
		@Override
		protected String parseValue(String text) { return text; }
	}
	
	public static class Number extends EntryValueField<Integer> {
		@Override
		protected Integer parseValue(String text)
			{ return Integer.parseInt(text); }
		@Override
		protected boolean isCharValid(char chr) {
			String validChars = "0123456789";
			return (validChars.contains(String.valueOf(chr)) ||
			        ((chr == '-') && (getCursorPosition() == 0) && !getText().startsWith("-")));
		}
	}
	
	public static class Decimal extends EntryValueField<Double> {
		@Override
		protected Double parseValue(String text)
			{ return Double.parseDouble(text); }
		@Override
		protected boolean isCharValid(char chr) {
			String validChars = "0123456789";
			return (validChars.contains(String.valueOf(chr)) ||
			        ((chr == '-') && (getCursorPosition() == 0) && !getText().startsWith("-")) ||
			        ((chr == '.') && !getText().contains(".")));
		}
	}
	
}
