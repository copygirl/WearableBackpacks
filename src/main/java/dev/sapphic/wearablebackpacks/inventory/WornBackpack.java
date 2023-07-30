package dev.sapphic.wearablebackpacks.inventory;

import dev.sapphic.wearablebackpacks.Backpack;
import dev.sapphic.wearablebackpacks.Backpacks;
import dev.sapphic.wearablebackpacks.client.BackpackWearer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

public final class WornBackpack implements BackpackContainer {
private final int rows;
private final int columns;

private final @Nullable LivingEntity wearer;
private final ItemStack backpack;

private final DefaultedList<ItemStack> contents;

public WornBackpack(final @Nullable LivingEntity wearer, final ItemStack backpack) {
  this.rows = Backpack.getRows(backpack);
  this.columns = Backpack.getColumns(backpack);
  this.contents = DefaultedList.ofSize(this.rows * this.columns, ItemStack.EMPTY);
  this.wearer = wearer;
  this.backpack = backpack;
  final @Nullable NbtCompound nbt = backpack.getSubNbt("BlockEntityTag");
  if (nbt != null) {
    Inventories.readNbt(nbt, this.contents);
  }
}

WornBackpack() {
  this(null, ItemStack.EMPTY);
}

public static NamedScreenHandlerFactory of(final LivingEntity wearer, final ItemStack backpack) {
  return new NamedScreenHandlerFactory() {
    @Override
    public Text getDisplayName() {
      return backpack.hasCustomName() ? backpack.getName() : Text.translatable("container." + Backpacks.ID);
    }

    @Override
    public ScreenHandler createMenu(
            final int containerId, final PlayerInventory inventory, final PlayerEntity player
    ) {
      return new BackpackMenu(containerId, inventory, new WornBackpack(wearer, backpack));
    }
  };
}

@Override
public int getRows() {
  return this.rows;
}

@Override
public int getColumns() {
  return this.columns;
}

@Override
public int size() {
  return this.rows * this.columns;
}

@Override
public boolean isEmpty() {
  for (final ItemStack stack : this.contents) {
    if (!stack.isEmpty()) {
      return false;
    }
  }
  return true;
}

@Override
public ItemStack getStack(final int slot) {
  if ((slot >= 0) && (slot < this.contents.size())) {
    return this.contents.get(slot);
  }
  return ItemStack.EMPTY;
}

@Override
public ItemStack removeStack(final int slot, final int amount) {
  final ItemStack stack = Inventories.splitStack(this.contents, slot, amount);
  if (!stack.isEmpty()) {
    this.markDirty();
  }
  return stack;
}

@Override
public ItemStack removeStack(final int slot) {
  final ItemStack stack = this.contents.get(slot);
  if (stack.isEmpty()) {
    return ItemStack.EMPTY;
  }
  this.contents.set(slot, ItemStack.EMPTY);
  this.markDirty();
  return stack;
}

@Override
public void setStack(final int slot, final ItemStack stack) {
  this.contents.set(slot, stack);
  if (!stack.isEmpty() && (stack.getCount() > this.getMaxCountPerStack())) {
    stack.setCount(this.getMaxCountPerStack());
  }
  this.markDirty();
}

@Override
public void markDirty() {
  if (!this.isEmpty()) {
    Inventories.writeNbt(this.backpack.getOrCreateSubNbt("BlockEntityTag"), this.contents);
  } else {
    this.backpack.removeSubNbt("BlockEntityTag");
  }
}

@Override
public boolean canPlayerUse(final PlayerEntity player) {
  if ((player == this.wearer) || ((this.wearer != null) && (player.squaredDistanceTo(this.wearer) <= 64.0))) {
    return this.wearer.getEquippedStack(EquipmentSlot.CHEST) == this.backpack;
  }
  return false;
}

@Override
public void onClose(final PlayerEntity player) {
  this.markDirty();
  final LivingEntity source = (this.wearer != null) ? this.wearer : player;
  if (!source.world.isClient) {
    source.world.playSound(null, source.getX(), source.getY(), source.getZ(),
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, source.getSoundCategory(),
            0.5F, (source.world.random.nextFloat() * 0.1F) + 0.9F
    );
    BackpackWearer.getBackpackState(source).closed();
  }
}

@Override
public void clear() {
  this.contents.clear();
  this.markDirty();
}
}
