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
	
	
	@Override
	void setContext(GuiContext context) {
		super.setContext(context);
		children.forEach(child -> child.setContext(context));
	}
	
	/** Adds the specified control to this container. */
	public void add(GuiElementBase control) {
		if (control.getContext() != null)
			throw new UnsupportedOperationException("The specified element already has a context set");
		children.add(control);
		
		if (getContext() != null)
			control.setContext(getContext());
		control.setParent(this);
		
		control.onControlAdded();
		if ((getWidth() != 0) && getHeight() != 0)
			control.onParentResized(0, 0);
	}
	
	/** Removes the specified control from this container. */
	public void remove(GuiElementBase control) {
		if (!children.remove(control))
			throw new UnsupportedOperationException("The specified element is not a child of this container");
		control.setContext(null);
		control.setParent(null);
	}
	
	
	@Override
	public void onResized(int prevWidth, int prevHeight)
		{ children.forEach(child -> child.onParentResized(prevWidth, prevHeight)); }
	
	@Override
	public boolean onMouseDown(int mouseButton, int mouseX, int mouseY) {
		for (GuiElementBase child : children) {
			int mx = mouseX - child.getX();
			int my = mouseY - child.getY();
			if (!child.controlContains(mx, my)) continue;
			if (child.onMouseDown(mouseButton, mouseX, mouseY)) return true;
		}
		return false;
	}
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		for (GuiElementBase child : children) {
			int mx = mouseX - child.getX();
			int my = mouseY - child.getY();
			GlStateManager.pushMatrix();
			GlStateManager.translate(child.getX(), child.getY(), 0);
			child.draw(mx, my, partialTicks);
			GlStateManager.popMatrix();
		}
	}
	
}
