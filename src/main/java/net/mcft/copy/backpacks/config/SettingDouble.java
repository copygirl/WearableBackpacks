package net.mcft.copy.backpacks.config;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagDouble;

import net.minecraftforge.common.config.Property;

public class SettingDouble extends SettingSingleValue<Double> {
	
	private double _minValue = Double.NEGATIVE_INFINITY;
	private double _maxValue = Double.POSITIVE_INFINITY;
	
	public double getMinValue() { return _minValue; }
	public double getMaxValue() { return _maxValue; }
	
	public SettingDouble(double defaultValue) {
		super(defaultValue);
		setConfigEntryClass("net.mcft.copy.backpacks.client.config.EntryField$Decimal");
	}
	
	/** Sets the valid range of values for this double setting. */
	public SettingDouble setValidRange(double min, double max) { _minValue = min; _maxValue = max; return this; }
	
	@Override
	protected Property.Type getPropertyType() { return Property.Type.DOUBLE; }
	
	@Override
	public Double parse(String str) {
		double i = Double.parseDouble(str);
		if ((i < _minValue) || (i > _maxValue))
			throw new IllegalArgumentException(
				"Not within valid bounds [" + _minValue + "," + _maxValue + "]");
		return i;
	}
	
	@Override
	public Double read(NBTBase tag) { return ((NBTTagDouble)tag).getDouble(); }
	@Override
	public NBTBase write(Double value) { return new NBTTagDouble(value); }
	
}