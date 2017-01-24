package net.mcft.copy.backpacks.item;

import net.minecraft.item.ItemStack;

/** Interface for items which may be dyed and washed.
 *  Used by RecipeDyeableItem and DyeWashingHandler. */
public interface IDyeableItem {
	
	/** Returns if the item can be dyed. */
	default boolean canDye(ItemStack stack) { return true; }
	
	/** Returns if the item's dye can be washed off (using a cauldron). */
	default boolean canWash(ItemStack stack) { return canDye(stack); }
	
}