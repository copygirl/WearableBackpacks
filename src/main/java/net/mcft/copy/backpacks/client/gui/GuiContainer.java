package net.mcft.copy.backpacks.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.GlStateManager;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiContainer extends GuiElementBase {
	
	protected List<GuiElementBase> children = new ArrayList<GuiElementBase>();
	
	
	public GuiContainer() {  }
	public GuiContainer(GuiContext context) { setContext(context); }
	
	
	/** Called when a child element is added. */
	public void onChildAdded(GuiElementBase element) {
		for (Direction direction : Direction.values())
			updateChildSizes(direction);
	}
	
	/** Called when a child element is added. */
	public void onChildRemoved(GuiElementBase element) {
		for (Direction direction : Direction.values())
			updateChildSizes(direction);
	}
	
	/** Called when a child element is resized. */
	public void onChildSizeChanged(GuiElementBase element, Direction direction) {  }
	
	/** Called when a child element's alignment changes. */
	public void onChildAlignChanged(GuiElementBase element, Direction direction) {  }
	
	
	public int getChildPos(GuiElementBase element, Direction direction) {
		Alignment align = element.getAlign(direction);
		if (align instanceof Alignment.Min)
			return ((Alignment.Min)align).min;
		else if (align instanceof Alignment.Max)
			return getSize(direction) - element.getSize(direction) - ((Alignment.Max)align).max;
		else if (align instanceof Alignment.Both)
			return ((Alignment.Both)align).min;
		else if (align instanceof Alignment.Center)
			return (getSize(direction) - element.getSize(direction)) / 2;
		else throw new UnsupportedOperationException("Unsupported Alignment '" + align.getClass() + "'");
	}
	public final int getChildX(GuiElementBase element)
		{ return getChildPos(element, Direction.HORIZONTAL); }
	public final int getChildY(GuiElementBase element)
		{ return getChildPos(element, Direction.VERTICAL); }
	
	
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
		updateChildSizes(direction);
	}
	
	protected void updateChildSizes(Direction direction) {
		for (GuiElementBase child : children) {
			Alignment align = child.getAlign(direction);
			if (align instanceof Alignment.Both) {
				Alignment.Both both = (Alignment.Both)align;
				child.setSize(direction, getSize(direction) - both.min - both.max);
			}
		}
	}
	
	@Override
	public boolean onMouseDown(int mouseButton, int mouseX, int mouseY) {
		for (GuiElementBase child : children) {
			int mx = mouseX - getChildX(child);
			int my = mouseY - getChildY(child);
			if (!child.contains(mx, my)) continue;
			if (child.onMouseDown(mouseButton, mx, my)) return true;
		}
		return false;
	}
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		for (GuiElementBase child : children) {
			int x = getChildX(child);
			int y = getChildY(child);
			int mx = mouseX - x;
			int my = mouseY - y;
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, 0);
			child.draw(mx, my, partialTicks);
			GlStateManager.popMatrix();
		}
	}
	
}
