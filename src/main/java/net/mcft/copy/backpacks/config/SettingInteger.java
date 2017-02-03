package net.mcft.copy.backpacks.config;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class SettingInteger extends Setting<Integer> {
	
	private int _minValue = Integer.MIN_VALUE;
	private int _maxValue = Integer.MAX_VALUE;
	
	public SettingInteger(int defaultValue) { super(defaultValue); }
	
	/** Sets the valid range of values for this integer setting. */
	public SettingInteger setValidRange(int min, int max) { _minValue = min; _maxValue = max; return this; }
	
	@Override
	protected Property getPropertyFromConfig(Configuration config) {
		Property property = config.get(getCategory(), getName(),
			String.valueOf(getDefault()), getComment(), Property.Type.INTEGER);
		property.setMinValue(_minValue);
		property.setMaxValue(_maxValue);
		return property;
	}
	
	@Override
	public Integer getFromProperty() { return getProperty().getInt(); }
	
	@Override
	public Integer read(NBTBase tag) { return ((NBTTagInt)tag).getInt(); }
	@Override
	public NBTBase write(Integer value) { return new NBTTagInt(value); }
	
}