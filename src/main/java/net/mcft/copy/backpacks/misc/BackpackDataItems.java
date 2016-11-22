package net.mcft.copy.backpacks.misc;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.items.ItemStackHandler;

import net.mcft.copy.backpacks.api.IBackpackData;

public class BackpackDataItems implements IBackpackData {
	
	public static final String TAG_ITEMS = "items";
	
	public final ItemStackHandler items;
	
	public BackpackDataItems(int size) {
		items = new ItemStackHandler(size);
	}
	
	@Override
	public NBTBase serializeNBT() { return items.serializeNBT(); }
	
	@Override
	public void deserializeNBT(NBTBase nbt) {
		if (!(nbt instanceof NBTTagCompound)) return;
		items.deserializeNBT((NBTTagCompound)nbt);
	}
	
}
