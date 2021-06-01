package dev.sapphic.wearablebackpacks.block.entity;

import dev.sapphic.wearablebackpacks.Backpack;
import dev.sapphic.wearablebackpacks.BackpackOptions;
import dev.sapphic.wearablebackpacks.Backpacks;
import dev.sapphic.wearablebackpacks.block.BackpackBlock;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public final class BackpackBlockEntity extends LootableContainerBlockEntity
  implements Backpack, Tickable, BlockEntityClientSerializable, BackpackContainer {
  private static final int NO_DAMAGE = 0;
  private static final int NO_COLOR = 0xFFFFFF + 1;

  private static final float CLOSED_DELTA = 0.0F;
  private static final float OPENED_DELTA = 1.0F;
  private static final float DELTA_STEP = 0.2F;

  private int rows = Backpack.getExpectedRows();
  private int columns = Backpack.getExpectedColumns();
  private DefaultedList<ItemStack> contents = this.createContents();

  private int damage = NO_DAMAGE;
  private int color = NO_COLOR;
  private int lastColor = NO_COLOR;

  private LidState lidState = LidState.CLOSED;
  private float lidDelta = 0.0F;
  private float lastLidDelta = 0.0F;

  private int openCount = 0;

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
  public int getDamage() {
    return this.damage;
  }

  @Override
  public int getColor() {
    return this.hasColor() ? this.color : DEFAULT_COLOR;
  }

  @Override
  public boolean hasColor() {
    return this.color != NO_COLOR;
  }

  @Override
  public void setColor(final int color) {
    this.lastColor = this.color;
    this.color = color & 0xFFFFFF;
    if (this.lastColor != this.color) {
      this.markDirty();
      this.trySync();
    }
  }

  @Override
  public void clearColor() {
    this.lastColor = this.color;
    this.color = NO_COLOR;
    if (this.lastColor != this.color) {
      this.markDirty();
      this.trySync();
    }
  }

  @Override
  public DefaultedList<ItemStack> getContents() {
    return this.contents;
  }

  public Direction getFacing() {
    if (this.hasWorld()) {
      final BlockState state = this.getCachedState();
      if (state.getBlock() instanceof BackpackBlock) {
        return state.get(BackpackBlock.FACING);
      }
    }
    return Direction.NORTH;
  }

  @Override
  protected DefaultedList<ItemStack> getInvStackList() {
    return this.contents;
  }

  @Override
  @Deprecated
  protected void setInvStackList(final DefaultedList<ItemStack> inventory) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return this.rows * this.columns;
  }

  @Override
  public void onOpen(final PlayerEntity player) {
    if ((this.world != null) && !player.isSpectator()) {
      if (this.openCount < 0) {
        this.openCount = 0;
      }
      ++this.openCount;
      this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), 1, this.openCount);
      if (this.openCount == 1) {
        this.world.playSound(null, this.pos, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
          SoundCategory.BLOCKS, 0.5F, (this.world.random.nextFloat() * 0.1F) + 0.9F
        );
      }
    }
  }

  @Override
  public void onClose(final PlayerEntity player) {
    if ((this.world != null) && !player.isSpectator()) {
      --this.openCount;
      this.world.addSyncedBlockEvent(this.pos, this.getCachedState().getBlock(), 1, this.openCount);
      if (this.openCount <= 0) {
        this.world.playSound(null, this.pos, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
          SoundCategory.BLOCKS, 0.5F, (this.world.random.nextFloat() * 0.1F) + 0.9F
        );
      }
    }
  }

  public void saveTo(final ItemStack stack) {
    stack.setDamage(this.damage);
    Inventories.toTag(stack.getOrCreateSubTag("BlockEntityTag"), this.contents);
    if (this.hasColor()) {
      Backpack.setColor(stack, this.getColor());
    }
    if (this.hasCustomName()) {
      stack.setCustomName(this.getCustomName());
    }
  }

  public void loadFrom(final ItemStack stack) {
    this.damage = BackpackOptions.getDamage(stack.getDamage());
    Inventories.fromTag(stack.getOrCreateSubTag("BlockEntityTag"), this.contents);
    if (Backpack.hasColor(stack)) {
      this.setColor(Backpack.getColor(stack));
    }
    if (stack.hasCustomName()) {
      this.setCustomName(stack.getName());
    }
  }

  @Override
  public void tick() {
    this.lidTick();
  }

  @Override
  public boolean onSyncedBlockEvent(final int type, final int data) {
    if (type == 1) {
      this.openCount = data;
      if (data == 0) {
        this.lidState = LidState.CLOSING;
      }
      if (data == 1) {
        this.lidState = LidState.OPENING;
      }
      return true;
    }
    return super.onSyncedBlockEvent(type, data);
  }

  @Override
  public void fromTag(final BlockState state, final CompoundTag nbt) {
    super.fromTag(state, nbt);
    this.loadDimensions(nbt);
    this.loadInventory(nbt);
    this.loadDamage(nbt);
    this.loadColor(nbt);
  }

  @Override
  public CompoundTag toTag(final CompoundTag nbt) {
    super.toTag(nbt);
    this.saveDimensions(nbt);
    this.saveInventory(nbt);
    this.saveDamage(nbt);
    this.saveColor(nbt);
    return nbt;
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
  public void fromClientTag(final CompoundTag nbt) {
    this.loadColor(nbt);
    this.loadDamage(nbt);
  }

  @Override
  public CompoundTag toClientTag(final CompoundTag nbt) {
    this.saveColor(nbt);
    this.saveDamage(nbt);
    return nbt;
  }

  public float getLidDelta(final float tickDelta) {
    return MathHelper.lerp(tickDelta, this.lastLidDelta, this.lidDelta);
  }

  private void trySync() {
    if (this.world instanceof ServerWorld) {
      this.sync();
    }
  }

  private DefaultedList<ItemStack> createContents() {
    return DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
  }

  private void loadDimensions(final CompoundTag nbt) {
    this.rows = BackpackOptions.getRows(nbt.getInt(Backpack.ROWS));
    this.columns = BackpackOptions.getColumns(nbt.getInt(Backpack.COLUMNS));
  }

  private void saveDimensions(final CompoundTag nbt) {
    nbt.putInt(Backpack.ROWS, this.rows);
    nbt.putInt(Backpack.COLUMNS, this.columns);
  }

  private void loadInventory(final CompoundTag nbt) {
    this.contents = this.createContents();
    if (!this.deserializeLootTable(nbt)) {
      Inventories.fromTag(nbt, ((Backpack) this).getContents());
    }
  }

  private void saveInventory(final CompoundTag nbt) {
    if (!this.serializeLootTable(nbt)) {
      Inventories.toTag(nbt, this.contents);
    }
  }

  private void loadDamage(final CompoundTag nbt) {
    this.damage = BackpackOptions.getDamage(nbt.getInt(Backpack.DAMAGE));
  }

  private void saveDamage(final CompoundTag nbt) {
    nbt.putInt(Backpack.DAMAGE, this.damage);
  }

  private void loadColor(final CompoundTag nbt) {
    if (nbt.contains(Backpack.COLOR, NbtType.INT)) {
      this.color = nbt.getInt(Backpack.COLOR) & 0xFFFFFF;
    } else {
      this.color = NO_COLOR;
    }
  }

  private void saveColor(final CompoundTag nbt) {
    if (this.hasColor()) {
      nbt.putInt(Backpack.COLOR, this.color);
    }
  }

  private void lidTick() {
    this.lastLidDelta = this.lidDelta;
    switch (this.lidState) {
      case CLOSED:
        this.lidDelta = CLOSED_DELTA;
        break;
      case OPENING:
        this.lidDelta += DELTA_STEP;
        if (this.lidDelta >= OPENED_DELTA) {
          this.lidState = LidState.OPENED;
          this.lidDelta = OPENED_DELTA;
        }
        break;
      case CLOSING:
        this.lidDelta -= DELTA_STEP;
        if (this.lidDelta <= CLOSED_DELTA) {
          this.lidState = LidState.CLOSED;
          this.lidDelta = CLOSED_DELTA;
        }
        break;
      case OPENED:
        this.lidDelta = OPENED_DELTA;
    }
  }

  private enum LidState {
    CLOSED, OPENING, OPENED, CLOSING
  }
}
