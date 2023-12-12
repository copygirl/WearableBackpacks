package dev.sapphic.wearablebackpacks.inventory;

import dev.sapphic.wearablebackpacks.item.BackpackItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

final class BackpackSlot extends Slot {
  BackpackSlot(final Inventory container, final int index, final int x, final int y) {
    super(container, index, x, y);
  }
  
  @Override
  public boolean canInsert(final ItemStack stack) {
    return !(stack.getItem() instanceof BackpackItem);
  }
}
