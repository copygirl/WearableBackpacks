package net.mcft.copy.backpacks.client.gui;

import java.util.Stack;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiContext {
	
	public static boolean DEBUG      = false;
	public static boolean DEBUG_TEXT = false;
	
	
	private GuiElementBase _focused;
	private GuiElementBase _pressed;
	private GuiContainerScreen _screen;
	
	public GuiElementBase getFocused() { return _focused; }
	void setFocused(GuiElementBase value) { _focused = value; }
	
	public GuiElementBase getPressed() { return _pressed; }
	void setPressed(GuiElementBase value) { _pressed = value; }
	
	public GuiContainerScreen getScreen() { return _screen; }
	void setScreen(GuiContainerScreen value) { _screen = value; }
	
	
	private Stack<ScissorRegion> _scissorStack = new Stack<>();
	
	public void pushScissor(int x, int y, int width, int height) {
		ScissorRegion region = new ScissorRegion(x, y, width, height);
		_scissorStack.push(region);
		setScissor(region);
	}
	public void popScissor() {
		_scissorStack.pop();
		setScissor(!_scissorStack.empty() ? _scissorStack.peek() : null);
	}
	
	private void setScissor(ScissorRegion region) {
		if (region != null) {
			Minecraft mc = Minecraft.getMinecraft();
			int scale = new ScaledResolution(mc).getScaleFactor();
			GL11.glScissor(region.x * scale, mc.displayHeight - (region.y + region.height) * scale,
						region.width * scale, region.height * scale);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
		} else GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
	
	private static class ScissorRegion {
		public final int x, y, width, height;
		public ScissorRegion(int x, int y, int width, int height)
			{ this.x = x; this.y = y; this.width = width; this.height = height; }
	}
	
}
