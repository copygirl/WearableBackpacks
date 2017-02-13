package net.mcft.copy.backpacks.misc;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.items.ItemStackHandler;

import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.misc.util.NbtUtils;

public class BackpackDataItems implements IBackpackData {
	
	public static final String TAG_COLUMNS = "columns";
	public static final String TAG_ROWS    = "rows";
	public static final String TAG_ITEMS   = "items";
	
	public int columns = -1;
	public int rows    = -1;
	public ItemStackHandler items = null;
	
	public BackpackDataItems() {  }
	public BackpackDataItems(int columns, int rows) {
		this.columns = columns;
		this.rows    = rows;
		items = new ItemStackHandler(columns * rows);
	}
	
	@Override
	public NBTBase serializeNBT() {
		return NbtUtils.createCompound(
			TAG_COLUMNS, (byte)columns,
			TAG_ROWS, (byte)rows,
			TAG_ITEMS, items.serializeNBT());
	}
	
	@Override
	public void deserializeNBT(NBTBase nbt) {
		NBTTagCompound compound = (NBTTagCompound)nbt;
		if (compound.hasKey(TAG_COLUMNS)) {
			columns = compound.getByte(TAG_COLUMNS);
			rows    = compound.getByte(TAG_ROWS);
			items.deserializeNBT(compound.getCompoundTag(TAG_ITEMS));
		} else {
			// Backwards compatibility for 1.5.0 / 2.2.0 and before.
			items.deserializeNBT(compound);
			columns = 9;
			rows    = (items.getSlots() / 9);
		}
	}
	
}
