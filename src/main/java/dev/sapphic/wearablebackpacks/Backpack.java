package dev.sapphic.wearablebackpacks;

import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public interface Backpack {
  String ROWS = "Rows";
  String COLUMNS = "Columns";
  
  int DEFAULT_COLOR = 0xA06540;
  
  static int getExpectedRows() {
    return Backpacks.config.rows;
  }
  
  static int getExpectedColumns() {
    return Backpacks.config.cols;
  }
  
  static int getMaxDamage() {
    return Backpacks.config.damage;
  }
  
  static int getDefense() {
    return Backpacks.config.defense;
  }
  
  static float getToughness() {
    return Backpacks.config.toughness;
  }
  
  static int getRows(final ItemStack backpack) {
    final @Nullable NbtCompound nbt = backpack.getSubNbt("BlockEntityTag");
    if ((nbt != null) && nbt.contains(ROWS, NbtType.INT)) {
      return BackpackOptions.getRows(nbt.getInt(ROWS));
    }
    return getExpectedRows();
  }
  
  static int getColumns(final ItemStack backpack) {
    final @Nullable NbtCompound nbt = backpack.getSubNbt("BlockEntityTag");
    if ((nbt != null) && nbt.contains(COLUMNS, NbtType.INT)) {
      return BackpackOptions.getColumns(nbt.getInt(COLUMNS));
    }
    return getExpectedColumns();
  }
  
  static int getColor(final ItemStack stack) {
    return ((DyeableItem) stack.getItem()).getColor(stack) & 0xFFFFFF;
  }
  
  static boolean hasColor(final ItemStack stack) {
    return ((DyeableItem) stack.getItem()).hasColor(stack);
  }
  
  static void setColor(final ItemStack stack, final int color) {
    ((DyeableItem) stack.getItem()).setColor(stack, color & 0xFFFFFF);
  }
  
  static void removeColor(final ItemStack stack) {
    ((DyeableItem) stack.getItem()).removeColor(stack);
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
  
  static DefaultedList<ItemStack> getContents(final ItemStack backpack) {
    // Minor optimizations over Inventories#readNbt
    final @Nullable NbtCompound nbt = backpack.getSubNbt("BlockEntityTag");
    
    if ((nbt != null) && nbt.contains("Items", NbtType.LIST)) {
      final NbtList items = nbt.getList("Items", NbtType.COMPOUND);
      final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(items.size(), ItemStack.EMPTY);
      
      for (int index = 0; index < items.size(); index++) {
        final NbtCompound item = items.getCompound(index);
        
        if ((item.getByte("Slot") & 255) < items.size()) {
          stacks.add(ItemStack.fromNbt(item));
        }
      }
      return stacks;
    }
    return DefaultedList.ofSize(0, ItemStack.EMPTY);
  }
  
  static boolean isEmpty(final ItemStack backpack) {
    // Short-circuiting deserialization
    final @Nullable NbtCompound nbt = backpack.getSubNbt("BlockEntityTag");
    
    if ((nbt != null) && nbt.contains("Items", NbtType.LIST)) {
      final NbtList items = nbt.getList("Items", NbtType.COMPOUND);
      
      for (int index = 0; index < items.size(); index++) {
        final NbtCompound item = items.getCompound(index);
        
        if ((item.getByte("Slot") & 255) < items.size()) {
          if (!ItemStack.fromNbt(item).isEmpty()) {
            return false;
          }
        }
      }
    }
    return true;
  }
  
  int getRows();
  
  int getColumns();
  
  boolean hasGlint();
  
  int getColor();
  
  void setColor(final int color);
  
  boolean hasColor();
  
  void clearColor();
  
  DefaultedList<ItemStack> getContents();
  
  float getLidDelta(final float tickDelta);
}
