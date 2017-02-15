package net.mcft.copy.backpacks.misc;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.items.ItemStackHandler;

import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.misc.util.NbtUtils;

public class BackpackDataItems implements IBackpackData {
	
	public static final String TAG_SIZE  = "size";
	public static final String TAG_ITEMS = "items";
	
	public BackpackSize size;
	public ItemStackHandler items;
	
	public BackpackDataItems() {  }
	public BackpackDataItems(int columns, int rows) {
		this(new BackpackSize(columns, rows)); }
	public BackpackDataItems(BackpackSize size) {
		this.size = size;
		items = new ItemStackHandler(size.getColumns() * size.getRows());
	}
	
	@Override
	public NBTBase serializeNBT() {
		return NbtUtils.createCompound(
			TAG_SIZE, size.serializeNBT(),
			TAG_ITEMS, items.serializeNBT());
	}
	
	@Override
	public void deserializeNBT(NBTBase nbt) {
		NBTTagCompound compound = (NBTTagCompound)nbt;
		if (compound.hasKey(TAG_SIZE)) {
			size = BackpackSize.parse(compound.getTag(TAG_SIZE));
			items.deserializeNBT(compound.getCompoundTag(TAG_ITEMS));
		} else {
			// Backwards compatibility for 1.5.0 / 2.2.0 and before.
			items.deserializeNBT(compound);
			size = new BackpackSize(9, items.getSlots() / 9);
		}
	}
	
}
