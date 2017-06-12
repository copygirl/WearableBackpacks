package net.mcft.copy.backpacks.config;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;

import net.minecraftforge.common.config.Property;

public class SettingInteger extends SettingSingleValue<Integer> {
	
	private int _minValue = Integer.MIN_VALUE;
	private int _maxValue = Integer.MAX_VALUE;
	
	public int getMinValue() { return _minValue; }
	public int getMaxValue() { return _maxValue; }
	
	public SettingInteger(int defaultValue) {
		super(defaultValue);
		setConfigEntryClass("net.mcft.copy.backpacks.client.config.EntryField$Number");
	}
	
	/** Sets the valid range of values for this integer setting. */
	public SettingInteger setValidRange(int min, int max) { _minValue = min; _maxValue = max; return this; }
	
	@Override
	protected Property.Type getPropertyType() { return Property.Type.INTEGER; }
	
	@Override
	public Integer parse(String str) {
		int i = Integer.parseInt(str);
		if ((i < _minValue) || (i > _maxValue))
			throw new IllegalArgumentException(
				"Not within valid bounds [" + _minValue + "," + _maxValue + "]");
		return i;
	}
	
	@Override
	public Integer read(NBTBase tag) { return ((NBTTagInt)tag).getInt(); }
	@Override
	public NBTBase write(Integer value) { return new NBTTagInt(value); }
	
}