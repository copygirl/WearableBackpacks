package net.mcft.copy.backpacks.config;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class SettingBoolean extends Setting<Boolean> {
	
	public SettingBoolean(boolean defaultValue) { super(defaultValue); }
	
	@Override
	protected Property getPropertyFromConfig(Configuration config) {
		return config.get(getCategory(), getName(),
			String.valueOf(getDefault()), getComment(), Property.Type.BOOLEAN);
	}
	
	@Override
	public Boolean getFromProperty() { return getProperty().getBoolean(); }
	
	@Override
	public Boolean read(NBTBase tag) { return (((NBTTagByte)tag).getByte() != 0); }
	@Override
	public NBTBase write(Boolean value) { return new NBTTagByte(value ? (byte)1 : (byte)0); }
	
}