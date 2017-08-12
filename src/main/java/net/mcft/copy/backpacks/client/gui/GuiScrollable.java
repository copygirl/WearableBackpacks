package net.mcft.copy.backpacks.client.gui;

import java.util.EnumSet;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.math.MathHelper;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.control.GuiSlider;

@SideOnly(Side.CLIENT)
public class GuiScrollable extends GuiContainer {
	
	private static final int GRADIENT_SIZE = 4;
	
	public final EnumSet<Direction> directions;
	
	private GuiScrollbar _scrollbarHor, _scrollbarVer;
	private int _scrollAmount = 10;
	private int _scrollX = 0, _scrollY = 0;
	private int _scrollStartX, _scrollStartY;
	
	private int _contentWidth = 0, _contentHeight = 0;
	private int _contentRight = 0, _contentBottom = 0;
	
	public GuiScrollable()
		{ this(EnumSet.allOf(Direction.class)); }
	public GuiScrollable(Direction direction)
		{ this(EnumSet.of(direction)); }
	public GuiScrollable(EnumSet<Direction> directions) {
		if (directions.isEmpty()) throw new IllegalArgumentException(
			"Argument 'directions' must be at least one Direction");
		this.directions = directions;
		for (Direction direction : directions) {
			GuiScrollbar scrollbar = new GuiScrollbar(direction);
			if (direction == Direction.HORIZONTAL)
				_scrollbarHor = scrollbar; else _scrollbarVer = scrollbar;
			add(scrollbar);
		}
	}
	
	
	public GuiScrollbar getScrollbar(Direction direction)
		{ return (direction == Direction.HORIZONTAL) ? _scrollbarHor : _scrollbarVer; }
	public final GuiScrollbar getScrollbar() {
		if (directions.size() != 1) throw new UnsupportedOperationException(
			"Element slides in both directions");
		return getScrollbar(directions.iterator().next());
	}
	
	public int getScroll(Direction direction)
		{ return (direction == Direction.HORIZONTAL) ? _scrollX : _scrollY; }
	public final int getScrollX() { return getScroll(Direction.HORIZONTAL); }
	public final int getScrollY() { return getScroll(Direction.VERTICAL); }
	
	public void setScroll(Direction direction, int value) {
		value = MathHelper.clamp(value, 0, getMaxScroll(direction));
		if (direction == Direction.HORIZONTAL) {
			if (value == _scrollX) return;
			_scrollX = value;
		} else {
			if (value == _scrollY) return;
			_scrollY = value;
		}
		onScrollChanged(direction);
	}
	public final void setScrollX(int value) { setScroll(Direction.HORIZONTAL, value); }
	public final void setScrollY(int value) { setScroll(Direction.VERTICAL, value); }
	
	public int getMaxScroll(Direction direction)
		{ return Math.max(0, getContentSize(direction) - getSize(direction)); }
	
		
	public int getContentSize(Direction direction)
		{ return (direction == Direction.HORIZONTAL) ? _contentWidth : _contentHeight; }
	
	private int getContentMax(Direction direction)
		{ return (direction == Direction.HORIZONTAL) ? _contentRight : _contentBottom; }
	private void resetContentMax(Direction direction)
		{ if (direction == Direction.HORIZONTAL) _contentRight = 0; else _contentBottom = 0; }
	private void contentMax(Direction direction, int value) {
		if (direction == Direction.HORIZONTAL)
			_contentRight = Math.max(_contentRight, value);
		else _contentBottom = Math.max(_contentBottom, value);
	}
	
	
	protected void onScrollChanged(Direction direction) {  }
	
	
	@Override
	public boolean canDrag() { return true; }
	@Override
	public void onPressed(int mouseX, int mouseY) {
		_scrollStartX = _scrollX;
		_scrollStartY = _scrollY;
	}
	@Override
	public void onDragged(int mouseX, int mouseY, int startX, int startY) {
		setScrollX(_scrollStartX + (startX - mouseX));
		setScrollY(_scrollStartY + (startY - mouseY));
	}
	
	@Override
	public boolean onMouseScroll(int scroll, int mouseX, int mouseY) {
		if (isDragged()) return false;
		if (super.onMouseScroll(scroll, mouseX, mouseY)) return true;
		setScrollY(getScrollY() - scroll * _scrollAmount);
		return true;
	}
	
