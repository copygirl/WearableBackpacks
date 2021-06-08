package dev.sapphic.wearablebackpacks;

import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Backpack {
  String ROWS = "Rows";
  String COLUMNS = "Columns";
  String DAMAGE = "Damage";
  String COLOR = "Color";
  String EMPTY = "Empty";

  int DEFAULT_COLOR = 0xA06540;

  static int getExpectedRows() {
    return BackpackOptions.getRows();
  }

  static int getExpectedColumns() {
    return BackpackOptions.getColumns();
  }

  static int getMaxDamage() {
    return BackpackOptions.getMaxDamage();
  }

  static int getDefense() {
    return BackpackOptions.getDefense();
  }

  static float getToughness() {
    return BackpackOptions.getToughness();
  }

  int getRows();

  int getColumns();

  int getDamage();

  int getColor();

  boolean hasColor();

  void setColor(final int color);

  void clearColor();

  DefaultedList<ItemStack> getContents();

  static int getRows(final ItemStack backpack) {
    final CompoundTag nbt = backpack.getOrCreateSubTag("BlockEntityTag");
    if (nbt.contains(ROWS, NbtType.INT)) {
      return BackpackOptions.getRows(nbt.getInt(ROWS));
    }
    return getExpectedRows();
  }

  static int getColumns(final ItemStack backpack) {
    final CompoundTag nbt = backpack.getOrCreateSubTag("BlockEntityTag");
    if (nbt.contains(COLUMNS, NbtType.INT)) {
      return BackpackOptions.getColumns(nbt.getInt(COLUMNS));
    }
    return getExpectedColumns();
  }

  static int getColor(final ItemStack stack) {
    if (stack.getItem() instanceof BackpackItem) {
      return ((DyeableItem) stack.getItem()).getColor(stack) & 0xFFFFFF;
    }
    return DEFAULT_COLOR;
  }

  static boolean hasColor(final ItemStack stack) {
    if (stack.getItem() instanceof BackpackItem) {
      return ((DyeableItem) stack.getItem()).hasColor(stack);
    }
    return false;
  }

  static void setColor(final ItemStack stack, final int color) {
    if (stack.getItem() instanceof BackpackItem) {
      ((DyeableItem) stack.getItem()).setColor(stack, color & 0xFFFFFF);
    }
  }

  static int getColor(final @Nullable BlockView world, final @Nullable BlockPos pos) {
    if ((world != null) && (pos != null)) {
      final @Nullable BlockEntity be = world.getBlockEntity(pos);
      if (be instanceof BackpackBlockEntity) {
        return ((Backpack) be).getColor();
      }
    }
    return DEFAULT_COLOR;
  }
}
