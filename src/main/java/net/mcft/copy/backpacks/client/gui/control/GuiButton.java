package net.mcft.copy.backpacks.client.gui.control;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.GuiElementBase;

@SideOnly(Side.CLIENT)
public class GuiButton extends GuiElementBase {
	
	public static final ResourceLocation BUTTON_TEX = new ResourceLocation("textures/gui/widgets.png");
	public static final int DEFAULT_WIDTH  = 200;
	public static final int DEFAULT_HEIGHT = 20;
	public static final int MIN_TEXT_PADDING     = 6;
	public static final int DEFAULT_TEXT_PADDING = 20;
	
	
	private String _text     = "";
	private int _textColor   = -1;
	private Runnable _action = null;
	
	
	public GuiButton() { this(DEFAULT_WIDTH); }
	public GuiButton(int width) { this(width, DEFAULT_HEIGHT); }
	public GuiButton(int width, int height) { this(width, height, ""); }
	
	public GuiButton(String text)
		{ this(getStringWidth(text) + DEFAULT_TEXT_PADDING, text); }
	public GuiButton(int width, String text)
		{ this(width, DEFAULT_HEIGHT, text); }
	public GuiButton(int width, int height, String text)
		{ this(0, 0, width, height, text); }
	
	public GuiButton(int x, int y, int width, int height, String text) {
		setPosition(x, y);
		setSize(width, height);
		setText(text);
	}
	
	
	public String getText() { return _text; }
	public void setText(String value) {
		if (value == null) throw new NullPointerException("Argument can't be null");
		_text = value;
	}
	public final void addText(String value) {
		if (value == null) throw new NullPointerException("Argument can't be null");
		setText(getText() + value);
	}
	
	public int getTextColor() { return getTextColor(false); }
	public int getTextColor(boolean isHighlighted) {
		return (_textColor >= 0) ? _textColor
			: !isEnabled() ? COLOR_CONTROL_DISABLED
			: (isHighlighted) ? COLOR_CONTROL_HIGHLIGHT
			: COLOR_CONTROL;
	}
	public final void unsetTextColor() { setTextColor(-1); }
	public void setTextColor(int value) { _textColor = value; }
	
	public void setAction(Runnable value) { _action = value; }
	
	public void playPressSound() {
		getMC().getSoundHandler().playSound(
			PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
	
	@Override
	public boolean canPress() { return true; }
	@Override
	public void onPressed(int mouseX, int mouseY) {
		if (!isEnabled()) return;
		playPressSound();
		if (_action != null) _action.run();
	}
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		if (!isVisible()) return;
		boolean isHighlighted = (isEnabled() && isDragged() || contains(mouseX, mouseY));
		
		int buttonIndex = !isEnabled() ? 0
		                : !isHighlighted ? 1
		                : 2;
		int ty = 46 + buttonIndex * 20;
		GuiUtils.drawContinuousTexturedBox(BUTTON_TEX, 0, 0, 0, ty, getWidth(), getHeight(),
		                                   DEFAULT_WIDTH, DEFAULT_HEIGHT, 2, 3, 2, 2, 0);
		
		drawWhateverIsOnTheButton(mouseX, mouseY, isHighlighted, partialTicks);
	}
	
	/** Draws whatever is on the button. Yay. */
	protected void drawWhateverIsOnTheButton(int mouseX, int mouseY, boolean isHighlighted, float partialTicks) {
		String text = getText();
		if (text.isEmpty()) return;
		FontRenderer fontRenderer = getFontRenderer();
		
		int textWidth = fontRenderer.getStringWidth(text);
		int maxTextWidth = getWidth() - MIN_TEXT_PADDING;
		if ((textWidth > maxTextWidth) && (textWidth > ELLIPSIS_WIDTH)) {
			text = fontRenderer.trimStringToWidth(text, maxTextWidth - ELLIPSIS_WIDTH).trim() + ELLIPSIS;
			textWidth = fontRenderer.getStringWidth(text);
		}
		
		fontRenderer.drawStringWithShadow(text, getWidth() / 2 - textWidth / 2,
		                                        (getHeight() - 8) / 2, getTextColor(isHighlighted));
	}
	
}
