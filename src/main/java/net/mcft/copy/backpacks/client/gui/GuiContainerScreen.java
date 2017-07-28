package net.mcft.copy.backpacks.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiContainerScreen extends GuiScreen {
	
	private int _lastMouseX = -1;
	private int _lastMouseY = -1;
	
	public final GuiContext context;
	public final GuiContainer container;
	
	public GuiContainerScreen() {
		context   = new GuiContext();
		container = new GuiContainer(context);
		container.setFill();
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
	
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		
		int mouseX = Mouse.getEventX() * width / mc.displayWidth;
		int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
		
		int scroll = Integer.signum(Mouse.getEventDWheel());
		if (scroll != 0) container.onMouseScroll(scroll, mouseX, mouseY);
	}
	
	@Override
	public void updateScreen() {
		int mouseX = Mouse.getX() * width / mc.displayWidth;
		int mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;
		if ((mouseX != _lastMouseX) || (mouseY != _lastMouseY)) {
			GuiElementBase pressed = context.getPressed();
			if (pressed != null) {
				int mx = mouseX;
				int my = mouseY;
				for (GuiElementBase element = pressed; element.getParent() != null; element = element.getParent()) {
					mx -= element.getParent().getChildX(element);
					my -= element.getParent().getChildY(element);
				}
				pressed.onMouseMove(mx, my);
			} else if (container.contains(mouseX, mouseY))
				container.onMouseMove(mouseX, mouseY);
			_lastMouseX = mouseX;
			_lastMouseY = mouseY;
		}
	}
	
	// TODO: Handle onMouseMove.
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		context.setPressed(null, mouseX, mouseY);
		// TODO: Handle onMouseUp.
	}
	
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (keyCode == Keyboard.KEY_F3)
			GuiContext.DEBUG = !GuiContext.DEBUG;
		// TODO: Handle keyboard input.
	}
	
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground(0);
		container.draw(mouseX, mouseY, partialTicks);
		// TODO: Render tooltips.
		
		if (GuiContext.DEBUG) {
			String debugText = "";
			LinkedList<ElementDebugInfo> hierarchy =
				new ElementDebugInfo(container).getHierarchy(mouseX, mouseY);
			int color = 0x40000000;
			for (ElementDebugInfo info : hierarchy) {
				Gui.drawRect(info.x, info.y, info.x + info.width, info.y + info.height, color);
				debugText += info.toString() + "\n";
				color ^= 0xFFFFFF;
			}
			ElementDebugInfo last = hierarchy.getLast();
			if (last.element instanceof GuiContainer) {
				GuiContainer container = (GuiContainer)last.element;
				Gui.drawRect(last.x,                                            last.y,
				             last.x + container.getPaddingLeft(),               last.y + last.height,               0x400000FF);
				Gui.drawRect(last.x + last.width - container.getPaddingRight(), last.y,
				             last.x + last.width,                               last.y + last.height,               0x400000FF);
				Gui.drawRect(last.x + container.getPaddingLeft(),               last.y,
				             last.x + last.width - container.getPaddingRight(), last.y + container.getPaddingTop(), 0x400000FF);
				Gui.drawRect(last.x + container.getPaddingLeft(),               last.y + last.height - container.getPaddingBottom(),
				             last.x + last.width - container.getPaddingRight(), last.y + last.height,               0x400000FF);
				debugText += "\nChildren:\n";
				for (ElementDebugInfo child : last.getChildElements()) {
					Gui.drawRect(child.x, child.y, child.x + child.width, child.y + child.height, 0x40FF0000);
					debugText += "  " + child.toString() + "\n";
				}
			}
			int y = 4;
			for (String line : debugText.split("\n")) {
				GuiContainer.getFontRenderer().drawStringWithShadow(line, 4 + 1, y + 1, 0xFFFFFF);
				y += GuiElementBase.LINE_HEIGHT;
			}
		}
	}
	
	
	private static final class ElementDebugInfo {
		
		public final GuiElementBase element;
		public final int x, y, width, height;
		
		public ElementDebugInfo(GuiElementBase element)
			{ this(element, 0, 0); }
		public ElementDebugInfo(GuiElementBase element, int x, int y)
			{ this(element, x, y, element.getWidth(), element.getHeight()); }
		public ElementDebugInfo(GuiElementBase element, int x, int y, int width, int height)
			{ this.element = element; this.x = x; this.y = y; this.width = width; this.height = height; }
		
		public LinkedList<ElementDebugInfo> getHierarchy(int x, int y) {
			LinkedList<ElementDebugInfo> info = new LinkedList<ElementDebugInfo>();
			addToHierarchy(info, x, y);
			return info;
		}
		private void addToHierarchy(List<ElementDebugInfo> list, int x, int y) {
			list.add(this);
			if (!(element instanceof GuiContainer)) return;
			GuiContainer container = (GuiContainer)element;
			for (GuiElementBase child : container.children) {
				int childX = this.x + container.getChildX(child);
				int childY = this.y + container.getChildY(child);
				if (!child.contains(x - childX, y - childY)) continue;
				new ElementDebugInfo(child, childX, childY).addToHierarchy(list, x, y);
				return;
			}
		}
		
		public List<ElementDebugInfo> getChildElements() {
			ArrayList<ElementDebugInfo> children = new ArrayList<ElementDebugInfo>();
			if (element instanceof GuiContainer) {
				GuiContainer container = (GuiContainer)element;
				for (GuiElementBase child : container.children)
					children.add(new ElementDebugInfo(child, x + container.getChildX(child),
					                                         y + container.getChildY(child)));
			}
			return children;
		}
		
		@Override
		public String toString() {
			Class<?> elementClass = element.getClass();
			
			String name = elementClass.getSimpleName();
			if (name.isEmpty()) name = elementClass.getSuperclass().getSimpleName();
			
			Class<?> enclosingClass = elementClass.getEnclosingClass();
			if (enclosingClass != null) name = enclosingClass.getSimpleName() + "." + name;
			
			return String.format("(%d,%d : %d,%d) %s", x, y, width, height, name);
		}
		
	}
	
}
