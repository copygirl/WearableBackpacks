package net.mcft.copy.backpacks.item.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeHooks;

import net.mcft.copy.backpacks.misc.util.DyeUtils;
import net.mcft.copy.backpacks.misc.util.NbtUtils;

public class RecipeDyeableItem implements IRecipe {
	
	@Override
	public int getRecipeSize() { return 10; }
	
	@Override
	public ItemStack getRecipeOutput() { return null; }
	
	@Override
	public boolean matches(InventoryCrafting crafting, World world) {
		boolean hasArmor = false;
		boolean hasDyes = false;
		for (int i = 0; i < crafting.getSizeInventory(); i++) {
			ItemStack stack = crafting.getStackInSlot(i);
			if (stack == null) continue;
			IDyeableItem dyeable = ((stack.getItem() instanceof IDyeableItem)
					? (IDyeableItem)stack.getItem() : null);
			if ((dyeable != null) && dyeable.canDye(stack)) {
				if (hasArmor) return false;
				hasArmor = true;
			} else if (DyeUtils.isDye(stack)) hasDyes = true;
			else return false;
		}
		return (hasArmor && hasDyes);
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting crafting) {
		ItemStack armor = null;
		IDyeableItem dyeable = null;
		List<ItemStack> dyes = new ArrayList<ItemStack>();
		for (int i = 0; i < crafting.getSizeInventory(); i++) {
			ItemStack stack = crafting.getStackInSlot(i);
			if (stack == null) continue;
			dyeable = ((stack.getItem() instanceof IDyeableItem)
					? (IDyeableItem)stack.getItem() : null);
			if ((dyeable != null) && dyeable.canDye(stack)) {
				if (armor != null) return null;
				armor = stack.copy();
			} else if (DyeUtils.isDye(stack)) dyes.add(stack);
			else return null;
		}
		if (dyes.isEmpty()) return null;
		int oldColor = NbtUtils.get(armor, -1, "display", "color");
		int newColor = DyeUtils.getColorFromDyes(oldColor, dyes);
		NbtUtils.set(armor, newColor, "display", "color");
		return armor;
	}
	
	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv) {
		ItemStack[] stacks = new ItemStack[inv.getSizeInventory()];
		for (int i = 0; i < stacks.length; ++i) {
			ItemStack stack = inv.getStackInSlot(i);
			stacks[i] = ForgeHooks.getContainerItem(stack);
		}
		return stacks;
	}
	
}