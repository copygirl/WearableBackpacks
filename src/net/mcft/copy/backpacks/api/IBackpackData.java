package net.mcft.copy.backpacks.api;

import net.minecraft.nbt.NBTTagCompound;

public interface IBackpackData {
	
	/** Writes the backpack data to an NBT compound and returns it. */
	public NBTTagCompound writeToNBT(NBTTagCompound compound);
	
	/** Reads the backpack data from an NBT compound. */
	public void readFromNBT(NBTTagCompound compound);
	
}
