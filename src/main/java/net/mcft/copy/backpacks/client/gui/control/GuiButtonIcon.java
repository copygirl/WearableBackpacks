package net.mcft.copy.backpacks.client.gui.control;

import net.minecraft.client.gui.FontRenderer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.GuiTextureResource;

@SideOnly(Side.CLIENT)
public class GuiButtonIcon extends GuiButton {
	
	public static final int ICON_SPACING = getStringWidth(" ");
	public static final int DEFAULT_WIDTH = DEFAULT_HEIGHT;
	
	private Icon _icon;
	
	
	public GuiButtonIcon(Icon icon)
		{ this(DEFAULT_WIDTH, icon); }
	public GuiButtonIcon(int width, Icon icon)
		{ this(width, DEFAULT_HEIGHT, icon); }
	public GuiButtonIcon(int width, int height, Icon icon)
		{ this(width, height, icon, ""); }

	public GuiButtonIcon(Icon icon, String text)
		{ this(icon.width + ICON_SPACING + getStringWidth(text) + DEFAULT_TEXT_PADDING, icon, text); }
	
	public GuiButtonIcon(int width, Icon icon, String text)
		{ this(width, DEFAULT_HEIGHT, icon, text); }
	public GuiButtonIcon(int width, int height, Icon icon, String text)
		{ this(0, 0, width, height, icon, text); }
	public GuiButtonIcon(int x, int y, int width, int height, Icon icon, String text)
		{ super(x, y, width, height, text); setIcon(icon); }
	
	
	public void setIcon(Icon icon) { _icon = icon;; }
	
	
	@Override
	protected void drawButtonForeground(boolean isHighlighted, float partialTicks) {
		String text = getText();
		FontRenderer fontRenderer = getFontRenderer();
		
		int contentWidth = _icon.width;
		if (!text.isEmpty()) {
			int textWidth = fontRenderer.getStringWidth(text);
			int maxTextWidth = getWidth() - _icon.width - ICON_SPACING - MIN_TEXT_PADDING;
			if ((textWidth > maxTextWidth) && (textWidth > ELLIPSIS_WIDTH)) {
				text = fontRenderer.trimStringToWidth(text, maxTextWidth - ELLIPSIS_WIDTH).trim() + ELLIPSIS;
				textWidth = fontRenderer.getStringWidth(text);
			}
			contentWidth += ICON_SPACING + textWidth;
		}
		
		int x = (getWidth() - contentWidth) / 2;
		int y = (getHeight() - _icon.height) / 2;
		_icon.texture.bind();
		_icon.texture.drawQuad(x, y, _icon.u, _icon.v, _icon.width, _icon.height);
		
		if (!text.isEmpty())
			fontRenderer.drawStringWithShadow(text,
				x + _icon.width + ICON_SPACING,
				(getHeight() - 8) / 2, getTextColor(isHighlighted));
	}
	
	public static final class Icon {
		public final GuiTextureResource texture;
		public final int u, v, width, height;
		
		public Icon(GuiTextureResource texture, int u, int v, int width, int height)
			{ this.texture = texture; this.u = u; this.v = v; this.width = width; this.height = height; }
	}
	
}
