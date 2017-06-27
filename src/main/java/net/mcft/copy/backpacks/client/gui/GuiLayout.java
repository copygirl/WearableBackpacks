package net.mcft.copy.backpacks.client.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntSupplier;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLayout extends GuiContainer {
	
	public final LayoutDirection direction;
	
	protected Map<GuiElementBase, ElementData> elementData =
		new HashMap<GuiElementBase, ElementData>();
	
	private int _spacing = 2;
	private double _totalWeight = 0.0;
	
	
	public GuiLayout(LayoutDirection direction) {
		this.direction = direction;
	}
	
	
	public void add(GuiElementBase control, ElementData data) {
		super.add(control);
		elementData.put(control, data);
		_totalWeight += data.weight;
	}
	
	@Override
	public void add(GuiElementBase control)
		{ add(control, ElementData.weighted(1.0)); }
	
	@Override
	public void remove(GuiElementBase control) {
		super.remove(control);
		_totalWeight -= elementData.get(control).weight;
		elementData.remove(control);
	}
	
	
	@Override
	public void onResized(int prevWidth, int prevHeight) {
		super.onResized(prevWidth, prevHeight);
		switch (direction) {
			case HORIZONAL: handleLayoutDirection(() -> getWidth(),
				(child, pos, size) -> { child.setX(pos); child.setWidth(size); }); break;
			case VERICAL: handleLayoutDirection(() -> getHeight(),
				(child, pos, size) -> { child.setY(pos); child.setHeight(size); }); break;
		}
	}
	private void handleLayoutDirection(IntSupplier getSize, ChildSetPosAndSizeConsumer setPosAndSize) {
		int availableSize = getSize.getAsInt();
		availableSize -= (children.size() - 1) * _spacing;
		availableSize -= children.stream()
			.map(child -> elementData.get(child))
			.mapToInt(data -> data.minSize).sum();
		int currentPos = 0;
		double remainingWeight = _totalWeight;
		for (GuiElementBase child : children) {
			ElementData data = elementData.get(child);
			int size = data.minSize;
			if (data.type == LayoutType.WEIGHTED) {
				size += (int)(availableSize * data.weight / remainingWeight);
				availableSize -= size;
				remainingWeight -= data.weight;
			}
			setPosAndSize.apply(child, currentPos, size);
			currentPos += size + _spacing;
		}
	}
	
	private interface ChildSetPosAndSizeConsumer {
		public void apply(GuiElementBase child, int pos, int size);
	}
	
	
	public static final class ElementData {
		
		public final LayoutType type;
		public final int minSize, maxSize;
		public final double weight;
		
		private ElementData(LayoutType type, int minSize, int maxSize, double weight)
			{ this.type = type; this.minSize = minSize; this.maxSize = maxSize; this.weight = weight; }
		
		public static ElementData weighted(double weight)
			{ return weighted(weight, 0, Integer.MAX_VALUE); }
		public static ElementData weighted(double weight, int minSize)
			{ return weighted(weight, minSize, Integer.MAX_VALUE); }
		public static ElementData weighted(double weight, int minSize, int maxSize)
			{ return new ElementData(LayoutType.WEIGHTED, minSize, maxSize, weight); }
		
		public static ElementData fixed(int size)
			{ return fixed(size, size); }
		public static ElementData fixed(int size, int minSize)
			{ return new ElementData(LayoutType.FIXED, minSize, size, 0.0); }
		
	}
	
	public enum LayoutType {
		FIXED,
		WEIGHTED
	}
	
	public enum LayoutDirection {
		HORIZONAL,
		VERICAL
	}
	
}
