package net.mcft.copy.backpacks.client.gui.control;

import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.function.DoubleFunction;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.MathHelper;

import net.minecraftforge.fml.client.config.GuiUtils;

import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;

public class GuiSlider extends GuiElementBase {
	
	public static final DecimalFormat DEFAULT_FORMATTER = new DecimalFormat("0.##");
	
	public static final int DEFAULT_WIDTH  = GuiButton.DEFAULT_WIDTH;
	public static final int DEFAULT_HEIGHT = GuiButton.DEFAULT_HEIGHT;
	
	public final EnumSet<Direction> directions;
	
	private double _amountX = 0.5, _amountY = 0.5;
	private double _minX    = 0.0, _minY    = 0.0;
	private double _maxX    = 1.0, _maxY    = 1.0;
	private double _stepX   = 0.0, _stepY   = 0.0;
	private int _sliderSize = 8;
	private Runnable _changedAction = null;
	private DoubleFunction<String> _valueFormatter = DEFAULT_FORMATTER::format;
	
	
	public GuiSlider() { this (Direction.HORIZONTAL); }
	public GuiSlider(Direction direction) { this(DEFAULT_WIDTH, direction); }
	
	public GuiSlider(int size)
		{ this(size, Direction.HORIZONTAL); }
	public GuiSlider(int size, Direction direction)
		{ this((direction == Direction.HORIZONTAL) ? size : DEFAULT_HEIGHT,
		       (direction == Direction.VERTICAL) ? size : DEFAULT_HEIGHT, direction); }
	
	public GuiSlider(int width, int height)
			{ this(width, height, Direction.HORIZONTAL); }
	public GuiSlider(int width, int height, Direction direction)
		{ this(width, height, EnumSet.of(direction)); }
	public GuiSlider(int width, int height, EnumSet<Direction> directions)
		{ this(0, 0, width, height, directions); }
	
	public GuiSlider(int x, int y, int width, int height, Direction direction)
		{ this(x, y, width, height, EnumSet.of(direction)); }
	public GuiSlider(int x, int y, int width, int height, EnumSet<Direction> directions) {
		if (directions.isEmpty()) throw new IllegalArgumentException(
			"Argument 'directions' must be at least one Direction");
		setPosition(x, y);
		setSize(width, height);
		this.directions = directions;
	}
	
	public int getSliderSize() { return _sliderSize; }
	public void setSliderSize(int value) { _sliderSize = value; }
	
	
	// Raw amount related
	
	public double getSliderRaw(Direction direction) {
		ensureValidDirection(direction);
		return (direction == Direction.HORIZONTAL) ? _amountX : _amountY;
	}
	public final double getSliderRawX() { return getSliderRaw(Direction.HORIZONTAL); }
	public final double getSliderRawY() { return getSliderRaw(Direction.VERTICAL); }
	public final double getSliderRaw() { return getSliderRaw(getOnlyDirection()); }
	
	public void setSliderRaw(Direction direction, double value)
		{ setSliderRawInternal(direction, value, true); }
	private boolean setSliderRawInternal(Direction direction, double value, boolean fireChanged) {
		ensureValidDirection(direction);
		value = MathHelper.clamp(value, 0, 1);
		
		double stepSize = getStepSize(direction);
		if (stepSize > 0) {
			double min = getMin(direction);
			double max = getMax(direction);
			value = (min + value * (max - min));
			value = Math.round(value / stepSize) * stepSize;
			value = (value - min) / (max - min);
		}
		
		if (direction == Direction.HORIZONTAL) {
			if (value == _amountX) return false;
			_amountX = value;
		} else {
			if (value == _amountY) return false;
			_amountY = value;
		}
		if (fireChanged) onChanged();
		return true;
	}
	public final void setSliderRawX(double value) { setSliderRaw(Direction.HORIZONTAL, value); }
	public final void setSliderRawY(double value) { setSliderRaw(Direction.VERTICAL, value); }
	public final void setSliderRaw(double value) { setSliderRaw(getOnlyDirection(), value); }
	public final void setSliderRaw(double hor, double vert) {
		if (setSliderRawInternal(Direction.HORIZONTAL, hor, false) |
			setSliderRawInternal(Direction.VERTICAL, vert, false))
			onChanged();
	}
	
	public void setChangedAction(Runnable value) { _changedAction = value; }
	/** Called when the slider amount / value changes. */
	protected void onChanged()
		{ if (_changedAction != null) _changedAction.run(); }
	
	
	// Value related
	
	public double getMin(Direction direction) {
		ensureValidDirection(direction);
		return (direction == Direction.HORIZONTAL) ? _minX : _minY;
	}
	public double getMax(Direction direction) {
		ensureValidDirection(direction);
		return (direction == Direction.HORIZONTAL) ? _maxX : _maxY;
	}
	public void setRange(Direction direction, double min, double max) {
		ensureValidDirection(direction);
		if (min > max) throw new IllegalArgumentException("min is greater than max");
		if (direction == Direction.HORIZONTAL)
			{ _minX = min; _maxX = max; }
		else { _minY = min; _maxY = max; }
	}
	public void setRangeX(double min, double max) { setRange(Direction.HORIZONTAL, min, max); }
	public void setRangeY(double min, double max) { setRange(Direction.VERTICAL, min, max); }
	public void setRange(double min, double max) { setRange(getOnlyDirection(), min, max); }
	
	
	public double getStepSize(Direction direction) {
		ensureValidDirection(direction);
		return (direction == Direction.HORIZONTAL) ? _stepX : _stepY;
	}
	public void setStepSize(Direction direction, double value) {
		ensureValidDirection(direction);
		if (value < 0) throw new IllegalArgumentException("value must be positive");
		if (direction == Direction.HORIZONTAL) _stepX = value; else _stepY = value;
	}
	public final void setStepSize(double value) { _stepX = _stepY = value; }
	
	
	public double getSliderValue(Direction direction) {
		double min = getMin(direction);
		double max = getMax(direction);
		double stepSize = getStepSize(direction);
		double value = min + getSliderRaw(direction) * (max - min);
		return (stepSize > 0) ? Math.round(value / stepSize) * stepSize : value;
	}
	public final double getSliderValueX() { return getSliderValue(Direction.HORIZONTAL); }
	public final double getSliderValueY() { return getSliderValue(Direction.VERTICAL); }
	public final double getSliderValue() { return getSliderValue(getOnlyDirection()); }
	
