package dev.sapphic.wearablebackpacks.inventory;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;

public final class BackpackContainer implements Inventory {
  private final int size;
  private final DefaultedList<ItemStack> stacks;
  private final ItemStack backpack;

  BackpackContainer(final int size) {
    this.size = size;
    this.stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
    this.backpack = ItemStack.EMPTY;
  }

  public BackpackContainer(final ItemStack stack, final int size) {
    this.size = size;
    this.stacks = DefaultedList.ofSize(this.size, ItemStack.EMPTY);
    this.backpack = stack;
    Inventories.fromTag(stack.getOrCreateSubTag("BlockEntityTag"), this.stacks);
  }

  public boolean hasBackpackItem() {
    return !this.backpack.isEmpty();
  }

  @Override
  public int size() {
    return this.size;
  }

  @Override
  public boolean isEmpty() {
    return this.stacks.stream().allMatch(ItemStack::isEmpty);
  }

  @Override
  public ItemStack getStack(final int slot) {
    if ((slot >= 0) && (slot < this.stacks.size())) {
      return this.stacks.get(slot);
    }
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack removeStack(final int slot, final int amount) {
    final ItemStack stack = Inventories.splitStack(this.stacks, slot, amount);
    if (!stack.isEmpty()) {
      this.markDirty();
    }
    return stack;
  }

  @Override
  public ItemStack removeStack(final int slot) {
    final ItemStack stack = this.stacks.get(slot);
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    this.stacks.set(slot, ItemStack.EMPTY);
    return stack;
  }

  @Override
  public void setStack(final int slot, final ItemStack stack) {
    this.stacks.set(slot, stack);
    if (!stack.isEmpty() && (stack.getCount() > this.getMaxCountPerStack())) {
      stack.setCount(this.getMaxCountPerStack());
    }
    this.markDirty();
  }

  @Override
  public void markDirty() {
  }

  @Override
  public boolean canPlayerUse(final PlayerEntity player) {
    return !this.hasBackpackItem() || (player.getEquippedStack(EquipmentSlot.CHEST) == this.backpack);
  }

  @Override
  public void clear() {
    this.stacks.clear();
    this.markDirty();
  }

  @Override
  public void onClose(final PlayerEntity player) {
    if (this.hasBackpackItem()) {
      Inventories.toTag(this.backpack.getOrCreateSubTag("BlockEntityTag"), this.stacks);
      player.world.playSound(null, player.getX(), player.getY(), player.getZ(),
        SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, player.getSoundCategory(),
        0.5F, (player.world.random.nextFloat() * 0.1F) + 0.9F
      );
    }
  }
}
