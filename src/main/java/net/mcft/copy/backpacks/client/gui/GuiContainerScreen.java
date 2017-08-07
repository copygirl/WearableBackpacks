package net.mcft.copy.backpacks.client.gui;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.GuiElementBase.MouseButton;

@SideOnly(Side.CLIENT)
public class GuiContainerScreen extends GuiScreen {
	
	private int _lastMouseX = -1;
	private int _lastMouseY = -1;
	
	public final GuiContext context;
	public final GuiContainer container;
	
	private GuiElementBase _tooltipElement = null;
	private long _tooltipTime = 0;
	
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
		if (mouseButton == MouseButton.LEFT)
			context.setFocused(null); // TODO: Handle this more nicely.
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
		
		// Handle onMouseMove.
		if ((mouseX != _lastMouseX) || (mouseY != _lastMouseY)) {
			GuiElementBase pressed = context.getPressed();
			if (pressed != null) {
				ElementInfo info = ElementInfo.getElementHierarchy(pressed).getFirst();
				pressed.onMouseMove(mouseX - info.globalX, mouseY - info.globalY);
			} else if (container.contains(mouseX, mouseY))
				container.onMouseMove(mouseX, mouseY);
			_lastMouseX = mouseX;
			_lastMouseY = mouseY;
		}
		
		// Handle tooltips.
		GuiElementBase tooltipElement = new ElementInfo(container)
			.getElementsAt(mouseX, mouseY)
			.map(info -> info.element)
			.filter(element -> element.hasTooltip())
			.reduce((first, second) -> second) // Get the last element.
			.orElse(null);
		if (tooltipElement != _tooltipElement)
			_tooltipTime = Minecraft.getSystemTime();
		_tooltipElement = tooltipElement;
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton != MouseButton.LEFT) return; // TODO: Support other mouse buttons?
		GuiElementBase pressed = context.getPressed();
		if (pressed == null) return;
		pressed.onMouseUp(mouseButton, mouseX, mouseY);
		context.setPressed(null);
	}
	
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (isShiftKeyDown() && (keyCode == Keyboard.KEY_F3))
			GuiContext.DEBUG = !GuiContext.DEBUG;
		
		GuiElementBase focused = context.getFocused();
		if (focused != null) focused.onKey(keyCode, typedChar);
		// TODO: Implement tabbing?
	}
	
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground(0);
		container.draw(mouseX, mouseY, partialTicks);
		
		if ((_tooltipElement != null) && (Minecraft.getSystemTime() >=
				_tooltipTime + _tooltipElement.getTooltipDelay()))
			_tooltipElement.drawTooltip(mouseX, mouseY, width, height, partialTicks);
		
		if (GuiContext.DEBUG)
			drawDebugInfo(mouseX, mouseY, partialTicks);
	}
	
	private void drawDebugInfo(int mouseX, int mouseY, float partialTicks) {
		FontRenderer fontRenderer = GuiElementBase.getFontRenderer();
		List<ElementInfo> hierarchy = new ElementInfo(container)
			.getElementsAt(mouseX, mouseY)
			.collect(Collectors.toList());
		ElementInfo last = hierarchy.get(hierarchy.size() - 1);
		List<ElementInfo> children = last.getChildElements().collect(Collectors.toList());
		
		// Draw element boundaries.
		int color = 0x40000000;
		for (ElementInfo info : hierarchy) {
			Gui.drawRect(info.globalX, info.globalY, info.globalX + info.width, info.globalY + info.height, color);
			color ^= 0xFFFFFF;
		}
		// Draw container padding.
		if (last.element instanceof GuiContainer) {
			GuiContainer container = (GuiContainer)last.element;
			int x = last.globalX; int y = last.globalY;
			int w = last.width;   int h = last.height;
			int padLeft   = container.getPaddingLeft();
			int padRight  = container.getPaddingRight();
			int padTop    = container.getPaddingTop();
			int padBottom = container.getPaddingBottom();
			Gui.drawRect(x,                y,                 x + padLeft,      y + h,      0x400000FF);
			Gui.drawRect(x + w - padRight, y,                 x + w,            y + h,      0x400000FF);
			Gui.drawRect(x + padLeft,      y,                 x + w - padRight, y + padTop, 0x400000FF);
			Gui.drawRect(x + padLeft,      y + h - padBottom, x + w - padRight, y + h,      0x400000FF);
		}
		// Draw child elements boundaries.
		if (!children.isEmpty())
		for (ElementInfo child : children)
			Gui.drawRect(child.globalX, child.globalY, child.globalX + child.width, child.globalY + child.height, 0x40FF0000);
		
		// Draw debug text for elements in the hierarchy ..
		int textY = 4;
		for (ElementInfo info : hierarchy) {
			fontRenderer.drawStringWithShadow(info.toString(), 4, textY, 0xFFFFFF);
			textY += GuiElementBase.LINE_HEIGHT;
		}
		// .. and the child elements of the pointed-at element.
		if (!children.isEmpty()) {
			textY += GuiElementBase.LINE_HEIGHT;
			fontRenderer.drawStringWithShadow("Children:", 4, textY, 0xFFFFFF);
			textY += GuiElementBase.LINE_HEIGHT;
			for (ElementInfo child : children) {
				fontRenderer.drawStringWithShadow(child.toString(), 10, textY, 0xFFFFFF);
				textY += GuiElementBase.LINE_HEIGHT;
			}
		}
	}
	
}
