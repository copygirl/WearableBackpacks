package net.mcft.copy.backpacks.config;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;

import net.minecraftforge.common.config.Property;

public class SettingBoolean extends SettingSingleValue<Boolean> {
	
	public SettingBoolean(boolean defaultValue) {
		super(defaultValue);
		setConfigEntryClass("net.mcft.copy.backpacks.client.config.EntryButton$Switch");
	}
	
	@Override
	protected Property.Type getPropertyType() { return Property.Type.BOOLEAN; }
	
	@Override
	public Boolean parse(String str) {
		str = str.toLowerCase();
		switch (str) {
			case "true": return true;
			case "false": return false;
			default: throw new IllegalArgumentException(
				"String '" + str + "' is not a valid boolean");
		}
	}
	
	@Override
	public Boolean read(NBTBase tag) { return (((NBTTagByte)tag).getByte() != 0); }
	@Override
	public NBTBase write(Boolean value) { return new NBTTagByte(value ? (byte)1 : (byte)0); }
	
}