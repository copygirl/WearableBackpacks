package net.mcft.copy.backpacks.config;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;

import net.minecraftforge.common.config.Property;

public class SettingInteger extends Setting<Integer> {
	
	public SettingInteger(String category, String name, int defaultValue) {
		super(category, name, defaultValue);
	}
	
	/** Sets the valid range of values for this integer setting,
	 *  causing a validation error for values outside this range. */
	public SettingInteger setValidRange(int min, int max) {
		setValidationFunc((value) -> ((value >= min) && (value <= max)) ? null
			: String.format("Value %s is not in valid range (%s to %s)", value, min, max));
		return this;
	}
	
	@Override
	public Integer read(NBTBase tag) { return ((NBTTagInt)tag).getInt(); }
	@Override
	public NBTBase write(Integer value) { return new NBTTagInt(value); }
	
	@Override
	public Property.Type getType() { return Property.Type.INTEGER; }
	
	@Override
	public Integer load(Property property) { return property.getInt(); }
	@Override
	public void save(Property property, Integer value) { property.set(value); }
	
}