	@Override
	public int getChildPos(GuiElementBase element, Direction direction)
		{ return getChildPos(element, direction, true); }
	protected int getChildPos(GuiElementBase element, Direction direction, boolean scroll) {
		Alignment align = element.getAlign(direction);
		int scrollAmount = (scroll ? getScroll(direction) : 0);
		return (align instanceof ContentMax)
				? Math.min(getSize(direction) - element.getSize(direction),
				           getContentMax(direction) + ((ContentMax)align).margin)
			: (align instanceof Alignment.Max)
				? (align instanceof FixedMax ? getSize(direction)
					: getContentSize(direction) - scrollAmount - getPaddingMax(direction))
				- element.getSize(direction) - ((Alignment.Max)align).max
			: (align instanceof IFixedAlign)
				? super.getChildPos(element, direction) - getPaddingMin(direction)
			: (super.getChildPos(element, direction) - scrollAmount);
	}
	
	@Override
	protected void updateChildSizes(Direction direction) {
		for (GuiElementBase child : children) {
			Alignment align = child.getAlign(direction);
			if (align instanceof Alignment.Both) {
				Alignment.Both both = (Alignment.Both)align;
				int size = (both instanceof FixedBoth)
					? getSize(direction) : getContentSize(direction);
				child.setSize(direction, size - both.min - both.max);
			}
		}
	}
	
	@Override
	protected void expandToFitChildren(Direction direction) {
		int max = 0;
		int extra = 0;
		resetContentMax(direction);
		for (GuiElementBase child : children) {
			Alignment align = child.getAlign(direction);
			if (align instanceof ContentMax)
				extra = Math.max(extra, ((ContentMax)align).margin + child.getSize(direction));
			else if (align instanceof Alignment.Center) {
				int size = child.getSize(direction);
				max = Math.max(max, size);
				contentMax(direction, getChildPos(child, direction, false) + size);
			} else if (!(align instanceof Alignment.Both)) {
				int maxPos = getChildPos(child, direction, false)
					- getPaddingMin(direction) + child.getSize(direction);
				max = Math.max(max, maxPos);
				contentMax(direction, maxPos);
			}
		}
		max += extra + getPadding(direction);
		if (direction == Direction.HORIZONTAL)
			_contentWidth = max; else _contentHeight = max;
		if (getScroll(direction) > getMaxScroll(direction))
			setScroll(direction, getMaxScroll(direction));
	}
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		if (!isVisible()) return;
		
		// TODO: Generalize this into GuiContext and allow nested scissors?
		ElementInfo info = ElementInfo.getElementHierarchy(this).getFirst();
		int scale = new ScaledResolution(getMC()).getScaleFactor();
		GL11.glScissor(info.globalX * scale, getMC().displayHeight - (info.globalY + getHeight()) * scale,
		               getWidth() * scale, getHeight() * scale);
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		
		drawBackground(info.globalX + getScrollX(), info.globalY + getScrollY(),
		               mouseX, mouseY, partialTicks);
		
