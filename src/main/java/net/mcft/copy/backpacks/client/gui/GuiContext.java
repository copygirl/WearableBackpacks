package net.mcft.copy.backpacks.client.gui;

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
	
}
