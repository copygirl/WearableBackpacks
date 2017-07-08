package net.mcft.copy.backpacks.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLayout extends GuiContainer {
	
	public final Direction direction;
	
	private int _spacing = 2;
	private boolean _fixedSize = false;
	
	public GuiLayout(Direction direction)
		{ this.direction = direction; }
	
	public int getSpacing() { return _spacing; }
	public void setSpacing(int value) { _spacing = value; }
	
	// Adding / removing elements
	
	public void addFixed(GuiElementBase control, int size)
		{ control.setSize(direction, size); addFixed(control); }
	public void addFixed(GuiElementBase control)
		{ addInternal(control, new LayoutAlignment.Fixed()); }
	
	@Override
	public void add(GuiElementBase control)
		{ addWeighted(control); }
	public void addWeighted(GuiElementBase control)
		{ addWeighted(control, 1.0); }
	public void addWeighted(GuiElementBase control, double weight)
		{ addWeighted(control, weight, 0); }
	public void addWeighted(GuiElementBase control, double weight, int minSize)
		{ addInternal(control, new LayoutAlignment.Weighted(weight, minSize)); }
	
	private void addInternal(GuiElementBase control, LayoutAlignment alignment) {
		control.setAlign(direction, alignment);
		super.add(control);
	}
	
	
	@Override
	public void onChildAdded(GuiElementBase control) {
		// If this was the first child being added, make layout
		// fixed sized dependant on wether a size was already set.
		if (children.size() == 1) _fixedSize =
			(getSize(direction.perpendicular()) > 0) ||
			(getAlign(direction.perpendicular()) instanceof Alignment.Both);
		
		super.onChildAdded(control);
		updateOwnSize();
	}
	
	@Override
	public void onChildSizeChanged(GuiElementBase control, Direction direction) {
		super.onChildSizeChanged(control, direction);
		if (direction == this.direction) updateChildSizes(direction);
		else updateOwnSize();
	}
	
	@Override
	public void onChildAlignChanged(GuiElementBase control, Direction direction) {
		if ((direction == this.direction) && !(getAlign(direction) instanceof LayoutAlignment))
			throw new UnsupportedOperationException(
				"Unsupported Alignment '" + getAlign(direction).getClass() + "' in GuiLayout");
	}
	
	@Override
	public int getChildPos(GuiElementBase control, Direction direction) {
		return (direction == this.direction)
			? ((LayoutAlignment)control.getAlign(direction))._childPos
			: super.getChildPos(control, direction);
	}
	
	/** Updates this layout control's size (perpendicular to its direction). */
	private void updateOwnSize() {
		Direction direction = this.direction.perpendicular();
		if (_fixedSize || (getAlign(direction) instanceof LayoutAlignment.Weighted)) return;
		setSize(direction, children.stream()
			.filter(child -> !(child.getAlign(direction) instanceof Alignment.Both))
			.mapToInt(child -> child.getSize(direction))
			.max().orElse(0));
	}
	
	/** Updates the position and size of this control's child elements. */
	@Override
	protected void updateChildSizes(Direction direction) {
		if (direction != this.direction) {
			super.updateChildSizes(direction);
			return;
		}
		double remainingWeight = 0.0;
		int availableSize = getSize(direction);
		availableSize -= (children.size() - 1) * _spacing;
		for (GuiElementBase child : children) {
			Alignment align = child.getAlign(direction);
			if (align instanceof LayoutAlignment.Weighted) {
				LayoutAlignment.Weighted weighted = (LayoutAlignment.Weighted)align;
				remainingWeight += weighted.weight;
				availableSize   -= weighted.minSize;
			} else availableSize -= child.getSize(direction);
		}
		int currentPos = 0;
		for (GuiElementBase child : children) {
			LayoutAlignment align = (LayoutAlignment)child.getAlign(direction);
			int size = 0;
			if (align instanceof LayoutAlignment.Weighted) {
				LayoutAlignment.Weighted weighted = (LayoutAlignment.Weighted)align;
				size = Math.max(0, (int)(availableSize * weighted.weight / remainingWeight));
				remainingWeight -= weighted.weight;
				availableSize   -= size;
				size += weighted.minSize;
				child.setSize(direction, size);
			} else size = child.getSize(direction);
			align._childPos = currentPos;
			currentPos += size + _spacing;
		}
		//if (availableSize != 0)
		//	direction.setSize(this, direction.getSize(this) - availableSize);
	}
	
	
	public static abstract class LayoutAlignment extends Alignment {
		
		private int _childPos;
		private LayoutAlignment() {  }
		
		public static final class Fixed extends LayoutAlignment {  }
		
		public static final class Weighted extends LayoutAlignment {
			public final double weight;
			public final int minSize;
			public Weighted(double weight, int minSize)
				{ this.weight = weight; this.minSize = minSize; }
		}
		
	}
	
}