		// Skip drawing the scrollbars.
		foreachChildMousePos(mouseX, mouseY, (child, x, y, mx, my) -> {
			if (child instanceof GuiScrollbar) return;
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, 0);
			child.draw(mx, my, partialTicks);
			GlStateManager.popMatrix();
		});
		
		drawForeground(mouseX, mouseY, partialTicks);
		
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
	
	public void drawBackground(int x, int y, int mouseX, int mouseY, float partialTicks) {
		float scale = 1 / 32.0F;
		float u1 = x * scale;
		float v1 = y * scale;
		float u2 = (x + getWidth()) * scale;
		float v2 = (y + getHeight()) * scale;
		
		bindTexture(Gui.OPTIONS_BACKGROUND);
		setRenderColorRGB(0x202020);
		drawRect(0, 0, getWidth(), getHeight(), u1, v1, u2, v2);
	}
	
	public void drawForeground(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(
			SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA,
			SourceFactor.ZERO, DestFactor.ONE);
		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		if (directions.contains(Direction.VERTICAL)) {
			drawColoredRectARGB(0, 0, getWidth(), GRADIENT_SIZE, Color.BLACK, Color.BLACK, Color.TRANSPARENT, Color.TRANSPARENT);
			drawColoredRectARGB(0, getHeight() - GRADIENT_SIZE, getWidth(), GRADIENT_SIZE, Color.TRANSPARENT, Color.TRANSPARENT, Color.BLACK, Color.BLACK);
		}
		if (directions.contains(Direction.HORIZONTAL)) {
			drawColoredRectARGB(0, 0, GRADIENT_SIZE, getHeight(), Color.BLACK, Color.TRANSPARENT, Color.BLACK, Color.TRANSPARENT);
			drawColoredRectARGB(getWidth() - GRADIENT_SIZE, 0, GRADIENT_SIZE, getHeight(), Color.BLACK, Color.TRANSPARENT, Color.BLACK, Color.TRANSPARENT);
		}
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		//GuiConfig
		
		// Draw only the scrollbars.
		foreachChildMousePos(mouseX, mouseY, (child, x, y, mx, my) -> {
			if (!(child instanceof GuiScrollbar)) return;
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, 0);
			child.draw(mx, my, partialTicks);
			GlStateManager.popMatrix();
		});
	}
	
	
	public class GuiScrollbar extends GuiSlider {
		
		public static final int THICKNESS = 6;
		
		public final Direction direction;
		
		private int _scrollStart;
		
		private GuiScrollbar(Direction direction) {
			super(0, 0, (direction == Direction.HORIZONTAL) ? DEFAULT_WIDTH : THICKNESS,
			            (direction == Direction.VERTICAL)   ? DEFAULT_WIDTH : THICKNESS,
			      direction);
			this.direction = direction;
			setAlign(direction, new FixedBoth(0, 0));
			setAlign(direction.perpendicular(), new FixedMax(0));
			setAmount(0);
		}
		
		@Override
		public int getSliderSize() {
			return MathHelper.clamp((int)Math.pow(GuiScrollable.this.getSize(direction), 2)
				/ getContentSize(direction), 32, getSize(direction) - 8);
		}
		
		@Override
		public double getAmount(Direction direction)
			{ return (double)getScroll(direction) / getMaxScroll(direction); }
		
		@Override
		public void onPressed(int mouseX, int mouseY) {
			_scrollStart = getScroll(direction);
			super.onPressed(mouseX, mouseY);
		}
		@Override
		public void onDragged(int mouseX, int mouseY, int startX, int startY) {
			int diff = (direction == Direction.HORIZONTAL) ? (mouseX - startX) : (mouseY - startY);
			double scrollFactor = (double)(getSize(direction) - getSliderSize()) / Math.max(1, getMaxScroll(direction));
			setScroll(direction, _scrollStart + (int)(diff / scrollFactor));
		}
		
		@Override
		public String getValueText() { return ""; }
		
		@Override
		public void draw(int mouseX, int mouseY, float partialTicks) {
			if (!isVisible() || (getMaxScroll(direction) == 0)) return;
			
			int sliderSize = getSliderSize();
			int sliderPosition = (int)(getAmount() * (getSize(direction) - sliderSize));
			int x, y, w, h;
			if (direction == Direction.HORIZONTAL) {
				x = sliderPosition; y = 0;
				w = sliderSize; h = getHeight();
			} else {
				x = 0; y = sliderPosition;
				w = getWidth(); h = sliderSize;
			}
			
			GlStateManager.disableTexture2D();
			drawColoredRectRGB(0, 0, getWidth(), getHeight(), Color.BLACK);
			drawColoredRectRGB(x, y, w, h, 0x808080);
			drawColoredRectRGB(x, y, w - 1, h - 1, 0xC0C0C0);
			GlStateManager.enableTexture2D();
		}
		
	}
	
	
	public interface IFixedAlign {  }
	
	public static class FixedMin extends Alignment.Min implements IFixedAlign
		{ public FixedMin(int min) { super(min); } }
	public static class FixedMax extends Alignment.Max implements IFixedAlign
		{ public FixedMax(int max) { super(max); } }
	public static class FixedBoth extends Alignment.Both implements IFixedAlign
		{ public FixedBoth(int min, int max) { super(min, max); } }
	
	public static class ContentMax extends Alignment {
		public final int margin;
		public ContentMax(int margin) { this.margin = margin; }
	}
	
}
