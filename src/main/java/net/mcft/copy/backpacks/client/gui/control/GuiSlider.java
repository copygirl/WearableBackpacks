package net.mcft.copy.backpacks.client.gui.control;

import java.util.EnumSet;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.config.GuiUtils;

import net.mcft.copy.backpacks.client.gui.Direction;

public class GuiSlider extends GuiButton {
	
	public final EnumSet<Direction> directions;
	
	private float _amountX = 0.5f, _amountY = 0.5f;
	private int _sliderSize = 8;
	
	
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
		super(x, y, width, height, "");
		if (directions.isEmpty()) throw new IllegalArgumentException(
			"Argument 'directions' must be at least one Direction");
		this.directions = directions;
	}
	
	public int getSliderSize() { return _sliderSize; }
	public void setSliderSize(int value) { _sliderSize = value; }
	
	
	@Override
	public boolean canDrag() { return true; }
	
	@Override
	public void onPressed(int mouseX, int mouseY) {
		super.onPressed(mouseX, mouseY);
		onDragged(mouseX, mouseY, mouseX, mouseY);
	}
	
	@Override
	public void onDragged(int mouseX, int mouseY, int startX, int startY) {
		for (Direction direction : directions) {
			int pos = (direction == Direction.HORIZONTAL) ? mouseX : mouseY;
			setAmount(direction, (pos - getSliderSize() / 2) / (float)(getSize(direction) - getSliderSize()));
		}
	}
	
	
	public float getAmount(Direction direction) {
		if (!directions.contains(direction)) throw new UnsupportedOperationException(
			"Element doesn't slide in direction '" + direction + "'");
		return (direction == Direction.HORIZONTAL) ? _amountX : _amountY;
	}
	public final float getAmountX() { return getAmount(Direction.HORIZONTAL); }
	public final float getAmountY() { return getAmount(Direction.VERTICAL); }
	public final float getAmount() {
		if (directions.size() != 1) throw new UnsupportedOperationException(
			"Element slides in both directions");
		return getAmount(directions.iterator().next());
	}
	
	public void setAmount(Direction direction, float value) {
		if (!directions.contains(direction)) throw new UnsupportedOperationException(
			"Element doesn't slide in direction '" + direction + "'");
		value = MathHelper.clamp(value, 0, 1);
		if (direction == Direction.HORIZONTAL) {
			if (value == _amountX) return;
			_amountX = value;
		} else {
			if (value == _amountY) return;
			_amountY = value;
		}
		onAmountChanged(direction);
	}
	public final void setAmountX(float value) { setAmount(Direction.HORIZONTAL, value); }
	public final void setAmountY(float value) { setAmount(Direction.VERTICAL, value); }
	public final void setAmount(float value) {
		if (directions.size() != 1) throw new UnsupportedOperationException(
			"Element slides in both directions");
		setAmount(directions.iterator().next(), value);
	}
	
	protected void onAmountChanged(Direction direction) {  }
	
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		if (!isVisible()) return;
		
		int sliderSize = getSliderSize();
		boolean slideHorizontal = directions.contains(Direction.HORIZONTAL);
		boolean slideVertical   = directions.contains(Direction.VERTICAL);
		int x = slideHorizontal ? (int)((getWidth()  - sliderSize) * getAmountX()) : 0;
		int y = slideVertical   ? (int)((getHeight() - sliderSize) * getAmountY()) : 0;
		int w = slideHorizontal ? sliderSize : getWidth();
		int h = slideVertical   ? sliderSize : getHeight();
		
		GuiUtils.drawContinuousTexturedBox(BUTTON_TEX, 0, 0, 0, 46, getWidth(), getHeight(),
		                                   DEFAULT_WIDTH, DEFAULT_HEIGHT, 2, 3, 2, 2, 0);
		GuiUtils.drawContinuousTexturedBox(BUTTON_TEX, x, y, 0, 66, w, h,
		                                   DEFAULT_WIDTH, DEFAULT_HEIGHT, 2, 3, 2, 2, 0);
		
		boolean isHighlighted = (isEnabled() && isDragged() || contains(mouseX, mouseY));
		drawWhateverIsOnTheButton(mouseX, mouseY, isHighlighted, partialTicks);
	}
	
}
