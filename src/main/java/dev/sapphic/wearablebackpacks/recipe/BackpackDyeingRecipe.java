package dev.sapphic.wearablebackpacks.recipe;

import dev.sapphic.wearablebackpacks.Backpacks;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Technically we could just be using Mojang's armor dyeing implementation here
 * but it's safer to create our own implementation to avoid conflict if they ever
 * change their recipe semantics. The type checking of the backpack item has also
 * been made stricter, migrating from {@link DyeableItem} comparisons
 * to {@link BackpackItem} comparisons.
 */
public final class BackpackDyeingRecipe extends SpecialCraftingRecipe {
public static final Identifier ID = new Identifier(Backpacks.ID, "backpack_dyeing");

public static final SpecialRecipeSerializer<BackpackDyeingRecipe> SERIALIZER =
        new SpecialRecipeSerializer<>(BackpackDyeingRecipe::new);

private BackpackDyeingRecipe(final Identifier id) {
  super(id);
}

@Override
public boolean matches(final CraftingInventory matrix, final World world) {
  ItemStack backpack = ItemStack.EMPTY;
  final Collection<ItemStack> dyes = new ArrayList<>(1);
  for (int slot = 0; slot < matrix.size(); ++slot) {
    final ItemStack stack = matrix.getStack(slot);
    if (!stack.isEmpty()) {
      if (stack.getItem() instanceof BackpackItem) {
        if (!backpack.isEmpty()) {
          return false;
        }
        backpack = stack;
      } else {
        if (!(stack.getItem() instanceof DyeItem)) {
          return false;
        }
        dyes.add(stack);
      }
    }
  }
  return !backpack.isEmpty() && !dyes.isEmpty();
}

@Override
public ItemStack craft(final CraftingInventory matrix) {
  ItemStack backpack = ItemStack.EMPTY;
  final List<DyeItem> dyes = new ArrayList<>(1);
  for (int slot = 0; slot < matrix.size(); ++slot) {
    final ItemStack stack = matrix.getStack(slot);
    if (!stack.isEmpty()) {
      final Item item = stack.getItem();
      if (item instanceof BackpackItem) {
        if (!backpack.isEmpty()) {
          return ItemStack.EMPTY;
        }
        backpack = stack.copy();
      } else {
        if (!(item instanceof DyeItem)) {
          return ItemStack.EMPTY;
        }
        dyes.add((DyeItem) item);
      }
    }
  }
  if (!backpack.isEmpty() && !dyes.isEmpty()) {
    final ItemStack result = DyeableItem.blendAndSetColor(backpack, dyes);
    if (!result.isEmpty()) {
      return result;
    }
  }
  return ItemStack.EMPTY;
}

@Override
@Environment(EnvType.CLIENT)
public boolean fits(final int width, final int height) {
  return (width * height) >= 2;
}

@Override
public RecipeSerializer<?> getSerializer() {
  return SERIALIZER;
}
}

