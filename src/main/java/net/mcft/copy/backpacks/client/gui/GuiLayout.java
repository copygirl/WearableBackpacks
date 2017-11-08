package net.mcft.copy.backpacks.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLayout extends GuiContainer {
	
	public final Direction direction;
	
	private int[] _spacing = { 2 };
	
	public GuiLayout(Direction direction)
		{ this.direction = direction; }
	
	public int[] getSpacing() { return _spacing; }
	public void setSpacing(int... value) { _spacing = value; }
	
	
	// Getting / adding / inserting elements
	
	/** Gets the element at the specified index in this layout container. */
	public GuiElementBase get(int index)
		{ return children.get(index); }
	
	@Override
	public void add(GuiElementBase element) { addWeighted(element); }
	
	public void addFixed(GuiElementBase element, int size)
		{ insertFixed(children.size(), element, size); }
	public void addFixed(GuiElementBase element)
		{ insertFixed(children.size(), element); }
	
	public void addWeighted(GuiElementBase element)
		{ insertWeighted(children.size(), element); }
	public void addWeighted(GuiElementBase element, double weight)
		{ insertWeighted(children.size(), element, weight); }
	public void addWeighted(GuiElementBase element, double weight, int minSize)
		{ insertWeighted(children.size(), element, weight, minSize); }
	
	public void insertFixed(int index, GuiElementBase element, int size)
		{ element.setSize(direction, size); insertFixed(index, element); }
	public void insertFixed(int index, GuiElementBase element)
		{ insert(index, element, new LayoutAlignment.Fixed()); }
	
	public void insertWeighted(int index, GuiElementBase element)
		{ insertWeighted(index, element, 1.0); }
	public void insertWeighted(int index, GuiElementBase element, double weight)
		{ insertWeighted(index, element, weight, 0); }
	public void insertWeighted(int index, GuiElementBase element, double weight, int minSize)
		{ insert(index, element, new LayoutAlignment.Weighted(weight, minSize)); }
	
	protected void insert(int index, GuiElementBase element, LayoutAlignment alignment) {
		if (element.getContext() != null)
			throw new UnsupportedOperationException("The specified element already has a context set");
		element.setAlign(direction, alignment);
		super.insert(index, element);
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
		if (direction != this.direction)
			{ super.updateChildSizes(direction); return; }
		
		int[] spacing = getSpacing();
		double remainingWeight = 0.0;
		int availableSize = getSize(direction) - getPadding(direction);
		for (int i = 0; i < children.size(); i++) {
			GuiElementBase child = children.get(i);
			
			Alignment align = child.getAlign(direction);
			if (align instanceof LayoutAlignment.Weighted) {
				LayoutAlignment.Weighted weighted = (LayoutAlignment.Weighted)align;
				remainingWeight += weighted.weight;
				availableSize   -= weighted.minSize;
			} else availableSize -= child.getSize(direction);
			
			if (i < children.size() - 1)
				availableSize -= spacing[Math.min(spacing.length - 1, i)];
		}
		
		int currentPos = 0;
		for (int i = 0; i < children.size(); i++) {
			GuiElementBase child = children.get(i);
			
			int size = 0;
			LayoutAlignment align = (LayoutAlignment)child.getAlign(direction);
			if (align instanceof LayoutAlignment.Weighted) {
				LayoutAlignment.Weighted weighted = (LayoutAlignment.Weighted)align;
				size = Math.max(0, (int)(availableSize * weighted.weight / remainingWeight));
				remainingWeight -= weighted.weight;
				availableSize   -= size;
				size += weighted.minSize;
				child.setSize(direction, size);
			} else size = child.getSize(direction);
			align._childPos = currentPos;
			currentPos += size;
			
			if (i < children.size() - 1)
				currentPos += spacing[Math.min(spacing.length - 1, i)];
		}
		
		if (doesExpand(direction))
			setSize(direction, currentPos + getPadding(direction));
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
