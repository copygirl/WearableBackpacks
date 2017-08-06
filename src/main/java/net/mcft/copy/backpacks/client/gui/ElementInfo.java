package net.mcft.copy.backpacks.client.gui;

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
	
	
	public Stream<ElementInfo> getElementsAt(int x, int y) {
		Stream<ElementInfo> stream = Stream.of(this);
		if (element instanceof GuiContainer)
			stream = Stream.concat(stream, getChildElements()
				.filter(child -> child.contains(x, y))
				.findAny().map(child -> child.getElementsAt(x, y))
				.orElse(Stream.empty()));
		return stream;
	}
	
	public Stream<ElementInfo> getChildElements() {
		if (element instanceof GuiContainer) {
			GuiContainer container = (GuiContainer)element;
			return container.children.stream().map(child -> {
				int childX = container.getChildX(child);
				int childY = container.getChildY(child);
				return new ElementInfo(child, globalX + childX, globalY + childY, childX, childY);
			});
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
