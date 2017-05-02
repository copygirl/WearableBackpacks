package net.mcft.copy.backpacks.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.misc.BackpackSize;

@SideOnly(Side.CLIENT)
public class EntryBackpackSize extends EntryButton<BackpackSize> {
	
	public EntryBackpackSize(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<BackpackSize> setting) {
		super(owningScreen, owningEntryList, setting, new Control());
		((Control)button)._entry = this;
	}
	
	@Override
	public int getSlotHeight() { return button.height + 2; }
	
	
	public static class Control extends GuiButtonExt {
		
		private boolean _dragging = false;
		protected EntryBackpackSize _entry;
		
		public Control() {
			// All 0 because x, y and width are set in drawEntry anyway,
			// and height depends on width and is set in drawButton.
			super(0, 0, 0, 0, 0, "");
		}
		
		@Override
		public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
			int slotSize = (width - 8) / BackpackSize.MAX.getColumns();
			int offset   = (width - slotSize * BackpackSize.MAX.getColumns()) / 2;
			
			int x1 = xPosition + offset;
			int y1 = yPosition + offset;
			int x2 = x1 + BackpackSize.MAX.getColumns() * slotSize;
			int y2 = y1 + BackpackSize.MAX.getRows()    * slotSize;
			
			if (enabled && (mouseX >= x1) && (mouseY >= y1) &&
			               (mouseX <  x2) && (mouseY <  y2)) {
				_entry.value = new BackpackSize(
					Math.min(Math.max(1 + (mouseX - x1) / slotSize, 1), BackpackSize.MAX.getColumns()),
					Math.min(Math.max(1 + (mouseY - y1) / slotSize, 1), BackpackSize.MAX.getRows()));
				_dragging = true;
				return true;
			} else return false;
		}
		@Override
		public void mouseReleased(int mouseX, int mouseY) { _dragging = false; }
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			int slotSize = (width - 8) / BackpackSize.MAX.getColumns();
			int offset   = (width - slotSize * BackpackSize.MAX.getColumns()) / 2;
			height = slotSize * BackpackSize.MAX.getRows() + (width - slotSize * BackpackSize.MAX.getColumns());
			
			if (!visible) return;
			if (_dragging)
				_entry.value = new BackpackSize(
					Math.min(Math.max(1 + (mouseX - (xPosition + offset)) / slotSize, 1), BackpackSize.MAX.getColumns()),
					Math.min(Math.max(1 + (mouseY - (yPosition + offset)) / slotSize, 1), BackpackSize.MAX.getRows()));
			BackpackSize value = _entry.value;
			
			GlStateManager.color(1.0F, 1.0F, 1.0F);
			// Draw background.
			GuiUtils.drawContinuousTexturedBox(BUTTON_TEXTURES, xPosition, yPosition, 0, 46,
			                                   width, height, 200, 20, 2, 3, 2, 2, zLevel);
			
			// Draw slots.
			for (int column = 1; column <= BackpackSize.MAX.getColumns(); column++)
				for (int row = 1; row <= BackpackSize.MAX.getRows(); row++) {
					int x = xPosition + offset + (column - 1) * slotSize;
					int y = yPosition + offset + (row    - 1) * slotSize;
					boolean hover = (mouseX >= x) && (mouseX < x + slotSize) &&
					                (mouseY >= y) && (mouseY < y + slotSize);
					boolean active = (column <= value.getColumns()) && (row <= value.getRows());
					boolean selected = (column == value.getColumns()) && (row == value.getRows());
					if (!active && (!enabled || !hover)) continue;
					int texY = (enabled ? (hover ? 86 : 66) : 46);
					int b    = ((selected || !enabled) ? 0 : 1);
					GuiUtils.drawContinuousTexturedBox(x, y, b, texY + b, slotSize, slotSize,
					                                   200 - b*2, 20 - b*2, 2-b, 3-b, 2-b, 2-b, zLevel);
				}
			
			int color = (enabled ? 0xFFFFFF : 0x707070);
			drawCenteredString(mc.fontRenderer, value.toString(), xPosition + width / 2, yPosition + (height - 8) / 2, color);
		}
		
	}
	
}
