package net.mcft.copy.backpacks.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiContext {
	
	private GuiElementBase _focused;
	private GuiElementBase _pressed;
	
	
	/** Returns the currently focused control, if any. */
	public GuiElementBase getFocused() { return _focused; }
	
	/** Sets the currently focused control. */
	protected void setFocused(GuiElementBase value) { _focused = value; }
	
	
	/** Returns the currently pressed control, if any. */
	public GuiElementBase getPressed() { return _pressed; }
	
	/** Sets the currently pressed control. */
	protected void setPressed(GuiElementBase value) { _pressed = value; }
	
}
