package net.mcft.copy.backpacks.api;

import net.minecraft.item.ItemStack;

public interface IBackpackTileEntity {
	
	public ItemStack getBackpackStack();
	
	public void setBackpackStack(ItemStack stack);
	
	public IBackpackData getBackpackData();
	
	public void setBackpackData(IBackpackData data);
	
	public boolean isUsedByPlayer();
	
}
