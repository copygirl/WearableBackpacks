package net.mcft.copy.backpacks.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLayout extends GuiContainer {
	
	public final Direction direction;
	
	private int _spacing = 2;
	
	public GuiLayout(Direction direction)
		{ this.direction = direction; }
	
	public int getSpacing() { return _spacing; }
	public void setSpacing(int value) { _spacing = value; }
	
	
	// Adding / removing elements
	
	public void addFixed(GuiElementBase element, int size)
		{ element.setSize(direction, size); addFixed(element); }
	public void addFixed(GuiElementBase element)
		{ addInternal(element, new LayoutAlignment.Fixed()); }
	
	@Override
	public void add(GuiElementBase element)
		{ addWeighted(element); }
	public void addWeighted(GuiElementBase element)
		{ addWeighted(element, 1.0); }
	public void addWeighted(GuiElementBase element, double weight)
		{ addWeighted(element, weight, 0); }
	public void addWeighted(GuiElementBase element, double weight, int minSize)
		{ addInternal(element, new LayoutAlignment.Weighted(weight, minSize)); }
	
	private void addInternal(GuiElementBase element, LayoutAlignment alignment) {
		element.setAlign(direction, alignment);
		super.add(element);
	}
	
	
	@Override
	public void onChildSizeChanged(GuiElementBase element, Direction direction) {
		super.onChildSizeChanged(element, direction);
		if (direction == this.direction) updateChildSizes(direction);
	}
	
	@Override
	public void onChildAlignChanged(GuiElementBase element, Direction direction) {
		if ((direction == this.direction) && !(getAlign(direction) instanceof LayoutAlignment))
			throw new UnsupportedOperationException(
				"Unsupported Alignment '" + getAlign(direction).getClass() + "' in GuiLayout");
		super.onChildAlignChanged(element, direction);
	}
	
	@Override
	public int getChildPos(GuiElementBase element, Direction direction) {
		return (direction == this.direction)
			? getPaddingMin(direction) + ((LayoutAlignment)element.getAlign(direction))._childPos
			: super.getChildPos(element, direction);
	}
	
	
	@Override
	protected void updateChildSizes(Direction direction) {
		if (children.isEmpty()) return;
		if (direction != this.direction)
			{ super.updateChildSizes(direction); return; }
		
		double remainingWeight = 0.0;
		int availableSize = getSize(direction)
			- getPadding(direction)
			- (children.size() - 1) * getSpacing();
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
			currentPos += size + getSpacing();
		}
		if (doesExpand(direction)) setSize(direction, currentPos + getPadding(direction) - getSpacing());
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
			@Override public boolean canExpand() { return false; }
		}
		
	}
	
}
