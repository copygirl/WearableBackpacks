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
		boolean hasDyeable = false;
		boolean hasDyes = false;
		for (int i = 0; i < crafting.getSizeInventory(); i++) {
			ItemStack stack = crafting.getStackInSlot(i);
			if (stack == null) continue;
			else if (DyeUtils.isDye(stack)) hasDyes = true;
			else if (!(stack.getItem() instanceof IDyeableItem)) return false;
			else if (!((IDyeableItem)stack.getItem()).canDye(stack)) return false;
			else if (hasDyeable) return false;
			else hasDyeable = true;
		}
		return (hasDyeable && hasDyes);
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting crafting) {
		ItemStack dyeable = null;
		List<ItemStack> dyes = new ArrayList<ItemStack>();
		for (int i = 0; i < crafting.getSizeInventory(); i++) {
			ItemStack stack = crafting.getStackInSlot(i);
			if (stack == null) continue;
			else if (DyeUtils.isDye(stack)) dyes.add(stack);
			else if (!(stack.getItem() instanceof IDyeableItem)) return null;
			else if (!((IDyeableItem)stack.getItem()).canDye(stack)) return null;
			else if (dyeable != null) return null;
			else dyeable = stack.copy();
		}
		if (dyes.isEmpty()) return null;
		int oldColor = NbtUtils.get(dyeable, -1, "display", "color");
		int newColor = DyeUtils.getColorFromDyes(oldColor, dyes);
		NbtUtils.set(dyeable, newColor, "display", "color");
		return dyeable;
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