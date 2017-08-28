package net.mcft.copy.backpacks.client.gui.config;

import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.control.GuiSlider;
import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.SettingDouble;
import net.mcft.copy.backpacks.config.SettingInteger;

@SideOnly(Side.CLIENT)
public abstract class EntrySlider<T> extends BaseEntrySetting<T> {
	
	public EntrySlider(Setting<T> setting)
		{ this(setting, 0.0, 1.0); }
	public EntrySlider(Setting<T> setting, double min, double max)
		{ this(setting, new GuiSlider(), min, max); }
	public EntrySlider(Setting<T> setting, GuiSlider slider, double min, double max) {
		super(setting, setSliderRange(slider, min, max));
		slider.setChangedAction(this::onSliderChanged);
	}
	private static GuiSlider setSliderRange(GuiSlider slider, double min, double max)
		{ slider.setRange(min, max); return slider; }
	
	public GuiSlider getSlider() { return (GuiSlider)control; }
	
	/** Called when the field's value changes from player input. */
	protected abstract void onSliderChanged();
	
	
	public static class RangeDouble extends EntrySlider<Double> {
		public RangeDouble(SettingDouble setting)
			{ this(setting, new GuiSlider()); }
		public RangeDouble(SettingDouble setting, GuiSlider slider)
			{ super(setting, slider, setting.getMinValue(), setting.getMaxValue()); }
		@Override
		protected void onChanged() { getSlider().setValue(getValue().get()); }
		@Override
		protected void onSliderChanged() { setValue(getSlider().getValue()); }
	}
	
	public static class RangeInteger extends EntrySlider<Integer> {
		public RangeInteger(SettingInteger setting)
			{ this(setting, new GuiSlider()); }
		public RangeInteger(SettingInteger setting, GuiSlider slider) {
			super(setting, slider, setting.getMinValue(), setting.getMaxValue());
			getSlider().setStepSize(1);
			getSlider().setValueFormatter(val -> Long.toString(Math.round(val)));
		}
		@Override
		protected void onChanged() { getSlider().setValue(getValue().get()); }
		@Override
		protected void onSliderChanged() { setValue((int)Math.round(getSlider().getValue())); }
	}
	
	public static class Percentage extends RangeDouble {
		public Percentage(SettingDouble setting)
			{ this(setting, new GuiSlider()); }
		public Percentage(SettingDouble setting, GuiSlider slider) {
			super(setting, slider);
			int numDecimals = (int)Math.ceil(Math.max(0, 1.5 - Math.log10(setting.getMaxValue() - setting.getMinValue())));
			DecimalFormat formatter = new DecimalFormat("0." + StringUtils.repeat('#', numDecimals));
			getSlider().setValueFormatter(val -> formatter.format(val * 100) + "%");
			getSlider().setStepSize(1 / Math.pow(10, numDecimals));
		}
	}
	
}
