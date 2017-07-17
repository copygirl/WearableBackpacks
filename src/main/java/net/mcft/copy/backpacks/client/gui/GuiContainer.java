package net.mcft.copy.backpacks.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.GlStateManager;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiContainer extends GuiElementBase {
	
	protected List<GuiElementBase> children = new ArrayList<GuiElementBase>();
	
	private boolean _expandHorizontal = true, _expandVertical = true;
	private int _padLeft, _padTop, _padRight, _padBottom;
	
	
	public GuiContainer() {  }
	public GuiContainer(GuiContext context) { setContext(context); }
	
	// Expand related
	
	/** Returns whether this container will expand its size for its child elements. */
	public boolean doesExpand(Direction direction) {
		return getAlign(direction).canExpand() &&
			((direction == Direction.HORIZONTAL) ? _expandHorizontal : _expandVertical);
	}
	
	public void setExpand(Direction direction, boolean value) {
		if (direction == Direction.HORIZONTAL) _expandHorizontal = value;
		else _expandVertical = value;
	}
	
	// Padding related
	
	public int getPaddingMin(Direction direction)
		{ return (direction == Direction.HORIZONTAL) ? _padLeft : _padTop; }
	public int getPaddingMax(Direction direction)
		{ return (direction == Direction.HORIZONTAL) ? _padRight : _padBottom; }
	public int getPadding(Direction direction)
		{ return getPaddingMin(direction) + getPaddingMax(direction); }
	
	public final int getPaddingLeft() { return getPaddingMin(Direction.HORIZONTAL); }
	public final int getPaddingTop() { return getPaddingMin(Direction.VERTICAL); }
	public final int getPaddingRight() { return getPaddingMax(Direction.HORIZONTAL); }
	public final int getPaddingBottom() { return getPaddingMax(Direction.VERTICAL); }
	
	public void setPadding(Direction direction, int min, int max) {
		if (direction == Direction.HORIZONTAL) { _padLeft = min; _padRight = max; }
		else { _padTop = min; _padBottom = max; }
		expandToFitChildren(direction);
	}
	
	public final void setPaddingHorizontal(int left, int right) { setPadding(Direction.HORIZONTAL, left, right); }
	public final void setPaddingHorizontal(int value) { setPaddingHorizontal(value, value); }
	public final void setPaddingVertical(int top, int bottom) { setPadding(Direction.VERTICAL, top, bottom); }
	public final void setPaddingVertical(int value) { setPaddingVertical(value, value); }
	
	public final void setPadding(int left, int top, int right, int bottom)
		{ setPaddingHorizontal(left, right); setPaddingVertical(top, bottom); }
	public final void setPadding(int horizontal, int vertical)
		{ setPadding(horizontal, vertical, horizontal, vertical); }
	public final void setPadding(int value) { setPadding(value, value); }
	
	public final void setFillHorizontal(int padding) { setFillHorizontal(); setPaddingHorizontal(padding); }
	public final void setFillVertical(int padding) { setFillVertical(); setPaddingVertical(padding); }
	public final void setFill(int padding) { setFillHorizontal(padding); setFillVertical(padding); }
	
	
	/** Called when a child element is resized. */
	public void onChildSizeChanged(GuiElementBase element, Direction direction)
		{ expandToFitChildren(direction); }
	
	/** Called when a child element's alignment changes. */
	public void onChildAlignChanged(GuiElementBase element, Direction direction)
		{ expandToFitChildren(direction); }
	
	
	public int getChildPos(GuiElementBase element, Direction direction) {
		Alignment align = element.getAlign(direction);
		if (align instanceof Alignment.Min)
			return getPaddingMin(direction) + ((Alignment.Min)align).min;
		else if (align instanceof Alignment.Max)
			return getSize(direction) - element.getSize(direction) - getPaddingMax(direction) - ((Alignment.Max)align).max;
		else if (align instanceof Alignment.Both)
			return getPaddingMin(direction) + ((Alignment.Both)align).min;
		else if (align instanceof Alignment.Center)
			return (getSize(direction) - element.getSize(direction)) / 2;
		else throw new UnsupportedOperationException("Unsupported Alignment '" + align.getClass() + "'");
	}
	public final int getChildX(GuiElementBase element)
		{ return getChildPos(element, Direction.HORIZONTAL); }
	public final int getChildY(GuiElementBase element)
		{ return getChildPos(element, Direction.VERTICAL); }
	
	
	/** Called when a child element is added. */
	public void onChildAdded(GuiElementBase element) {
		for (Direction direction : Direction.values()) {
			updateChildSizes(direction);
			expandToFitChildren(direction);
		}
	}
	
	/** Called when a child element is added. */
	public void onChildRemoved(GuiElementBase element) {
		for (Direction direction : Direction.values()) {
			updateChildSizes(direction);
			expandToFitChildren(direction);
		}
	}
	
	
	/** Adds the specified element to this container. */
	public void add(GuiElementBase element) {
		if (element.getContext() != null)
			throw new UnsupportedOperationException("The specified element already has a context set");
		if (getContext() != null)
			element.setContext(getContext());
		element.setParent(this);
		children.add(element);
		onChildAdded(element);
	}
	/** Adds all of the specified elements to this container. */
	public void addAll(GuiElementBase... elements)
		{ for (GuiElementBase element : elements) add(element); }
	
	/** Removes the specified element from this container. */
	public void remove(GuiElementBase element) {
		if (!children.remove(element))
			throw new UnsupportedOperationException("The specified element is not a child of this container");
		element.setContext(null);
		element.setParent(null);
		onChildRemoved(element);
	}
	
	
	@Override
	void setContext(GuiContext element) {
		super.setContext(element);
		children.forEach(child -> child.setContext(element));
	}
	
	@Override
	public void onSizeChanged(Direction direction) {
		if (children.isEmpty()) setExpand(direction, false);
		updateChildSizes(direction);
		expandToFitChildren(direction);
	}
	
	protected void updateChildSizes(Direction direction) {
		for (GuiElementBase child : children) {
			Alignment align = child.getAlign(direction);
			if (align instanceof Alignment.Both) {
				Alignment.Both both = (Alignment.Both)align;
				child.setSize(direction, getSize(direction) - getPadding(direction) - both.min - both.max);
			}
		}
	}
	
	protected void expandToFitChildren(Direction direction) {
		if (children.isEmpty() || !doesExpand(direction)) return;
		setSize(direction, getPadding(direction) + children.stream()
			.mapToInt(child -> {
				Alignment align = child.getAlign(direction);
				return (align instanceof Alignment.Both) ? 0
					: (align instanceof Alignment.Center) ? child.getSize(direction)
					: getChildPos(child, direction) - getPaddingMin(direction) + child.getSize(direction);
			}).max().orElse(0));
	}
	
	
	@Override
	public boolean onMouseDown(int mouseButton, int mouseX, int mouseY) {
		return (isVisible() && isEnabled() &&
		        (foreachFindChildMousePos(mouseX, mouseY, (child, x, y, mx, my) ->
		             (child.contains(mx, my) && child.onMouseDown(mouseButton, mx, my))) != null) ||
		         super.onMouseDown(mouseButton, mouseX, mouseY));
	}
	
	@Override
	public void onMouseMove(int mouseX, int mouseY) {
		if (!isVisible()) return;
		foreachChildMousePos(mouseX, mouseY, (child, x, y, mx, my) ->
			{ if (child.contains(mx, my)) child.onMouseMove(mx, my); });
		super.onMouseMove(mouseX, mouseY);
	}
	
	@Override
	public boolean onMouseScroll(int scroll, int mouseX, int mouseY) {
		return (isVisible() && isEnabled() &&
		        (foreachFindChildMousePos(mouseX, mouseY, (child, x, y, mx, my) ->
		             (child.contains(mx, my) && child.onMouseScroll(scroll, mx, my))) != null) ||
		         super.onMouseScroll(scroll, mouseX, mouseY));
	}
	
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		if (!isVisible()) return;
		foreachChildMousePos(mouseX, mouseY, (child, x, y, mx, my) -> {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, 0);
			child.draw(mx, my, partialTicks);
			GlStateManager.popMatrix();
		});
	}
	
	// Utility methods
	
	protected void foreachChildPos(ChildPosConsumer consumer) {
		for (GuiElementBase child : children)
			consumer.apply(child, getChildX(child), getChildY(child));
	}
	
	protected void foreachChildMousePos(int mouseX, int mouseY, ChildPosMouseConsumer consumer) {
		for (GuiElementBase child : children) {
			int x = getChildX(child);
			int y = getChildY(child);
			int mx = mouseX - x;
			int my = mouseY - y;
			consumer.apply(child, x, y, mx, my);
		}
	}
	
	protected GuiElementBase foreachFindChildMousePos(
		int mouseX, int mouseY, ChildPosMousePredicate consumer) {
		for (GuiElementBase child : children) {
			int x = getChildX(child);
			int y = getChildY(child);
			int mx = mouseX - x;
			int my = mouseY - y;
			if (consumer.apply(child, x, y, mx, my)) return child;
		}
		return null;
	}
	
	@FunctionalInterface
	protected interface ChildPosConsumer
		{ void apply(GuiElementBase element, int childX, int childY); }
	
	@FunctionalInterface
	protected interface ChildPosMouseConsumer
		{ void apply(GuiElementBase element, int childX, int childY, int mouseX, int mouseY); }
	@FunctionalInterface
	protected interface ChildPosMousePredicate
		{ boolean apply(GuiElementBase element, int childX, int childY, int mouseX, int mouseY); }
	
}
