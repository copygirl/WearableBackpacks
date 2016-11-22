package net.mcft.copy.backpacks.api;

import net.minecraft.item.ItemStack;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface IBackpack {
	
	@CapabilityInject(IBackpack.class)
	public static Capability<IBackpack> CAPABILITY = null;
	
	ItemStack getStack();
	void setStack(ItemStack value);
	
	IBackpackData getData();
	void setData(IBackpackData value);
	
	int getPlayersUsing();
	void setPlayersUsing(int value);
	
	int getLidTicks();
	int getPrevLidTicks();
	void setLidTicks(int value);
	
	default IBackpackType getType() {
		return BackpackHelper.getBackpackType(getStack());
	}
	
}
