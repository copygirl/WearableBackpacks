package net.mcft.copy.backpacks.client.gui.config;

import java.text.DecimalFormat;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.control.GuiSlider;
import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.SettingDouble;
import net.mcft.copy.backpacks.config.SettingInteger;

@SideOnly(Side.CLIENT)
public abstract class EntryValueSlider<T> extends GuiSlider
	implements IConfigValue<T>, IConfigValue.Setup<T> {
	
	public EntryValueSlider() { setHeight(IConfigEntry.DEFAULT_ENTRY_HEIGHT); }
	
	public static class RangeDouble extends EntryValueSlider<Double> {
		@Override
		public void setup(Setting<Double> setting) {
			SettingDouble settingD = (SettingDouble)setting;
			setRange(settingD.getMinValue(), settingD.getMaxValue());
		}
		@Override
		public Optional<Double> getValue() { return Optional.of(getSliderValue()); }
		@Override
		public void setValue(Double value) { setSliderValue(value); }
	}
	
	public static class RangeInteger extends EntryValueSlider<Integer> {
		public RangeInteger() {
			setValueFormatter(val -> Long.toString(Math.round(val)));
			setStepSize(1);
		}
		@Override
		public void setup(Setting<Integer> setting) {
			SettingInteger settingI = (SettingInteger)setting;
			setRange(settingI.getMinValue(), settingI.getMaxValue());
		}
		@Override
		public Optional<Integer> getValue() { return Optional.of((int)Math.round(getSliderValue())); }
		@Override
		public void setValue(Integer value) { setSliderValue(value); }
	}
	
	public static class Percentage extends RangeDouble {
		public Percentage() { setRange(0.0, 1.0); }
		@Override
		public void setRange(Direction direction, double min, double max) {
			super.setRange(direction, min, max);
			double range = getMax(Direction.HORIZONTAL) - getMin(Direction.HORIZONTAL);
			int numDecimals = (int)Math.ceil(Math.max(0, 1.5 - Math.log10(range)));
			DecimalFormat formatter = new DecimalFormat("0." + StringUtils.repeat('#', numDecimals));
			setValueFormatter(val -> formatter.format(val * 100) + "%");
			setStepSize(1 / Math.pow(10, numDecimals));
		}
	}
	
}
