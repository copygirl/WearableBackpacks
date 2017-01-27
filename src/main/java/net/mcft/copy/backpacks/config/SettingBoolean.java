package net.mcft.copy.backpacks.config;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;

import net.minecraftforge.common.config.Property;

public class SettingBoolean extends Setting<Boolean> {
	
	public SettingBoolean(String category, String name, boolean defaultValue) {
		super(category, name, defaultValue);
	}
	
	@Override
	public Boolean read(NBTBase tag) { return (((NBTTagByte)tag).getByte() != 0); }
	@Override
	public NBTBase write(Boolean value) { return new NBTTagByte(value ? (byte)1 : (byte)0); }
	
	@Override
	public Property.Type getType() { return Property.Type.BOOLEAN; }
	
	@Override
	public Boolean load(Property property) { return property.getBoolean(); }
	@Override
	public void save(Property property, Boolean value) { property.set(value); }
	
}