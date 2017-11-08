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
	
	public static final int COLOR_TEXT_DEFAULT       = COLOR_CONTROL;
	public static final int COLOR_BACKGROUND_DEFAULT = 0xFF000000;
	public static final int COLOR_BORDER_DEFAULT     = 0xFFA0A0A0;
	
	public static final int COLOR_TEXT_DISABLED      = 0xFF707070;
	
	
	private final GuiTextField _field = new GuiTextField(0, getFontRenderer(), 1, 1, 0, 0);
	private int _colorText       = COLOR_TEXT_DEFAULT;
	private int _colorBackground = COLOR_BACKGROUND_DEFAULT;
	private int _colorBorder     = COLOR_BORDER_DEFAULT;
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
	
	
	public void setTextColor(int value)
		{ _colorText = ((value & 0xFF000000) == 0) ? (value | 0xFF000000) : value; }
	public void resetTextColor() { setTextColor(COLOR_TEXT_DEFAULT); }
	
	public void setBackgroundColor(int value)
		{ _colorBackground = ((value & 0xFF000000) == 0) ? (value | 0xFF000000) : value; }
	public void resetBackgroundColor() { setBorderColor(COLOR_BACKGROUND_DEFAULT); }
	
	public void setBorderColor(int value)
		{ _colorBorder = ((value & 0xFF000000) == 0) ? (value | 0xFF000000) : value; }
	public void resetBorderColor() { setBorderColor(COLOR_BORDER_DEFAULT); }
	
	// FIXME: Remove me!
	public void setTextAndBorderColor(int value, boolean setElseDefault) {
		setTextColor(setElseDefault ? value : COLOR_TEXT_DEFAULT);
		setBorderColor(setElseDefault ? value : COLOR_BORDER_DEFAULT);
	}
	
	
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
		
		// Draw background and border.
		enableBlendAlphaStuffs();
			setRenderColorARGB(_colorBackground);
			drawRect(1, 1, getWidth() - 2, getHeight() - 2);
			
			setRenderColorARGB(_colorBorder);
			drawOutline(0, 0, getWidth(), getHeight());
		disableBlendAlphaStuffs();
		
		// Draw text box.
		//   Disabling background drawing to do our own (beforehand). This requires
		//   adjusting field position and width to have it render in its usual way.
		_field.setEnableBackgroundDrawing(false);
		_field.x = 5;
		_field.y = (getHeight() - 8) / 2;
		_field.width -= 8;
			_field.setTextColor(_colorText);
			_field.setEnabled(isEnabled());
			_field.setFocused(isFocused());
			_field.drawTextBox();
		_field.x = _field.y = 1;
		_field.width += 8;
		_field.setEnableBackgroundDrawing(true);
	}
	
}
