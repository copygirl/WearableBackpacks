package net.mcft.copy.backpacks.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiContext {
	
	public static boolean DEBUG = false;
	
	private GuiElementBase _focused;
	private GuiElementBase _pressed;
	
	/** Returns the currently focused element, if any. */
	public GuiElementBase getFocused() { return _focused; }
	/** Sets the currently focused element. */
	void setFocused(GuiElementBase value) { _focused = value; }
	
	/** Returns the currently pressed element, if any. */
	public GuiElementBase getPressed() { return _pressed; }
	/** Sets the currently pressed element. */
	void setPressed(GuiElementBase value) { _pressed = value; }
	
}
