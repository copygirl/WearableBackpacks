package net.mcft.copy.backpacks.misc;

import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.core.util.NbtUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;

public class BackpackDataItems implements IBackpackData {
	
	public static final String TAG_ITEMS = "items";
	
	public final ItemStack[] items;
	
	public BackpackDataItems(int size) {
		items = new ItemStack[size];
	}
	
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setTag(TAG_ITEMS, NbtUtils.writeItems(items));
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		// TODO: Capture invalid items and have them drop or similar?
		NbtUtils.readItems(compound.getTagList(TAG_ITEMS, NBT.TAG_COMPOUND), items);
	}
	
}
