package net.mcft.copy.backpacks.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiElementBase {
	
	private GuiContext _context;
	private GuiContainer _parent;
	private int _x, _y, _width, _height;
	private boolean _leftAlign = true, _topAlign = true;
	private boolean _rightAlign = false, _bottomAlign = false;
	
	
	void setContext(GuiContext context) { _context = context; }
	void setParent(GuiContainer parent) { _parent = parent; }
	
	public final GuiContext getContext() { return _context; }
	public final GuiContainer getParent() { return _parent; }
	
	
	// Position related
	
	public int getX() { return _x; }
	public int getY() { return _y; }
	
	/** Sets the control's X position relative to the parent control. */
	public void setX(int x) { setPosition(x, _y); }
	/** Sets the control's Y position relative to the parent control. */
	public void setY(int y) { setPosition(_x, y); }
	
	/** Sets the control's position relative to the parent control. */
	public void setPosition(int x, int y) { _x = x; _y = y; }
	
	public int getWidth() { return _width; }
	public int getHeight() { return _height; }
	
	public void setWidth(int width) { setSize(width, _height); }
	public void setHeight(int height) { setSize(_width, height); }
	
	public void setSize(int width, int height) {
		if ((width == _width) && (height == _height)) return;
		int prevWidth  = _width;
		int prevHeight = _height;
		if (_rightAlign) _x += (_width - width) / (_leftAlign ? 2 : 1);
		else if (!_leftAlign) _x = (getParentWidth() - width) / 2;
		if (_bottomAlign) _y += (_height - height) / (_topAlign ? 2 : 1);
		else if (!_topAlign) _y = (getParentHeight() - height) / 2;
		_width  = width;
		_height = height;
		onResized(prevWidth, prevHeight);
	}
	
	// Alignment related
	
	/** Returns the width of this control's parent, or 0 if none. */
	public int getParentWidth() { return (_parent != null) ? _parent.getWidth() : 0; }
	/** Returns the height of this control's parent, or 0 if none. */
	public int getParentHeight() { return (_parent != null) ? _parent.getHeight() : 0; }
	
	public int getLeft() { return _x; }
	public int getRight() { return getParentWidth() - _width - _x; }
	
	public int getTop() { return _y; }
	public int getBottom() { return getParentHeight() - _height - _y; }
	
	public void setLeftAligned(int left) { setLeftAligned(left, _width); }
	public void setLeftAligned(int left, int width) {
		_leftAlign = true; _rightAlign = false;
		setXAndWidthInternal(left, width);
	}
	public void setRightAligned(int right) { setRightAligned(right, _width); }
	public void setRightAligned(int right, int width) {
		_leftAlign = false; _rightAlign = true;
		setXAndWidthInternal(getParentWidth() - width - right, width);
	}
	public void setLeftRightAligned(int left, int right) {
		_leftAlign = true; _rightAlign = true;
		setXAndWidthInternal(left, getParentWidth() - left - right);
	}
	public void setHorizontalCentered() { setHorizontalCentered(_width); }
	public void setHorizontalCentered(int width) {
		_leftAlign = false; _rightAlign = false;
		setXAndWidthInternal((getParentWidth() - width) / 2, width);
	}
	
	public void setTopAligned(int top) { setTopAligned(top, _height); }
	public void setTopAligned(int top, int height) {
		_topAlign = true; _bottomAlign = false;
		setYAndHeightInternal(top, height);
	}
	public void setBottomAligned(int bottom) { setBottomAligned(bottom, _height); }
	public void setBottomAligned(int bottom, int height) {
		_topAlign = false; _bottomAlign = true;
		setYAndHeightInternal(getParentHeight() - height - bottom, height);
	}
	public void setTopBottomAligned(int top, int bottom) {
		_topAlign = true; _bottomAlign = true;
		setYAndHeightInternal(top, getParentHeight() - top - bottom);
	}
	public void setVerticalCentered() { setVerticalCentered(_height); }
	public void setVerticalCentered(int height) {
		_topAlign = false; _bottomAlign = false;
		setYAndHeightInternal((getParentHeight() - height) / 2, height);
	}
	
	private void setXAndWidthInternal(int x, int width) {
		_x = x;
		if (width == _width) return;
		int prevWidth = _width;
		_width = width;
		onResized(prevWidth, _height);
	}
	private void setYAndHeightInternal(int y, int height) {
		_y = y;
		if (height == _height) return;
		int prevHeight = _height;
		_height = height;
		onResized(_width, prevHeight);
	}
	
	// Control focus
	
	/** Returns if this control can be focused. */
	public boolean canFocus() { return false; }
	
	/** Returns if this control is currently focused. */
	public boolean isFocused() { return (getContext().getFocused() == this); }
	
	/** Makes this control the currently focused control. */
	public void setFocused() { setFocused(true); }
	
	/** Sets whether this control is the currently focused control. */
	public void setFocused(boolean value) {
		if (value && !canFocus())
			throw new UnsupportedOperationException("This control can't be focused");
		getContext().setFocused(value ? this : null);
	}
	
	// Drag related
	
	/** Returns if this control can be dragged. */
	public boolean canDrag() { return false; }
	
	/** Returns if this control is currently being pressed down. */
	public boolean isPressed() { return (getContext().getPressed() == this); }
	
	/** Returns if this control is currently being dragged. */
	public boolean isDragged() { return (canDrag() && isPressed()); }
	
	// Basic events
	
	/** Called when this control is added to a container. */
	public void onControlAdded() {  }
	
	/** Called when this control is resized. */
	public void onResized(int prevWidth, int prevHeight) {  }
	
	/** Called when the parent control is resized. */
	public void onParentResized(int prevWidth, int prevHeight) {
		int ownPrevWidth  = _width;
		int ownPrevHeight = _height;
		
		if (_rightAlign) {
			int widthDelta = getParentWidth() - prevWidth;
			if (_leftAlign) _width += widthDelta;
			else _x += widthDelta;
		} else if (!_leftAlign)
			_x = (getParentWidth() - _width) / 2;
		
		if (_bottomAlign) {
			int heightDelta = getParentHeight() - prevHeight;
			if (_topAlign) _height += heightDelta;
			else _y += heightDelta;
		} else if (!_topAlign)
			_y = (getParentHeight() - _height) / 2;
		
		if ((_width != ownPrevWidth) || (_height != ownPrevHeight))
			onResized(ownPrevWidth, ownPrevHeight);
	}
	
	// Mouse events
	
	/** Called when the mouse is clicked in this control.
	 *  Mouse position is relative to the control's position.
	 *  Returns if the mouse action was handled. */
	public boolean onMouseDown(int mouseButton, int mouseX, int mouseY) {
		if (mouseButton == MouseButton.LEFT)
			getContext().setPressed(this);
		if (canFocus()) {
			setFocused();
			return true;
		} else return false;
	}
	
	/** Called when the mouse is moved over this control or being dragged.
	 *  Mouse position is relative to the control's position. */
	public void onMouseMove(int mouseX, int mouseY) {  }
	
	public void onMouseUp(int mouseButton, int mouseX, int mouseY) {  }
	
	// Keyboard events
	
	
	
	// Draw events
	
	/** Draws this control on the screen. Rendered relative to its position.
	 *  Mouse position is relative to the control's position. */
	public void draw(int mouseX, int mouseY, float partialTicks) {  }
	
	/** Draws this control's tooltip on the screen.
	 *  Tooltip is rendered relative to this control's position.
	 *  Mouse position is relative to the control's position. */
	public void drawTooltip(int mouseX, int mouseY, float partialTicks) {  }
	
	
	// Utility methods
	
	/** Returns whether the specified region (x, y, width, height)
	 *  contains the specified point (testX, testY). */
	public static boolean regionContains(int x, int y, int width, int height,
	                                     int testX, int testY) {
		return (testX >= x) && (testX < x + width) &&
		       (testY >= y) && (testY < y + height);
	}
	/** Returns whether the specified relative
	 *  position is within the control's region. */
	public boolean controlContains(int x, int y)
		{ return regionContains(0, 0, _width, _height, x, y); }
	
	public static Minecraft getMC() { return Minecraft.getMinecraft(); }
	public static FontRenderer getFontRenderer() { return getMC().fontRenderer; }
	
	// Utility classes
	
	public static final class MouseButton {
		
		private MouseButton() {  }
		
		public static int LEFT = 0;
		public static int RIGHT = 1;
		public static int MIDDLE = 2;
		
	}
	
}
