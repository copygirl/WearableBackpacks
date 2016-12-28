package net.mcft.copy.backpacks.item.recipe;

import net.minecraft.item.ItemStack;

// TODO: Allow dyeable items to be cleaned in a cauldron.
/** Interface for items which may be dyed. Used by RecipeDyeableItem. */
public interface IDyeableItem {
	
	/** Returns if the item can be dyed, */
	public boolean canDye(ItemStack stack);
	
}