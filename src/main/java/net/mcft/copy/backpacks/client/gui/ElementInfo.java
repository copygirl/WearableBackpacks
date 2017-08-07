package net.mcft.copy.backpacks.client.gui;

import java.util.LinkedList;
import java.util.stream.Stream;

// TODO: Use this 
public final class ElementInfo {
	
	public final GuiElementBase element;
	public final int globalX, globalY;
	public final int relX, relY;
	public final int width, height;
	
	public ElementInfo(GuiElementBase element)
		{ this(element, 0, 0, 0, 0); }
	public ElementInfo(GuiElementBase element, int globalX, int globalY, int relX, int relY) {
		this.element = element;
		this.globalX = globalX; this.globalY = globalY;
		this.relX    = relX;    this.relY    = relY;
		this.width   = element.getWidth();
		this.height  = element.getHeight();
	}
	
	public ElementInfo getChild(GuiElementBase child) {
		if (!(element instanceof GuiContainer))
			throw new UnsupportedOperationException("This element is not a GuiContainer");
		if (child.getParent() != element)
			throw new IllegalArgumentException("This element is not the parent of the specified child");
		GuiContainer container = (GuiContainer)element;
		int childX = container.getChildX(child);
		int childY = container.getChildY(child);
		return new ElementInfo(child, globalX + childX, globalY + childY, childX, childY);
	}
	
	
	/** Returns a stream of elements at this position, going from parent to child. */
	public Stream<ElementInfo> getElementsAt(int x, int y) {
		Stream<ElementInfo> stream = Stream.of(this);
		if (element instanceof GuiContainer)
			stream = Stream.concat(stream, getChildElements()
				.filter(child -> child.contains(x, y))
				.findAny().map(child -> child.getElementsAt(x, y))
				.orElse(Stream.empty()));
		return stream;
	}
	
	/** Builds a hierarchy of elements, going from child to parent. */
	public static LinkedList<ElementInfo> getElementHierarchy(GuiElementBase element)
		{ return getElementHierarchy(element, new LinkedList<ElementInfo>()); }
	private static LinkedList<ElementInfo> getElementHierarchy(GuiElementBase element, LinkedList<ElementInfo> list) {
		list.addFirst((element.getParent() != null)
			? getElementHierarchy(element.getParent(), list).getFirst().getChild(element)
			: new ElementInfo(element));
		return list;
	}
	
	public Stream<ElementInfo> getChildElements() {
		if (element instanceof GuiContainer) {
			GuiContainer container = (GuiContainer)element;
			return container.children.stream()
				.map(child -> getChild(child));
		} else return Stream.empty();
	}
	
	
	public boolean contains(int x, int y)
		{ return GuiElementBase.regionContains(globalX, globalY, width, height, x, y); }
	
	@Override
	public String toString() {
		Class<?> elementClass = element.getClass();
		
		String name = elementClass.getSimpleName();
		if (name.isEmpty()) name = elementClass.getSuperclass().getSimpleName();
		
		Class<?> enclosingClass = elementClass.getEnclosingClass();
		if ((enclosingClass != null) && !enclosingClass.getSimpleName().isEmpty())
			name = enclosingClass.getSimpleName() + "." + name;
		
		return String.format("(%d,%d : %d,%d) %s", globalX, globalY, width, height, name);
	}
	
}
