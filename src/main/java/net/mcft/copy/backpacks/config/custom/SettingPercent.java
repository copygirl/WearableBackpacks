package net.mcft.copy.backpacks.config.custom;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import net.mcft.copy.backpacks.config.SettingDouble;

public class SettingPercent extends SettingDouble {
	
	public SettingPercent(double defaultValue) {
		super(defaultValue);
		setValidRange(0.0F, 1.0F);
		setConfigEntryClass("net.mcft.copy.backpacks.client.gui.config.EntrySlider$Percentage");
	}
	
	@Override
	protected Property getPropertyFromConfig(Configuration config) {
		return config.get(getCategory(), getName(),
			String.valueOf(getDefault()), getComment(), Property.Type.STRING);
	}
	
	@Override
	public Double parse(String str) {
		boolean isPercent = false;
		if (str.endsWith("%")) {
			isPercent = true;
			str = str.substring(0, str.length() - 1);
		}
		double d = Double.parseDouble(str);
		if (isPercent) d /= 100;
		if ((d < getMinValue()) || (d > getMaxValue()))
			throw new IllegalArgumentException(
				"Not within valid bounds [" + getMinValue() + "," + getMaxValue() + "]");
		return d;
	}
	
	@Override
	public String stringify(Double value) {
		return (value * 100) + "%";
	}
	
}