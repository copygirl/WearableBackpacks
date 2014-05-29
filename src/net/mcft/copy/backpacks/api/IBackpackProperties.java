package net.mcft.copy.backpacks.api;

import net.minecraft.item.ItemStack;

public interface IBackpackProperties {
	
	public static final String IDENTIFIER = "WearableBackpack";
	
	public ItemStack getBackpackStack();
	
	public void setBackpackStack(ItemStack stack);
	
	public IBackpackData getBackpackData();
	
	public void setBackpackData(IBackpackData data);
	
	public IBackpack getLastBackpackType();
	
	public void setLastBackpackType(IBackpack type);
	
}
