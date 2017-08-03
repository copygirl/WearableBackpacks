package net.mcft.copy.backpacks.client.gui.control;

import java.util.function.Predicate;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiTextField;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;

@SideOnly(Side.CLIENT)
public class GuiField extends GuiElementBase {
	
	public static final int DEFAULT_WIDTH  = 200;
	public static final int DEFAULT_HEIGHT = 20;
	
	public static final int COLOR_DEFAULT  = 0xFFE0E0E0;
	public static final int COLOR_DISABLED = 0xFF707070;
	
	
	private final GuiTextField _field = new GuiTextField(0, getFontRenderer(), 1, 1, 0, 0);
	private Runnable _changedAction = null;
	private Predicate<Character> _charValidator = null;
	
	public GuiField() { this(DEFAULT_WIDTH); }
	public GuiField(int width) { this(width, DEFAULT_HEIGHT); }
	public GuiField(int width, int height) { this(width, height, ""); }
	
	public GuiField(String text)
		{ this(DEFAULT_WIDTH, text); }
	public GuiField(int width, String text)
		{ this(width, DEFAULT_HEIGHT, text); }
	public GuiField(int width, int height, String text)
		{ this(0, 0, width, height, text); }
	
	public GuiField(int x, int y, int width, int height, String text) {
		setPosition(x, y);
		setSize(width, height);
		setText(text);
		setMaxLength(Integer.MAX_VALUE);
	}
	
	
	public String getText() { return _field.getText(); }
	public void setText(String value) {
		if (value == null) throw new NullPointerException("Argument can't be null");
		if (value.equals(getText())) return;
		_field.setText(value);
		_field.setCursorPositionZero();
	}
	
	public int getMaxLength() { return _field.getMaxStringLength(); }
	public void setMaxLength(int value) { _field.setMaxStringLength(value); }
	
	public int getCursorPosition() { return _field.getCursorPosition(); }
	
	public void setTextColor(int value) { _field.setTextColor(value); }
	public void setDisabledTextColor(int value) { _field.setDisabledTextColour(value); }
	
	
	public void setChangedAction(Runnable value) { _changedAction = value; }
	protected void onTextChanged()
		{ if (_changedAction != null) _changedAction.run(); }
	
	public void setCharValidator(Predicate<Character> value) { _charValidator = value; }
	protected boolean isCharValid(char chr)
		{ return (_charValidator != null) ? _charValidator.test(chr) : true; }
	
	
	@Override
	public boolean canFocus() { return true; }
	
	@Override
	public void onSizeChanged(Direction direction) {
		if (direction == Direction.HORIZONTAL)
			_field.width = getWidth() - 2;
		else _field.height = getHeight() - 2;
	}
	
	
	// TODO: Select all text if focused for the first time. (Requires focus changes.)
	@Override
	public void onPressed(int mouseX, int mouseY)
		{ _field.mouseClicked(mouseX, mouseY, MouseButton.LEFT); }
	
	@Override
	public void onKey(int keyCode, char keyChar) {
		if (!isEnabled() && (keyCode != Keyboard.KEY_LEFT) && (keyCode != Keyboard.KEY_RIGHT) &&
		                    (keyCode != Keyboard.KEY_HOME) && (keyCode != Keyboard.KEY_END)) return;
		
		String beforeText = getText();
		if (!Character.isISOControl(keyChar) && !isCharValid(keyChar)) return;
		_field.textboxKeyTyped(keyChar, keyCode);
		if (!getText().equals(beforeText)) onTextChanged();
	}
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		if (!isVisible()) return;
		_field.setEnabled(isEnabled());
		_field.setFocused(isFocused());
		_field.drawTextBox();
	}
	
}
