package net.mcft.copy.backpacks.misc;

import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.core.util.NbtUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;

public class BackpackDataItems implements IBackpackData {
	
	public final ItemStack[] items;
	
	public BackpackDataItems(int size) {
		items = new ItemStack[size];
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("items", NbtUtils.writeItems(items));
		return compound;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		NbtUtils.readItems(compound.getTagList("items", NBT.TAG_COMPOUND), items);
	}
	
}
