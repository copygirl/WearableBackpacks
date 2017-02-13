package net.mcft.copy.backpacks.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import net.mcft.copy.backpacks.container.ContainerBackpack;

public class GuiBackpack extends GuiContainer {
	
	private static final GuiTextureResource CONTAINER_TEX =
		new GuiTextureResource("backpack", 512, 512);
	
	
	private final ContainerBackpack _container;
	
	public GuiBackpack(ContainerBackpack container) {
		super(container);
		_container = container;
		xSize = container.getWidth();
		ySize = container.getHeight();
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String title = (_container.titleLocalized ? _container.title : I18n.format(_container.title));
		fontRendererObj.drawString(title, _container.getBorderSide(), 6, 0x404040);
		int invTitleX = _container.getPlayerInvXOffset();
		int invTitleY = _container.getBorderTop() + _container.getContainerInvHeight() + 3;
		fontRendererObj.drawString(I18n.format("container.inventory"), invTitleX, invTitleY, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		
		// Miiight have gone *a little* overboard with the local variables...
		int b = _container.getBorderSide();
		int bTop = _container.getBorderTop();
		int bBot = _container.getBorderBottom();
		int w = xSize - b * 2;
		int h = _container.getContainerInvHeight();
		int pw = _container.getPlayerInvWidth();
		int ph = _container.getPlayerInvHeight();
		int maxw = _container.getMaxColumns() * 18;
		int maxh = _container.getMaxRows() * 18;
		int bufi = _container.getBufferInventory();
		
		int x1 = x;
		int x2 = x + b;
		int x3 = x + xSize - b;
		int px = x + _container.getPlayerInvXOffset();
		
		int tx1 = 4;
		int tx2 = tx1 + b + 2;
		int tx3 = tx2 + maxw + 2;
		int ty = 4;
		int tpx = 2 + b + 2 + (maxw - (pw + b * 2)) / 2 + 2;
		
		CONTAINER_TEX.bind();
		
		// Top
		CONTAINER_TEX.drawQuad(x1, y, tx1, ty, b, bTop);
		CONTAINER_TEX.drawQuad(x2, y, tx2, ty, w, bTop);
		CONTAINER_TEX.drawQuad(x3, y, tx3, ty, b, bTop);
		y += bTop; ty += bTop + 2;
		
		// Container background
		CONTAINER_TEX.drawQuad(x1, y, tx1, ty, b, h);
		CONTAINER_TEX.drawQuad(x2, y, tx2, ty, w, h);
		CONTAINER_TEX.drawQuad(x3, y, tx3, ty, b, h);
		// Container slots
		CONTAINER_TEX.drawQuad(x + _container.getContainerInvXOffset(), y, tx2, 256,
		                       _container.getContainerInvWidth(), h);
		y += h; ty += maxh + 2;
		
		// Space between container and player inventory
		if (_container.columns > 9) {
			int sw = (w - (pw + b * 2)) / 2;
			CONTAINER_TEX.drawQuad(x1, y, tx1 - 2, ty, b, bBot);
			CONTAINER_TEX.drawQuad(x2, y, tx1 + b, ty, sw, bBot);
			CONTAINER_TEX.drawQuad(px - b, y, tpx, ty, pw + b * 2, bufi);
			CONTAINER_TEX.drawQuad(px + pw + b, y, tx3 - sw, ty, sw, bBot);
			CONTAINER_TEX.drawQuad(x3, y, tx3 + 2, ty, b, bBot);
		}
		ty += bufi + 2;
		if (_container.columns <= 9)
			CONTAINER_TEX.drawQuad(x, y, tpx, ty, pw + b * 2, bufi);
		y += bufi; ty += bufi + 2;
		
		// Player inventory
		CONTAINER_TEX.drawQuad(px - b, y, tpx, ty, pw + b * 2, ph + bBot);
	}
	
}
