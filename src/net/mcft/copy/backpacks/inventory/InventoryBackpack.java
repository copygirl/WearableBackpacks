package net.mcft.copy.backpacks.inventory;

import net.mcft.copy.backpacks.api.IBackpackProperties;
import net.mcft.copy.backpacks.client.BackpackLocalization;
import net.mcft.copy.backpacks.misc.BackpackDataItems;
import net.mcft.copy.core.inventory.InventoryStacks;
import net.minecraft.entity.player.EntityPlayer;

public abstract class InventoryBackpack extends InventoryStacks {
	
	public final IBackpackProperties properties;
	
	public InventoryBackpack(IBackpackProperties properties) {
		super(((BackpackDataItems)properties.getBackpackData()).items);
		this.properties = properties;
	}
	
	@Override
	public String getInventoryName() {
		return (properties.getBackpackStack().hasDisplayName()
				? properties.getBackpackStack().getDisplayName()
				: BackpackLocalization.CONTAINER_BACKPACK);
	}
	
	@Override
	public boolean hasCustomInventoryName() {
		return properties.getBackpackStack().hasDisplayName();
	}
	
	@Override
	public abstract boolean isUseableByPlayer(EntityPlayer player);
	
	@Override
	public void openInventory() {
		properties.setPlayersUsing(properties.getPlayersUsing() + 1);
	}
	
	@Override
	public void closeInventory() {
		properties.setPlayersUsing(properties.getPlayersUsing() - 1);
	}
	
}
