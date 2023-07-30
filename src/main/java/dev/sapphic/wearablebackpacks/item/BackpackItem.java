package dev.sapphic.wearablebackpacks.item;

import dev.sapphic.wearablebackpacks.Backpack;
import dev.sapphic.wearablebackpacks.advancement.BackpackCriteria;
import dev.sapphic.wearablebackpacks.block.BackpackBlock;
import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ModifiableWorld;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

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
    final @Nullable NbtCompound blockStateTag = stack.getSubNbt("BlockStateTag");
    if (blockStateTag != null) {
      BlockState parsedState = state;
      final StateManager<Block, BlockState> container = state.getBlock().getStateManager();
      for (final String name : blockStateTag.getKeys()) {
        final @Nullable Property<?> property = container.getProperty(name);
        if (property != null) {
          final @Nullable NbtElement value = blockStateTag.get(name);
          if (value != null) {
            parsedState = with(parsedState, property, value.asString());
          }
        }
      }
      if (parsedState != state) {
        world.setBlockState(pos, parsedState, 2); // FIXME Use constant
      }
      return parsedState;
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
        BackpackCriteria.EQUIPPED.trigger((ServerPlayerEntity) entity);
      }
      if (this.hasColor(backpack)) {
        BackpackCriteria.DYED.trigger((ServerPlayerEntity) entity);
      }
    }
    if ((slot != EquipmentSlot.CHEST.getEntitySlotId()) && !((PlayerEntity) entity).getAbilities().creativeMode) {
      final DefaultedList<ItemStack> stacks = Backpack.getContents(backpack);
      boolean hasContents = false;
      for (final ItemStack stack : stacks) {
        if (!stack.isEmpty()) {
          hasContents = true;
          ((PlayerEntity) entity).getInventory().offerOrDrop(stack);
        }
      }
      if (hasContents) {
        backpack.removeSubNbt("BlockEntityTag");
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
      this.block.appendStacks(group, stacks);
    }
  }
  
  private @Nullable BlockState getPlacementState(final ItemPlacementContext context) {
    final @Nullable BlockState state = this.block.getPlacementState(context);
    if ((state != null) && state.canPlaceAt(context.getWorld(), context.getBlockPos())) {
      final @Nullable PlayerEntity player = context.getPlayer();
      final ShapeContext shapeContext = (player != null) ? ShapeContext.of(player) : ShapeContext.absent();
      if (context.getWorld().canPlace(state, context.getBlockPos(), shapeContext)) {
        return state;
      }
    }
    return null;
  }
  
  public ActionResult place(final ItemPlacementContext context) {
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
      ((BackpackBlockEntity) be).readFromStack(stack);
      //BlockItem.writeTagToBlockEntity(world, player, pos, stack);
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
