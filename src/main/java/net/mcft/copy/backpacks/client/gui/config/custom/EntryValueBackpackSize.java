package net.mcft.copy.backpacks.client.gui.config.custom;

import java.util.EnumSet;
import java.util.Optional;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.config.IConfigValue;
import net.mcft.copy.backpacks.client.gui.control.GuiSlider;
import net.mcft.copy.backpacks.misc.BackpackSize;

@SideOnly(Side.CLIENT)
public class EntryValueBackpackSize extends GuiSlider implements IConfigValue<BackpackSize> {
	
	public EntryValueBackpackSize() {
		super(200, 0, EnumSet.allOf(Direction.class));
		setRangeX(BackpackSize.MIN.getColumns(), BackpackSize.MAX.getColumns());
		setRangeY(BackpackSize.MIN.getRows(), BackpackSize.MAX.getRows());
		setStepSize(1);
		setSliderSize(16);
	}
	
	@Override
	public Optional<BackpackSize> getValue() {
		return Optional.of(new BackpackSize(
			(int)getSliderValueX(), (int)getSliderValueY()));
	}
	@Override
	public void setValue(BackpackSize value) {
		setSliderValue(value.getColumns(), value.getRows());
	}
	
	@Override
	public String getValueText()
		{ return getValue().get().toString(); }
	
	@Override
	public void onSizeChanged(Direction direction)
		{ setHeight(getWidth() * BackpackSize.MAX.getRows() / BackpackSize.MAX.getColumns()); }
	
}