	public void setSliderValue(Direction direction, double value) {
		double min = getMin(direction);
		double max = getMax(direction);
		setSliderRaw(direction, (value - min) / (max - min));
	}
	public final void setSliderValue(double hor, double vert) {
		double hMin = getMin(Direction.HORIZONTAL);
		double hMax = getMax(Direction.HORIZONTAL);
		double vMin = getMin(Direction.VERTICAL);
		double vMax = getMax(Direction.VERTICAL);
		setSliderRaw((hor - hMin) / (hMax - hMin), (vert - vMin) / (vMax - vMin));
	}
	public final void setSliderValueX(double value) { setSliderValue(Direction.HORIZONTAL, value); }
	public final void setSliderValueY(double value) { setSliderValue(Direction.VERTICAL, value); }
	public final void setSliderValue(double value) { setSliderValue(getOnlyDirection(), value); }
	
	public void setValueFormatter(DoubleFunction<String> value) {
		if (value == null) throw new NullPointerException("Argument value can't be null");
		_valueFormatter = value;
	}
	public String getValueText() { return _valueFormatter.apply(getSliderValue()); }
	
	
	@Override
	public boolean canDrag() { return true; }
	
	@Override
	public void onPressed(int mouseX, int mouseY) {
		super.onPressed(mouseX, mouseY);
		onDragged(mouseX, mouseY, 0, 0, mouseX, mouseY);
	}
	
	@Override
	public void onDragged(int mouseX, int mouseY, int deltaX, int deltaY, int startX, int startY) {
		for (Direction direction : directions) {
			int pos = (direction == Direction.HORIZONTAL) ? mouseX : mouseY;
			setSliderRaw(direction, (pos - getSliderSize() / 2) / (float)(getSize(direction) - getSliderSize()));
		}
	}
	
	
	// Drawing
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		if (!isVisible()) return;
		// TODO: Shouldn't be highlighted if another element is being dragged.
		boolean isHighlighted = (isEnabled() && isDragged() || contains(mouseX, mouseY));
		drawSliderBackground(isHighlighted, partialTicks);
		drawSliderBar(isHighlighted, partialTicks);
		drawSliderForeground(isHighlighted, partialTicks);
	}
	
	/** Draws the slider background. */
	protected void drawSliderBackground(boolean isHighlighted, float partialTicks) {
		GuiUtils.drawContinuousTexturedBox(GuiButton.BUTTON_TEX, 0, 0, 0, 46, getWidth(), getHeight(),
		                                   DEFAULT_WIDTH, DEFAULT_HEIGHT, 2, 3, 2, 2, 0);
	}
	
	/** Draws the slider bar. */
	protected void drawSliderBar(boolean isHighlighted, float partialTicks) {
		int sliderSize = getSliderSize();
		boolean slideHorizontal = directions.contains(Direction.HORIZONTAL);
		boolean slideVertical   = directions.contains(Direction.VERTICAL);
		int x = slideHorizontal ? (int)((getWidth()  - sliderSize) * getSliderRawX()) : 0;
		int y = slideVertical   ? (int)((getHeight() - sliderSize) * getSliderRawY()) : 0;
		int w = slideHorizontal ? sliderSize : getWidth();
		int h = slideVertical   ? sliderSize : getHeight();
		int ty = isEnabled() ? 66 : 46;
		GuiUtils.drawContinuousTexturedBox(GuiButton.BUTTON_TEX, x, y, 0, ty, w, h,
		                                   DEFAULT_WIDTH, DEFAULT_HEIGHT, 2, 3, 2, 2, 0);
	}
	
	/** Draws the content on the slider, such as the value. */
	protected void drawSliderForeground(boolean isHighlighted, float partialTicks) {
		String text = getValueText();
		if (text.isEmpty()) return;
		FontRenderer fontRenderer = getFontRenderer();
		int textWidth = fontRenderer.getStringWidth(text);
		int textColor = !isEnabled()  ? COLOR_CONTROL_DISABLED
		              : isHighlighted ? COLOR_CONTROL_HIGHLIGHT
		                              : COLOR_CONTROL;
		fontRenderer.drawStringWithShadow(text,
			getWidth() / 2 - textWidth / 2,
			(getHeight() - 8) / 2, textColor);
	}
	
	
	// Utility methods
	
	/** Makes sure that the specified direction is valid for this slider. */
	private void ensureValidDirection(Direction direction) {
		if (!directions.contains(direction)) throw new UnsupportedOperationException(
			"Element doesn't slide in direction '" + direction + "'");
	}
		
	/** Returns the direction of this slider, throwing
	 *  an exception if this slider is two-dimensional. */
	private Direction getOnlyDirection() {
		if (directions.size() != 1) throw new UnsupportedOperationException(
			"Element slides in both directions");
		return directions.iterator().next();
	}
	
}
