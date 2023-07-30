package dev.sapphic.wearablebackpacks.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public final class BackpackMenu extends ScreenHandler {
    public static final int HOTBAR_ROWS = 1;
    public static final int HOTBAR_COLS = 9;

    public static final int INVENTORY_ROWS = 3;
    public static final int INVENTORY_COLS = 9;

    public static final int HOTBAR_SLOTS = HOTBAR_COLS * HOTBAR_ROWS;
    public static final int INVENTORY_SLOTS = INVENTORY_COLS * INVENTORY_ROWS;

    public static final int TITLE_PADDING = 18;
    public static final int SLOT_DIMENSIONS = 18;

    public static final int THIN_EDGE = 8;
    private final BackpackContainer backpack;

    public BackpackMenu(final int containerId, final PlayerInventory inventory) {
        this(containerId, inventory, new WornBackpack());
    }

    public BackpackMenu(final int containerId, final PlayerInventory inventory, final BackpackContainer backpack) {
        super(TYPE, containerId);
        final int rows = backpack.getRows();
        final int columns = backpack.getColumns();

        backpack.onOpen(inventory.player);

        this.backpack = backpack;

        final int backpackXOffset;
        final int playerXOffset;

        if (columns > 9) {
            backpackXOffset = 0;
            playerXOffset = (18 * (columns - 9)) / 2;
        } else {
            backpackXOffset = (18 * (9 - columns)) / 2;
            playerXOffset = 0;
        }
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                final int index = column + (row * columns);
                final int x = backpackXOffset + THIN_EDGE + (SLOT_DIMENSIONS * column);
                final int y = 18 + (SLOT_DIMENSIONS * row);
                this.addSlot(new BackpackSlot(backpack, index, x, y));
            }
        }

        final int yOffset = (rows - 4) * 18;

        // TODO Fix magic numbers

        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                final int index = column + (row * 9) + 9;
                final int x = playerXOffset + 8 + (column * 18);
                final int y = 103 + (row * 18) + yOffset;
                this.addSlot(new Slot(inventory, index, x, y));
            }
        }

        for (int column = 0; column < 9; ++column) {
            final int x = playerXOffset + 8 + (column * 18);
            final int y = 161 + yOffset;
            this.addSlot(new Slot(inventory, column, x, y));
        }
    }

    public static final ScreenHandlerType<BackpackMenu> TYPE = new ScreenHandlerType<>(BackpackMenu::new);

    public int getRows() {
        return this.backpack.getRows();
    }

    public int getColumns() {
        return this.backpack.getColumns();
    }

    private int getSlotCount() {
        return HOTBAR_SLOTS + INVENTORY_SLOTS + (this.getColumns() * this.getRows());
    }

    @Override
    public ItemStack transferSlot(final PlayerEntity player, final int index) {
        ItemStack original = ItemStack.EMPTY;
        final Slot slot = this.slots.get(index);
        if ((slot != null) && slot.hasStack()) {
            final ItemStack stack = slot.getStack();
            original = stack.copy();
            if (index < (this.getColumns() * this.getRows())) {
                if (!this.insertItem(stack, this.getColumns() * this.getRows(), this.getSlotCount(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(stack, 0, this.getColumns() * this.getRows(), false)) {
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


}
