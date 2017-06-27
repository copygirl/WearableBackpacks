package net.mcft.copy.backpacks.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiContainerScreen extends GuiScreen {
	
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
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground(0);
		container.draw(mouseX, mouseY, partialTicks);
		// TODO: Render tooltips.
	}
	
}
