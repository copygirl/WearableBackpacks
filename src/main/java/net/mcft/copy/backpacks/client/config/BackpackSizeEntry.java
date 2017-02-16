package net.mcft.copy.backpacks.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ListEntryBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.misc.BackpackSize;

@SideOnly(Side.CLIENT)
public class BackpackSizeEntry extends ListEntryBase implements GuiConfigExt.IVarHeightEntry {
	
	private final Control _control;
	private BackpackSize _beforeValue;
	
	public BackpackSizeEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
		super(owningScreen, owningEntryList, configElement);
		BackpackSize value = BackpackSize.parse(configElement.get().toString());
		_control = new Control(value);
		_beforeValue = value;
	}
	
	@Override
	public int getSlotHeight() { return _control.height + 2; }
	
	@Override
	public void keyTyped(char eventChar, int eventKey) {  }
	@Override
	public void updateCursorCounter() {  }
	@Override
	public void mouseClicked(int x, int y, int mouseEvent) {  }
	
	@Override
	public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		if (!_control.mousePressed(mc, x, y))
			return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
		
		_control.playPressSound(mc.getSoundHandler());
		//valueButtonPressed(index);
		//updateValueButtonText();
		return true;
	}
	@Override
	public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		super.mouseReleased(index, x, y, mouseEvent, relativeX, relativeY);
		_control.mouseReleased(x, y);
	}
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight,
	                      int mouseX, int mouseY, boolean isSelected) {
		super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);
		_control.width = owningEntryList.controlWidth;
		_control.xPosition = owningScreen.entryList.controlX;
		_control.yPosition = y;
		_control.enabled = enabled();
		_control.drawButton(mc, mouseX, mouseY);
	}
	
	@Override
	public boolean isDefault() {
		return _control.value.toString().equals(configElement.getDefault().toString());
	}
	@Override
	public void setToDefault() {
		if (enabled()) _control.value = BackpackSize.parse(configElement.getDefault().toString());
	}
	
	@Override
	public boolean isChanged() { return !_control.value.equals(_beforeValue); }
	@Override
	public void undoChanges() { if (enabled()) _control.value = _beforeValue; }
	
	@Override
	public boolean saveConfigElement() {
		if (!enabled() || !isChanged()) return false;
		configElement.set(_control.value.toString());
		return configElement.requiresMcRestart();
	}
	
	@Override
	public Object getCurrentValue() { return _control.value.toString(); }
	@Override
	public Object[] getCurrentValues() { return new Object[] { getCurrentValue() }; }
	
	
	public static class Control extends GuiButtonExt {
		
		public BackpackSize value;
		
		public boolean _dragging = false;
		
		// X, Y and width are set in drawEntry anyway, height depends on width.
		public Control(BackpackSize value) {
			super(0, 0, 0, 0, 16, "");
			this.value = value;
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
				value = new BackpackSize(
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
				value = new BackpackSize(
					Math.min(Math.max(1 + (mouseX - (xPosition + offset)) / slotSize, 1), BackpackSize.MAX.getColumns()),
					Math.min(Math.max(1 + (mouseY - (yPosition + offset)) / slotSize, 1), BackpackSize.MAX.getRows()));
			
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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
					if (!active && !hover) continue;
					int texY = (hover ? 86 : 66);
					int b    = (selected ? 0 : 1);
					GuiUtils.drawContinuousTexturedBox(x, y, b, texY + b, slotSize, slotSize,
					                                   200 - b*2, 20 - b*2, 2-b, 3-b, 2-b, 2-b, zLevel);
				}
			
			drawCenteredString(mc.fontRendererObj, value.toString(), xPosition + width / 2, yPosition + (height - 8) / 2, 0xFFFFFF);
		}
		
	}
	
}
