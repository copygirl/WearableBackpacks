package dev.sapphic.wearablebackpacks.item;

import dev.sapphic.wearablebackpacks.advancement.BackpackCriteriaTriggers;
import dev.sapphic.wearablebackpacks.block.BackpackBlock;
import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import dev.sapphic.wearablebackpacks.inventory.BackpackMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ModifiableWorld;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class BackpackItem extends DyeableArmorItem {
  private final Block block;

  public BackpackItem(final Block block, final Settings settings) {
    super(BackpackMaterial.INSTANCE, EquipmentSlot.CHEST, settings);
    Validate.isInstanceOf(BackpackBlock.class, block);
    this.block = block;
  }

  private static BlockState getBlockStateFromTag(
    final BlockPos pos, final ModifiableWorld world, final ItemStack stack, final BlockState state
  ) {
    final @Nullable CompoundTag nbt = stack.getSubTag("BlockStateTag");
    if (nbt != null) {
      BlockState actualState = state;
      final StateManager<Block, BlockState> container = state.getBlock().getStateManager();
      for (final String key : nbt.getKeys()) {
        final @Nullable Property<?> property = container.getProperty(key);
        if (property != null) {
          final Tag value = Objects.requireNonNull(nbt.get(key));
          actualState = with(actualState, property, value.asString());
        }
      }
      if (actualState != state) {
        world.setBlockState(pos, actualState, 2); // FIXME Use constant
      }
      return actualState;
    }
    return state;
  }

  private static <T extends Comparable<T>> BlockState with(
    final BlockState state, final Property<T> property, final String value
  ) {
    return property.parse(value).map(v -> state.with(property, v)).orElse(state);
  }

  @Override
  public ActionResult useOnBlock(final ItemUsageContext context) {
    final ActionResult placeResult = this.place(new ItemPlacementContext(context));
    if (placeResult != ActionResult.SUCCESS) {
      return this.use(context.getWorld(), context.getPlayer(), context.getHand()).getResult();
    }
    return placeResult;
  }

  @Override
  public String getTranslationKey() {
    return this.block.getTranslationKey();
  }

  @Override
  public void inventoryTick(
    final ItemStack backpack, final World world, final Entity entity, final int slot, final boolean selected
  ) {
    super.inventoryTick(backpack, world, entity, slot, selected);
    if (!(entity instanceof PlayerEntity)) {
      return;
    }
    if (!world.isClient) {
      if (slot == EquipmentSlot.CHEST.getEntitySlotId()) {
        BackpackCriteriaTriggers.EQUIPPED.trigger((ServerPlayerEntity) entity);
      }
      if (this.hasColor(backpack)) {
        BackpackCriteriaTriggers.DYED.trigger((ServerPlayerEntity) entity);
      }
    }
    if ((slot != EquipmentSlot.CHEST.getEntitySlotId()) && !((PlayerEntity) entity).abilities.creativeMode) {
      final @Nullable CompoundTag nbt = backpack.getSubTag("BlockEntityTag");
      if ((nbt != null) && nbt.contains("Items", NbtType.LIST)) {
        final int size = nbt.getList("Items", NbtType.COMPOUND).size();
        final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
        Inventories.fromTag(nbt, stacks);
        for (final ItemStack stack : stacks) {
          if (!stack.isEmpty()) {
            ItemScatterer.spawn(world, entity.getBlockPos(), stacks);
            backpack.removeSubTag("BlockEntityTag");
            entity.dropStack(backpack);
            ((PlayerEntity) entity).inventory.setStack(slot, ItemStack.EMPTY);
            break;
          }
        }
      }
    }
  }

  @Override
  @Environment(EnvType.CLIENT)
  public void appendTooltip(
    final ItemStack stack, final @Nullable World world, final List<Text> tooltip,
    final TooltipContext context
  ) {
    super.appendTooltip(stack, world, tooltip, context);
    this.block.appendTooltip(stack, world, tooltip, context);
  }

  @Override
  public void appendStacks(final ItemGroup group, final DefaultedList<ItemStack> stacks) {
    if (this.isIn(group)) {
      this.block.addStacksForDisplay(group, stacks);
    }
  }

  private @Nullable BlockState getPlacementState(final ItemPlacementContext context) {
    final @Nullable BlockState state = this.block.getPlacementState(context);
    if ((state != null) && state.canPlaceAt(context.getWorld(), context.getBlockPos())) {
      if (context.getWorld().canPlace(state, context.getBlockPos(), Optional.ofNullable(context.getPlayer())
        .map(ShapeContext::of).orElseGet(ShapeContext::absent))
      ) {
        return state;
      }
    }
    return null;
  }

  private ActionResult place(final ItemPlacementContext context) {
    if (!context.canPlace()) {
      return ActionResult.FAIL;
    }
    final @Nullable BlockState placementState = this.getPlacementState(context);
    if (placementState == null) {
      return ActionResult.FAIL;
    }
    if (!context.getWorld().setBlockState(context.getBlockPos(), placementState, 11)) { // FIXME Use constant
      return ActionResult.FAIL;
    }
    final BlockPos pos = context.getBlockPos();
    final World world = context.getWorld();
    final PlayerEntity player = Objects.requireNonNull(context.getPlayer());
    final ItemStack stack = context.getStack();
    BlockState state = world.getBlockState(pos);
    final Block block = state.getBlock();
    if (block == placementState.getBlock()) {
      state = getBlockStateFromTag(pos, world, stack, state);
      final @Nullable BlockEntity be = world.getBlockEntity(pos);
      if (!(be instanceof BackpackBlockEntity)) {
        return ActionResult.FAIL;
      }
      BlockItem.writeTagToBlockEntity(world, player, pos, stack);
      ((BackpackBlockEntity) be).loadFrom(stack);
      block.onPlaced(world, pos, state, player, stack);
      if (player instanceof ServerPlayerEntity) {
        Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
      }
    }
    final BlockSoundGroup group = state.getSoundGroup();
    world.playSound(player, pos, group.getPlaceSound(), SoundCategory.BLOCKS,
      (group.getVolume() + 1.0F) / 2.0F, group.getPitch() * 0.8F
    );
    stack.decrement(1);
    return ActionResult.SUCCESS;
  }
}
