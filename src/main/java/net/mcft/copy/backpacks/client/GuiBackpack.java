package net.mcft.copy.backpacks.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import net.mcft.copy.backpacks.container.ContainerBackpack;

public class GuiBackpack extends GuiContainer {
	
	// TODO: Use own container texture which can be resized.
	private static final ResourceLocation CONTAINER_TEX =
		new ResourceLocation("textures/gui/container/generic_54.png");
	
	private final ContainerBackpack _container;
	
	public GuiBackpack(ContainerBackpack container) {
		super(container);
		_container = container;
		ySize = 114 + container.items.getSlots() * 2;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String title = (_container.titleLocalized ? _container.title : I18n.format(_container.title));
		fontRendererObj.drawString(title, 8, 6, 0x404040);
		fontRendererObj.drawString(I18n.format("container.inventory"), 8, ySize - 94, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(CONTAINER_TEX);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize - 97);
		drawTexturedModalRect(x, y + ySize - 97, 0, 126, xSize, 96);
	}
	
}
