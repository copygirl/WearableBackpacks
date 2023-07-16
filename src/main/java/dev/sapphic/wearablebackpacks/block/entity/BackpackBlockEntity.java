package dev.sapphic.wearablebackpacks.block.entity;

import dev.sapphic.wearablebackpacks.Backpack;
import dev.sapphic.wearablebackpacks.BackpackOptions;
import dev.sapphic.wearablebackpacks.Backpacks;
import dev.sapphic.wearablebackpacks.client.BackpackLid;
import dev.sapphic.wearablebackpacks.inventory.BackpackContainer;
import dev.sapphic.wearablebackpacks.inventory.BackpackMenu;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import org.apache.logging.log4j.LogManager;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BackpackBlockEntity extends LootableContainerBlockEntity implements
        BlockEntityClientSerializable, Backpack, BackpackContainer, Tickable {

    private static final int OPENS_DATA_TYPE = 0x0;
    private static final int COLOR_DATA_TYPE = 0x1;
    private static final int EMPTY_FLAG_TYPE = 0x2;
    private static final int GLINT_FLAG_TYPE = 0x3;

    private static final int DEFAULT_COLOR = 0xA06540;
    private static final int NO_COLOR = 0xFFFFFF + 1;

    private final BackpackLid lid = new BackpackLid(o -> this.event(OPENS_DATA_TYPE, o.openCount()));

    private int rows = BackpackOptions.DEFAULT_ROWS;
    private int columns = BackpackOptions.DEFAULT_COLUMNS;
    private @MonotonicNonNull DefaultedList<ItemStack> contents;
    private @Nullable NbtList enchantments;

    private int color = NO_COLOR;

    private boolean empty;
    private boolean enchanted;

    public BackpackBlockEntity() {
        super(Backpacks.BLOCK_ENTITY);
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
    public boolean hasGlint() {
        return this.enchanted;
    }

    @Override
    public int getColor() {
        if (this.color == NO_COLOR) {
            return DEFAULT_COLOR;
        }
        return this.color;
    }

    @Override
    public void setColor(final int color) {
        if (this.color != (color & 0xFFFFFF)) {
            this.color = color & 0xFFFFFF;
            this.event(COLOR_DATA_TYPE, this.color);
            this.markDirty();
        }
    }

    @Override
    public boolean hasColor() {
        return this.color != NO_COLOR;
    }

    @Override
    public void clearColor() {
        if (this.color != NO_COLOR) {
            this.color = NO_COLOR;
            this.event(COLOR_DATA_TYPE, NO_COLOR);
            this.markDirty();
        }
    }

    @Override
    public DefaultedList<ItemStack> getContents() {
        return this.contents;
    }

    @Override
    public float getLidDelta(final float tickDelta) {
        return this.lid.lidDelta(tickDelta);
    }

    @Override
    public boolean isEmpty() {
        return this.empty;
    }

    @Override
    public ItemStack removeStack(final int slot, final int amount) {
        final ItemStack stack = super.removeStack(slot, amount);
        this.updateEmptyState();
        return stack;
    }

    @Override
    public ItemStack removeStack(final int slot) {
        final ItemStack stack = super.removeStack(slot);
        this.updateEmptyState();
        return stack;
    }

    @Override
    public void setStack(final int slot, final ItemStack stack) {
        super.setStack(slot, stack);
        this.updateEmptyState();
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return this.contents;
    }

    @Override
    protected void setInvStackList(final DefaultedList<ItemStack> list) {
        throw new UnsupportedOperationException();
    }

    public void readFromStack(final ItemStack stack) {
        this.rows = Backpack.getRows(stack);
        this.columns = Backpack.getColumns(stack);
        this.contents = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        final @Nullable NbtCompound tag = stack.getSubTag("BlockEntityTag");
        if (tag != null) {
            Inventories.readNbt(tag, this.contents);
        }
        if (stack.hasEnchantments()) {
            this.enchantments = stack.getEnchantments();
        }
        if (Backpack.hasColor(stack)) {
            this.color = Backpack.getColor(stack);
        }
        this.enchanted = this.enchantments != null;
        this.empty = super.isEmpty();
        this.event(COLOR_DATA_TYPE, this.color);
        this.event(GLINT_FLAG_TYPE, this.enchanted ? 1 : 0);
        this.event(EMPTY_FLAG_TYPE, this.empty ? 1 : 0);
        this.markDirty();
    }

    public void writeToStack(final ItemStack stack) {
        if (this.hasColor()) {
            Backpack.setColor(stack, this.color);
        }
        if (this.enchantments != null) {
            stack.putSubTag("Enchantments", this.enchantments);
        }
        final NbtCompound tag = stack.getOrCreateSubTag("BlockEntityTag");
        tag.putInt("Rows", this.rows);
        tag.putInt("Columns", this.columns);
    }

    @Override
    public void fromTag(final BlockState state, final NbtCompound tag) {
        super.fromTag(state, tag);
        if (tag.contains("Rows", NbtType.INT)) {
            this.rows = BackpackOptions.getRows(tag.getInt("Rows"));
        }
        if (tag.contains("Columns", NbtType.INT)) {
            this.columns = BackpackOptions.getColumns(tag.getInt("Columns"));
        }
        this.contents = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(tag)) {
            Inventories.readNbt(tag, this.contents);
        }
        this.empty = super.isEmpty();
        if (tag.contains("Color", NbtType.INT)) {
            this.color = tag.getInt("Color") & 0xFFFFFF;
        }
        if (tag.contains("Enchantments", NbtType.LIST)) {
            this.enchantments = tag.getList("Enchantments", NbtType.COMPOUND);
            this.enchanted = true;
        }
    }

    @Override
    public NbtCompound writeNbt(final NbtCompound tag) {
        super.writeNbt(tag);
        tag.putInt(Backpack.ROWS, this.rows);
        tag.putInt(Backpack.COLUMNS, this.columns);
        if (this.contents != null) {
            if (!this.serializeLootTable(tag)) {
                Inventories.writeNbt(tag, this.contents);
            }
        }
        if (this.hasColor()) {
            tag.putInt("Color", this.color);
        }
        if (this.enchantments != null) {
            tag.put("Enchantments", this.enchantments);
        }
        return tag;
    }

    @Override
    protected Text getContainerName() {
        return new TranslatableText("container." + Backpacks.ID);
    }

    @Override
    protected ScreenHandler createScreenHandler(final int id, final PlayerInventory inventory) {
        return new BackpackMenu(id, inventory, this);
    }

    @Override
    public void fromClientTag(final NbtCompound tag) {
        if (tag.contains("Color", NbtType.INT)) {
            this.color = tag.getInt("Color") & 0xFFFFFF;
        }
        this.empty = tag.getBoolean("Empty");
        this.enchanted = tag.getBoolean("Enchanted");
    }

    @Override
    public NbtCompound toClientTag(final NbtCompound tag) {
        if (this.hasColor()) {
            tag.putInt("Color", this.color);
        }
        tag.putBoolean("Empty", this.empty);
        tag.putBoolean("Enchanted", this.enchanted);
        return tag;
    }

    @Override
    public boolean onSyncedBlockEvent(final int type, final int data) {
        if (type == OPENS_DATA_TYPE) {
            this.lid.count(data);
            return true;
        }
        if (type == COLOR_DATA_TYPE) {
            if (data == NO_COLOR) {
                this.color = NO_COLOR;
            } else {
                this.color = data & 0xFFFFFF;
            }
            return true;
        }
        if (type == EMPTY_FLAG_TYPE) {
            this.empty = data == 1;
            return true;
        }
        if (type == GLINT_FLAG_TYPE) {
            this.enchanted = data == 1;
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return this.rows * this.columns;
    }

    @Override
    public void onOpen(final PlayerEntity player) {
        if ((this.world != null) && !player.isSpectator()) {
            this.lid.opened();
            if (this.lid.isOpen()) {
                this.world.playSound(null, this.pos, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
                        SoundCategory.BLOCKS, 0.5F, (this.world.random.nextFloat() * 0.1F) + 0.9F
                );
            }
        }
    }

    @Override
    public void onClose(final PlayerEntity player) {
        if ((this.world != null) && !player.isSpectator()) {
            this.lid.closed();
            if (this.lid.isClosed()) {
                this.world.playSound(null, this.pos, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
                        SoundCategory.BLOCKS, 0.5F, (this.world.random.nextFloat() * 0.1F) + 0.9F
                );
            }
        }
    }

    @Override
    public void tick() {
        this.lid.tick();
    }

    private void updateEmptyState() {
        this.empty = super.isEmpty();
        this.event(EMPTY_FLAG_TYPE, this.empty ? 1 : 0);
    }

    private void event(final int type, final int data) {
        if (this.world instanceof ServerWorld) {
            this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), type, data);
        }
    }
}
