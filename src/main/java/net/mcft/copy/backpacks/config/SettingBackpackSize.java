package net.mcft.copy.backpacks.config;

import net.minecraft.nbt.NBTBase;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import net.mcft.copy.backpacks.misc.BackpackSize;

public class SettingBackpackSize extends Setting<BackpackSize> {
	
	public SettingBackpackSize(int defaultColumns, int defaultRows) {
		super(new BackpackSize(defaultColumns, defaultRows));
		setConfigEntryClass("net.mcft.copy.backpacks.client.config.BackpackSizeEntry");
	}
	
	@Override
	protected Property getPropertyFromConfig(Configuration config) {
		return config.get(getCategory(), getName(),
			String.valueOf(getDefault()), getComment(), Property.Type.STRING);
	}
	
	@Override
	public BackpackSize getFromProperty() { return BackpackSize.parse(getProperty().getString()); }
	
	@Override
	public BackpackSize read(NBTBase tag) { return BackpackSize.parse(tag); }
	@Override
	public NBTBase write(BackpackSize value) { return value.serializeNBT(); }
	
}