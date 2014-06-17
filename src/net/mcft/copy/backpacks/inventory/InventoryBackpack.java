package net.mcft.copy.backpacks.inventory;

import net.mcft.copy.backpacks.client.BackpackLocalization;
import net.mcft.copy.backpacks.misc.BackpackDataItems;
import net.mcft.copy.core.inventory.InventoryStacks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public abstract class InventoryBackpack extends InventoryStacks {
	
	public final ItemStack backpack;
	public final BackpackDataItems data;
	
	public InventoryBackpack(ItemStack backpack, BackpackDataItems data) {
		super(data.items);
		this.backpack = backpack;
		this.data = data;
	}
	
	@Override
	public String getInventoryName() {
		return (backpack.hasDisplayName() ? backpack.getDisplayName()
		                                  : BackpackLocalization.CONTAINER_BACKPACK);
	}
	
	@Override
	public boolean hasCustomInventoryName() { return backpack.hasDisplayName(); }
	
	@Override
	public abstract boolean isUseableByPlayer(EntityPlayer player);
	
}
