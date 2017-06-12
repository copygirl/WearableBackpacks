package net.mcft.copy.backpacks.config.custom;

import net.minecraft.nbt.NBTBase;

import net.mcft.copy.backpacks.config.SettingSingleValue;
import net.mcft.copy.backpacks.misc.BackpackSize;

public class SettingBackpackSize extends SettingSingleValue<BackpackSize> {
	
	public SettingBackpackSize(int defaultColumns, int defaultRows) {
		super(new BackpackSize(defaultColumns, defaultRows));
		setConfigEntryClass("net.mcft.copy.backpacks.client.config.custom.EntryBackpackSize");
	}
	
	@Override
	public BackpackSize parse(String str) { return BackpackSize.parse(str); }
	
	@Override
	public BackpackSize read(NBTBase tag) { return BackpackSize.parse(tag); }
	@Override
	public NBTBase write(BackpackSize value) { return value.serializeNBT(); }
	
}