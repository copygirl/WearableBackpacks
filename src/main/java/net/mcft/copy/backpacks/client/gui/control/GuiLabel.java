package net.mcft.copy.backpacks.client.gui.control;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;

@SideOnly(Side.CLIENT)
public class GuiLabel extends GuiElementBase {
	
	public static final int SHADOW_DISABLED = -1;
	public static final int SHADOW_AUTOMATIC = -2;
	
	
	private String _text = "";
	private TextAlign _align = TextAlign.LEFT;
	private boolean _expands;
	
	private int _color         = Color.WHITE;
	private int _disabledColor = COLOR_CONTROL_DISABLED;
	private int _shadowColor   = SHADOW_AUTOMATIC;
	
	
	public GuiLabel(String text)
		{ this(text, TextAlign.LEFT); }
	public GuiLabel(String text, TextAlign align)
		{ this(0, 0, text, align); }
	public GuiLabel(int x, int y, String text)
		{ this(x, y, text, TextAlign.LEFT); }
	public GuiLabel(int x, int y, String text, TextAlign align) {
		_expands = true;
		setPosition(x, y);
		setText(text);
		setTextAlign(align);
	}
	
	public GuiLabel(int width, String text)
		{ this(width, text, TextAlign.LEFT); }
	public GuiLabel(int width, String text, TextAlign align)
		{ this(0, 0, width, text, align); }
	public GuiLabel(int x, int y, int width, String text)
		{ this(x, y, width, LINE_HEIGHT, text, TextAlign.LEFT); }
	public GuiLabel(int x, int y, int width, String text, TextAlign align)
		{ this(x, y, width, LINE_HEIGHT, text, align); }
	public GuiLabel(int x, int y, int width, int height, String text)
		{ this(x, y, width, height, text, TextAlign.LEFT); }
	public GuiLabel(int x, int y, int width, int height, String text, TextAlign align) {
		setPosition(x, y);
		setSize(width, height);
		setText(text);
		setTextAlign(align);
	}
	
	
	/** Returns if this label expands its size depending on text contents. */
	public boolean getExpands() { return _expands; }
	
	public TextAlign getTextAlign() { return _align; }
	public void setTextAlign(TextAlign value) { _align = value; }
	
	
	public String getText() { return _text; }
	public void setText(String text) {
		if (text == null) throw new NullPointerException("Argument text can't be null");
		_text = text;
		if (_expands) {
			FontRenderer fontRenderer = getFontRenderer();
			int width  = fontRenderer.getStringWidth(_text);
			int height = fontRenderer.getWordWrappedHeight(_text, Integer.MAX_VALUE);
			setSize(width, height);
		}
	}
	
	public int getColor() { return _color; }
	public void setColor(int value) { _color = value; }
	
	public int getDisabledColor() { return _disabledColor; }
	public void setDisabledColor(int value) { _disabledColor = value; }
	
	public int getShadowColor() { return _shadowColor; }
	public void setShadowColor(int value) {
		if (value < SHADOW_AUTOMATIC) throw new IllegalArgumentException(
			String.format("'%d' is not a valid special shadow value", value));
		_shadowColor = value;
	}
	public void setShadowDisabled() { setShadowColor(SHADOW_DISABLED); }
	
	
	@Override
	public void setSize(Direction direction, int value) {
		super.setSize(direction, value);
		_expands = false;
	}
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		if (!isVisible()) return;
		FontRenderer fontRenderer = getFontRenderer();
		List<String> lines = fontRenderer.listFormattedStringToWidth(_text, Integer.MAX_VALUE);
		int yPos = 1;
		for (String line : lines) {
			int lineWidth = fontRenderer.getStringWidth(line);
			if (!_expands && (lineWidth > getWidth())) {
				line = fontRenderer.trimStringToWidth(line, getWidth() - ELLIPSIS_WIDTH) + ELLIPSIS;
				lineWidth = fontRenderer.getStringWidth(line);
			}
			int xPos = 0;
			switch (_align) {
				case LEFT:   xPos = 0; break;
				case RIGHT:  xPos = getWidth() - lineWidth; break;
				case CENTER: xPos = (getWidth() - lineWidth) / 2; break;
			}
			
			int color = isEnabled() ? getColor() : getDisabledColor();
			int shadowColor = getShadowColor();
			if (shadowColor >= 0)
				fontRenderer.drawString(line, xPos + 1, yPos + 1, shadowColor);
			if (shadowColor == SHADOW_AUTOMATIC)
				fontRenderer.drawStringWithShadow(line, xPos, yPos, color);
			else fontRenderer.drawString(line, xPos, yPos, color);
			
			yPos += LINE_HEIGHT;
		}
	}
	
	public enum TextAlign {
		LEFT,
		RIGHT,
		CENTER
	}
	
}