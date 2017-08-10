package net.mcft.copy.backpacks.client.gui.config.custom;

import java.util.EnumSet;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.config.BaseEntrySetting;
import net.mcft.copy.backpacks.client.gui.control.GuiSlider;
import net.mcft.copy.backpacks.config.custom.SettingBackpackSize;
import net.mcft.copy.backpacks.misc.BackpackSize;

@SideOnly(Side.CLIENT)
public class EntryBackpackSize extends BaseEntrySetting<BackpackSize> {
	
	public EntryBackpackSize(SettingBackpackSize setting) {
		super(setting, new Control());
		getControl()._entry = this;
		getControl().setChangedAction(this::onControlChanged);
	}
	
	private Control getControl() { return (Control)control; }
	
	@Override
	protected void onChanged() {
		getControl().setValue(getValue().get().getColumns(),
		                      getValue().get().getRows());
	}
	
	protected void onControlChanged() {
		setValue(new BackpackSize((int)getControl().getValueX(),
		                          (int)getControl().getValueY()));
	}
	
	private static class Control extends GuiSlider {
		
		private EntryBackpackSize _entry;
		
		public Control() {
			super(200, 0, EnumSet.allOf(Direction.class));
			setRangeX(BackpackSize.MIN.getColumns(), BackpackSize.MAX.getColumns());
			setRangeY(BackpackSize.MIN.getRows(), BackpackSize.MAX.getRows());
			setStepSize(1);
			setSliderSize(16);
		}
		
		@Override
		public String getValueText()
			{ return _entry.getValue().get().toString(); }
		
		@Override
		public void onSizeChanged(Direction direction)
			{ setHeight(getWidth() * BackpackSize.MAX.getRows() / BackpackSize.MAX.getColumns()); }
		
	}
	
}
