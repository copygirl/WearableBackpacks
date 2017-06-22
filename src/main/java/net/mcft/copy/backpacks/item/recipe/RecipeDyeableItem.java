/*
package net.mcft.copy.backpacks.item.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeHooks;

import net.mcft.copy.backpacks.item.IDyeableItem;
import net.mcft.copy.backpacks.misc.util.DyeUtils;
import net.mcft.copy.backpacks.misc.util.NbtUtils;

/** Recipe which handled coloring of dyeable items which implement IDyeableItem. *\/
 * // FIXME: Reimplement dyeing recipe.
public class RecipeDyeableItem implements IRecipe {
	
	@Override
	public int getRecipeSize() { return 10; }
	
	@Override
	public ItemStack getRecipeOutput() { return ItemStack.EMPTY; }
	
	@Override
	public boolean matches(InventoryCrafting crafting, World world) {
		// Check if crafting inventory has:
		// - Exactly one dyeable item.
		// - At least one dye.
		// - No other items.
		boolean hasDyeable = false;
		boolean hasDyes = false;
		for (int i = 0; i < crafting.getSizeInventory(); i++) {
			ItemStack stack = crafting.getStackInSlot(i);
			if (stack.isEmpty()) continue;                                         // Ignore empty stacks.
			else if (DyeUtils.isDye(stack)) hasDyes = true;                        // Check for dyes.
			else if (!(stack.getItem() instanceof IDyeableItem)) return false;     // Don't allow non-dyeable items.
			else if (!((IDyeableItem)stack.getItem()).canDye(stack)) return false; // canDye has to return true, too.
			else if (hasDyeable) return false;                                     // Check if we already have one.
			else hasDyeable = true;                                                // Item is dyeable.
		}
		return (hasDyeable && hasDyes);
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting crafting) {
		// Collect dyeable item and dyes.
		ItemStack dyeable = ItemStack.EMPTY;;
		List<ItemStack> dyes = new ArrayList<ItemStack>();
		for (int i = 0; i < crafting.getSizeInventory(); i++) {
			ItemStack stack = crafting.getStackInSlot(i);
			if (stack.isEmpty()) continue;
			else if (DyeUtils.isDye(stack)) dyes.add(stack);
			else if (!(stack.getItem() instanceof IDyeableItem)) return ItemStack.EMPTY;
			else if (!((IDyeableItem)stack.getItem()).canDye(stack)) return ItemStack.EMPTY;
			else if (!dyeable.isEmpty()) return ItemStack.EMPTY;
			else dyeable = stack.copy();
		}
		if (dyes.isEmpty()) return ItemStack.EMPTY;
		// Caclulate and set resulting item's color.
		int oldColor = NbtUtils.get(dyeable, -1, "display", "color");
		int newColor = DyeUtils.getColorFromDyes(oldColor, dyes);
		NbtUtils.set(dyeable, newColor, "display", "color");
		return dyeable;
	}
	
	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> list = NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);

		for (int i = 0; i < list.size(); ++i) {
			ItemStack stack = inv.getStackInSlot(i);
			list.set(i, ForgeHooks.getContainerItem(stack));
		}

		return list;
	}
}
*/
