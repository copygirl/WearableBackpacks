package net.mcft.copy.backpacks.client.gui.control;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonGlyph extends GuiButton {
	
	public static final int GLYPH_SPACING = getStringWidth(" ");
	public static final int DEFAULT_WIDTH = 20;
	public static final float DEFAULT_GLYPH_SCALE = 2.0f;
	
	private String _glyph;
	private float _glyphScale;
	private int _glyphWidth;
	
	
	public GuiButtonGlyph(String glyph)
		{ this(glyph, DEFAULT_GLYPH_SCALE); }
	public GuiButtonGlyph(String glyph, float glyphScale)
		{ this(DEFAULT_WIDTH, glyph, glyphScale, ""); }
	
	public GuiButtonGlyph(String glyph, String text)
		{ this((int)(getStringWidth(glyph) * 2.0f) + GLYPH_SPACING + getStringWidth(text) + DEFAULT_TEXT_PADDING, glyph, text); }
	public GuiButtonGlyph(int width, String glyph, String text)
		{ this(width, glyph, DEFAULT_GLYPH_SCALE, text); }
		
	public GuiButtonGlyph(int width, String glyph, float glyphScale, String text)
		{ this(0, 0, width, DEFAULT_HEIGHT, glyph, glyphScale, text); }
	public GuiButtonGlyph(int x, int y, int width, int height, String glyph, float glyphScale, String text)
		{ super(x, y, width, height, text); setGlyph(glyph, glyphScale); }
	
	
	public String getGlyph() { return _glyph; }
	public float getGlyphScale() { return _glyphScale; }
	public int getGlyphWidth() { return _glyphWidth; }
	
	public void setGlyph(String glyph) { setGlyph(glyph, getGlyphScale()); }
	public void setGlyph(String glyph, float glyphScale) {
		_glyph = glyph;
		_glyphScale = glyphScale;
		_glyphWidth = (int)(getStringWidth(glyph) * glyphScale);
	}
	
	
	@Override
	protected void drawWhateverIsOnTheButton(int mouseX, int mouseY, boolean isHighlighted, float partialTicks) {
		String text = getText();
		FontRenderer fontRenderer = getFontRenderer();
		
		int contentWidth = getGlyphWidth();
		if (!text.isEmpty()) {
			int textWidth = fontRenderer.getStringWidth(text);
			int maxTextWidth = getWidth() - getGlyphWidth() - GLYPH_SPACING - MIN_TEXT_PADDING;
			if ((textWidth > maxTextWidth) && (textWidth > ELLIPSIS_WIDTH)) {
				text = fontRenderer.trimStringToWidth(text, maxTextWidth - ELLIPSIS_WIDTH).trim() + ELLIPSIS;
				textWidth = fontRenderer.getStringWidth(text);
			}
			contentWidth += GLYPH_SPACING + textWidth;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.scale(getGlyphScale(), getGlyphScale(), 1.0F);
		fontRenderer.drawStringWithShadow(getGlyph(),
			(int)((getWidth() - contentWidth) / 2 / getGlyphScale()),
			(int)((((getHeight() - 8) / getGlyphScale() / 2) - 1) / getGlyphScale()), getTextColor(isHighlighted));
		GlStateManager.popMatrix();
		
		if (!text.isEmpty())
			fontRenderer.drawStringWithShadow(text,
				(getWidth() - contentWidth) / 2 + getGlyphWidth() + GLYPH_SPACING,
				(getHeight() - 8) / 2, getTextColor(isHighlighted));
	}
	
}
