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
	
	
	/** Called when a child control is added. */
	public void onChildAdded(GuiElementBase control) {
		for (Direction direction : Direction.values())
			updateChildSizes(direction);
	}
	
	/** Called when a child control is added. */
	public void onChildRemoved(GuiElementBase control) {
		for (Direction direction : Direction.values())
			updateChildSizes(direction);
	}
	
	/** Called when a child control is resized. */
	public void onChildSizeChanged(GuiElementBase control, Direction direction) {  }
	
	/** Called when a child control's alignment changes. */
	public void onChildAlignChanged(GuiElementBase control, Direction direction) {  }
	
	
	public int getChildPos(GuiElementBase control, Direction direction) {
		Alignment align = control.getAlign(direction);
		if (align instanceof Alignment.Min)
			return ((Alignment.Min)align).min;
		else if (align instanceof Alignment.Max)
			return getSize(direction) - control.getSize(direction) - ((Alignment.Max)align).max;
		else if (align instanceof Alignment.Both)
			return ((Alignment.Both)align).min;
		else if (align instanceof Alignment.Center)
			return (getSize(direction) - control.getSize(direction)) / 2;
		else throw new UnsupportedOperationException("Unsupported Alignment '" + align.getClass() + "'");
	}
	public final int getChildX(GuiElementBase control)
		{ return getChildPos(control, Direction.HORIZONTAL); }
	public final int getChildY(GuiElementBase control)
		{ return getChildPos(control, Direction.VERTICAL); }
	
	
	/** Adds the specified control to this container. */
	public void add(GuiElementBase control) {
		if (control.getContext() != null)
			throw new UnsupportedOperationException("The specified element already has a context set");
		
		if (getContext() != null)
			control.setContext(getContext());
		control.setParent(this);
		children.add(control);
		
		control.onControlAdded();
		onChildAdded(control);
	}
	/** Adds all of the specified controls to this container. */
	public void addAll(GuiElementBase... controls)
		{ for (GuiElementBase control : controls) add(control); }
	
	/** Removes the specified control from this container. */
	public void remove(GuiElementBase control) {
		if (!children.remove(control))
			throw new UnsupportedOperationException("The specified element is not a child of this container");
		control.setContext(null);
		control.setParent(null);
		onChildRemoved(control);
	}
	
	
	@Override
	void setContext(GuiContext context) {
		super.setContext(context);
		children.forEach(child -> child.setContext(context));
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
			if (!child.controlContains(mx, my)) continue;
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
