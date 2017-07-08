package net.mcft.copy.backpacks.client.gui;

import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiContainerScreen extends GuiScreen {
	
	private static boolean DEBUG = false;
	
	public final GuiContext context;
	public final GuiContainer container;
	
	public GuiContainerScreen() {
		context   = new GuiContext();
		container = new GuiContainer(context);
	}
	
	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		container.setSize(width, height);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		container.onMouseDown(mouseButton, mouseX, mouseY);
	}
	
	// TODO: Handle onMouseMove.
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		context.setPressed(null);
		// TODO: Handle onMouseUp.
	}
	
	// TODO: Handle keyboard input.
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground(0);
		container.draw(mouseX, mouseY, partialTicks);
		// TODO: Render tooltips.
		
		if (DEBUG) {
			String debugText = "";
			LinkedList<ControlDebugInfo> hierarchy = getHierarchy(mouseX, mouseY);
			int color = 0x80000000;
			for (ControlDebugInfo info : hierarchy) {
				Gui.drawRect(info.x, info.y, info.x + info.width, info.y + info.height, color);
				debugText += String.format("(%d,%d : %d,%d) %s\n", info.x, info.y, info.width, info.height,
				                                                   info.control.getClass().getSimpleName());
				color ^= 0xFFFFFF;
			}
			// if (!hierarchy.isEmpty()) {
			// 	ControlDebugInfo info = hierarchy.getLast();
			// }
			int y = 4;
			for (String line : debugText.split("\n")) {
				GuiContainer.getFontRenderer().drawString(line, 4, y, 0xFFFFFF);
				y += GuiContainer.LINE_HEIGHT;
			}
		}
	}
	
	
	private LinkedList<ControlDebugInfo> getHierarchy(int x, int y) {
		LinkedList<ControlDebugInfo> info = new LinkedList<ControlDebugInfo>();
		getHierarchy(info, container, x, y, 0, 0);
		return info;
	}
	private void getHierarchy(LinkedList<ControlDebugInfo> info, GuiContainer container,
	                          int x, int y, int globalX, int globalY) {
		for (GuiElementBase child : container.children) {
			int childX = container.getChildX(child);
			int childY = container.getChildY(child);
			int relX = x - childX;
			int relY = y - childY;
			if (!child.controlContains(relX, relY)) continue;
			
			globalX += childX; globalY += childY;
			info.add(new ControlDebugInfo(child, globalX, globalY, child.getWidth(), child.getHeight()));
			if (child instanceof GuiContainer)
				getHierarchy(info, (GuiContainer)child, relX, relY, globalX, globalY);
			return;
		}
	}
	
	private static final class ControlDebugInfo {
		public final GuiElementBase control;
		public final int x, y, width, height;
		public ControlDebugInfo(GuiElementBase control, int x, int y, int width, int height)
			{ this.control = control; this.x = x; this.y = y; this.width = width; this.height = height; }
	}
	
}
