package dev.sapphic.wearablebackpacks.inventory;

import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import dev.sapphic.wearablebackpacks.mixin.MenuTypeAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public final class BackpackMenu extends ScreenHandler {
  private static final int HOTBAR_ROWS = 1;
  private static final int HOTBAR_COLS = 9;

  private static final int INVENTORY_ROWS = 3;
  private static final int INVENTORY_COLS = 9;

  private static final int HOTBAR_SLOTS = HOTBAR_COLS * HOTBAR_ROWS;
  private static final int INVENTORY_SLOTS = INVENTORY_COLS * INVENTORY_ROWS;

  private static final int TITLE_PADDING = 18;
  private static final int SLOT_DIMENSIONS = 18;

  public static final ScreenHandlerType<BackpackMenu> TYPE =
    MenuTypeAccessor.callCreate((id, inventory) -> { throw new AssertionError(); });

  private final int rows;
  private final int columns;
  private final Inventory backpack;

  public BackpackMenu(final int containerId, final PlayerInventory inventory, final int rows, final int columns) {
    this(containerId, inventory, new BackpackContainer(rows * columns), rows, columns);
  }

  public BackpackMenu(
    final int containerId, final PlayerInventory inventory, final ItemStack backpack, final int rows, final int columns
  ) {
    this(containerId, inventory, new BackpackContainer(backpack, rows * columns), rows, columns);
  }

  public BackpackMenu(final int containerId, final PlayerInventory inventory, final BackpackBlockEntity backpack) {
    this(containerId, inventory, backpack, backpack.getRows(), backpack.getColumns());
  }

  public BackpackMenu(
    final int containerId, final PlayerInventory inventory, final Inventory backpack, final int rows, final int columns
  ) {
    super(TYPE, containerId);
    checkSize(backpack, rows * columns);
    backpack.onOpen(inventory.player);

    this.rows = rows;
    this.columns = columns;
    this.backpack = backpack;

    for (int column = 0; column < this.columns; ++column) {
      for (int row = 0; row < this.rows; ++row) {
        final int index = column + (row * this.columns);
        final int x = 8 + (SLOT_DIMENSIONS * column);
        final int y = 18 + (SLOT_DIMENSIONS * row);
        this.addSlot(new BackpackSlot(backpack, index, x, y));
      }
    }

    final int yOffset = (this.rows - 4) * 18;

    // TODO Fix magic numbers

    for(int row = 0; row < 3; ++row) {
      for(int column = 0; column < 9; ++column) {
        final int index = column + (row * 9) + 9;
        final int x = 8 + (column * 18);
        final int y = 103 + (row * 18) + yOffset;
        this.addSlot(new Slot(inventory, index, x, y));
      }
    }

    for(int column = 0; column < 9; ++column) {
      final int x = 8 + (column * 18);
      final int y = 161 + yOffset;
      this.addSlot(new Slot(inventory, column, x, y));
    }
  }

  public int getRows() {
    return this.rows;
  }

  public int getColumns() {
    return this.columns;
  }

  @Override
  public ItemStack transferSlot(final PlayerEntity player, final int index) {
    ItemStack original = ItemStack.EMPTY;
    final Slot slot = this.slots.get(index);
    if ((slot != null) && slot.hasStack()) {
      final ItemStack stack = slot.getStack();
      original = stack.copy();
      if (index < HOTBAR_SLOTS) {
        if (!this.insertItem(stack, HOTBAR_SLOTS, this.getSlotCount(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.insertItem(stack, 0, HOTBAR_SLOTS, false)) {
        return ItemStack.EMPTY;
      }
      if (stack.isEmpty()) {
        slot.setStack(ItemStack.EMPTY);
      } else {
        slot.markDirty();
      }
      if (stack.getCount() == original.getCount()) {
        return ItemStack.EMPTY;
      }
      slot.onTakeItem(player, stack);
    }
    return original;
  }

  @Override
  public void close(final PlayerEntity player) {
    super.close(player);
    this.backpack.onClose(player);
  }

  @Override
  public boolean canUse(final PlayerEntity player) {
    return this.backpack.canPlayerUse(player);
  }

  private int getSlotCount() {
    return HOTBAR_SLOTS + INVENTORY_SLOTS + (this.columns * this.rows);
  }
}
