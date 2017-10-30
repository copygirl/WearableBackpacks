package net.mcft.copy.backpacks.api;

import net.minecraft.item.ItemStack;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/** Interface which represents a "fully realized" backpack, consisting
 *  of a backpack stack, backpack data and some additional information
 *  such as the number of players using the backpack and animation info. */
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
	
	
	/** A dummy implementation of IBackpack. */
	public static class Impl implements IBackpack {
		
		protected ItemStack stack    = ItemStack.EMPTY;
		protected IBackpackData data = null;
		protected int playersUsing   = 0;
		protected int lidTicks       = 0;
		protected int prevLidTicks   = 0;
		
		public ItemStack getStack() { return stack; }
		public void setStack(ItemStack value) { stack = value; }
		
		public IBackpackData getData() { return data; }
		public void setData(IBackpackData value) { data = value; }
		
		public int getPlayersUsing() { return playersUsing; }
		public void setPlayersUsing(int value) { playersUsing = value; }
		
		public int getLidTicks() { return lidTicks; }
		public int getPrevLidTicks() { return prevLidTicks; }
		public void setLidTicks(int value)
			{ prevLidTicks = lidTicks; lidTicks = value; }
		
	}
	
}
