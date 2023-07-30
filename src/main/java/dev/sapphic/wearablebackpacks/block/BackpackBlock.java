package dev.sapphic.wearablebackpacks.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.sapphic.wearablebackpacks.Backpack;
import dev.sapphic.wearablebackpacks.Backpacks;
import dev.sapphic.wearablebackpacks.advancement.BackpackCriteria;
import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import dev.sapphic.wearablebackpacks.mixin.BucketItemAccessor;
import dev.sapphic.wearablebackpacks.stat.BackpackStats;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.tick.OrderedTick;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class BackpackBlock extends BlockWithEntity implements Waterloggable {
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
  
  private static final Map<Direction, VoxelShape> SHAPES = ImmutableMap.of(
      Direction.NORTH, VoxelShapes.union(
          VoxelShapes.cuboid(0.1875, 0.0, 0.375, 0.8125, 0.75, 0.6875),
          VoxelShapes.cuboid(0.25, 0.0625, 0.25, 0.75, 0.4375, 0.375)
      ),
      Direction.EAST, VoxelShapes.union(
          VoxelShapes.cuboid(0.3125, 0.0, 0.1875, 0.625, 0.75, 0.8125),
          VoxelShapes.cuboid(0.625, 0.0625, 0.25, 0.75, 0.4375, 0.75)
      ),
      Direction.SOUTH, VoxelShapes.union(
          VoxelShapes.cuboid(0.1875, 0.0, 0.3125, 0.8125, 0.75, 0.625),
          VoxelShapes.cuboid(0.25, 0.0625, 0.625, 0.75, 0.4375, 0.75)
      ),
      Direction.WEST, VoxelShapes.union(
          VoxelShapes.cuboid(0.375, 0.0, 0.1875, 0.6875, 0.75, 0.8125),
          VoxelShapes.cuboid(0.25, 0.0625, 0.25, 0.375, 0.4375, 0.75)
      )
  );
  
  public BackpackBlock(final Settings settings) {
    super(settings);
    this.setDefaultState(this.stateManager.getDefaultState()
                             .with(FACING, Direction.NORTH).with(WATERLOGGED, false));
  }
  
  @Override
  public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
    
    if (state.get(WATERLOGGED)) {
      world.getFluidTickScheduler().scheduleTick(OrderedTick.create(Fluids.WATER, pos));
    }
    return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
  }
  
  @Override
  @Deprecated
  public void onStateReplaced(
      final BlockState state, final World world, final BlockPos pos, final BlockState next, final boolean moved
  ) {
    if (state.getBlock() != next.getBlock()) {
      final @Nullable BlockEntity be = world.getBlockEntity(pos);
      if (be instanceof BackpackBlockEntity) {
        ItemScatterer.spawn(world, pos, (Inventory) be);
        world.updateComparators(pos, this);
      }
      super.onStateReplaced(state, world, pos, next, moved);
    }
  }
  
  @Override
  @Deprecated
  public ActionResult onUse(
      final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand,
      final BlockHitResult hit
  ) {
    final BlockEntity be = world.getBlockEntity(pos);
    if (be instanceof BackpackBlockEntity) {
      final ItemStack stack = player.getStackInHand(hand);
      final Backpack backpack = (Backpack) be;
      if (stack.getItem() instanceof DyeItem) {
        if (!world.isClient) {
          final int newColor = this.getBlendedColor(backpack, (DyeItem) stack.getItem());
          if (!backpack.hasColor() || (backpack.getColor() != newColor)) {
            if (!world.canPlayerModifyAt(player, pos)) {
              return ActionResult.FAIL;
            }
            backpack.setColor(newColor);
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, player.getSoundCategory(),
                0.5F, (player.world.random.nextFloat() * 0.1F) + 0.9F
            );
            if (!player.getAbilities().creativeMode) {
              stack.decrement(1);
            }
            BackpackCriteria.DYED.trigger((ServerPlayerEntity) player);
          }
        }
        return ActionResult.success(world.isClient);
      }
      if (backpack.hasColor() && (stack.getItem() instanceof BucketItem)) {
        //noinspection CastToIncompatibleInterface
        final BucketItemAccessor bucket = (BucketItemAccessor) stack.getItem();
        if (bucket.getFluid().isIn(FluidTags.WATER)) {
          if (!world.canPlayerModifyAt(player, pos)) {
            return ActionResult.FAIL;
          }
          if (!world.isClient) {
            player.setStackInHand(hand, bucket.callGetEmptiedStack(stack, player));
            bucket.invokePlayEmptyingSound(null, world, pos);
            backpack.clearColor();
            player.incrementStat(BackpackStats.CLEANED);
          }
          return ActionResult.success(world.isClient);
        }
      }
      if (!world.isClient) {
        NamedScreenHandlerFactory factory = (NamedScreenHandlerFactory) be;
        player.openHandledScreen(factory);
      }
      player.incrementStat(BackpackStats.OPENED);
      return ActionResult.success(world.isClient);
    }
    return ActionResult.FAIL;
  }
  
  @Override
  @Deprecated
  public FluidState getFluidState(final BlockState state) {
    return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
  }
  
  @Override
  @Deprecated
  public boolean hasComparatorOutput(final BlockState state) {
    return true;
  }
  
  @Override
  @Deprecated
  public BlockState rotate(final BlockState state, final BlockRotation rotation) {
    return state.with(FACING, rotation.rotate(state.get(FACING)));
  }
  
  @Override
  @Deprecated
  public BlockState mirror(final BlockState state, final BlockMirror mirror) {
    return state.rotate(mirror.getRotation(state.get(FACING)));
  }
  
  @Override
  @Deprecated
  public int getComparatorOutput(final BlockState state, final World world, final BlockPos pos) {
    return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
  }
  
  @Override
  @Deprecated
  public VoxelShape getOutlineShape(
      final BlockState state, final BlockView view, final BlockPos pos, final ShapeContext context
  ) {
    return SHAPES.get(state.get(FACING));
  }
  
  @Override
  @Deprecated
  public float calcBlockBreakingDelta(
      final BlockState state, final PlayerEntity player, final BlockView world, final BlockPos pos
  ) {
    final @Nullable BlockEntity be = world.getBlockEntity(pos);
    if ((be instanceof BackpackBlockEntity) && ((Inventory) be).isEmpty()) {
      return super.calcBlockBreakingDelta(state, player, world, pos);
    }
    if (player.isSneaking() && !(player.getEquippedStack(EquipmentSlot.CHEST).getItem() instanceof BackpackItem)) {
      return super.calcBlockBreakingDelta(state, player, world, pos);
    }
    return 0.005F;
  }
  
  @Override
  public @Nullable BlockState getPlacementState(final ItemPlacementContext context) {
    final Direction facing = context.getPlayerFacing().getOpposite();
    final Fluid fluid = context.getWorld().getFluidState(context.getBlockPos()).getFluid();
    return this.getDefaultState().with(FACING, facing).with(WATERLOGGED, fluid == Fluids.WATER);
  }
  
  @Override
  public void afterBreak(final World world, final PlayerEntity player, final BlockPos pos, final BlockState state, final @Nullable BlockEntity be, final ItemStack stack) {
    player.incrementStat(Stats.MINED.getOrCreateStat(this));
    player.addExhaustion(0.005F);
    if (!player.isSneaking()) {
      dropStacks(state, world, pos, be, player, stack);
    }
  }
  
  @Override
  public ItemStack getPickStack(final BlockView world, final BlockPos pos, final BlockState state) {
    return this.getPickStack(world.getBlockEntity(pos), world, pos, state);
  }
  
  @Override
  public void onBreak(final World world, final BlockPos pos, final BlockState state, final PlayerEntity player) {
    final @Nullable BlockEntity be = world.getBlockEntity(pos);
    if ((be instanceof BackpackBlockEntity) && player.isSneaking() && !player.hasStackEquipped(EquipmentSlot.CHEST)) {
      final ItemStack stack = this.getPickStack(be, world, pos, state);
      final NbtCompound tag = stack.getOrCreateSubNbt("BlockEntityTag");
      Inventories.writeNbt(tag, ((Backpack) be).getContents());
      player.equipStack(EquipmentSlot.CHEST, stack);
      super.onBreak(world, pos, state, player);
      world.removeBlockEntity(pos);
      return;
    }
    super.onBreak(world, pos, state, player);
  }
  
  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING, WATERLOGGED);
  }
  
  @Override
  @Environment(EnvType.CLIENT)
  public void appendTooltip(
      final ItemStack stack, final @Nullable BlockView world, final List<Text> tooltip, final TooltipContext options
  ) {
    super.appendTooltip(stack, world, tooltip, options);
    final @Nullable NbtCompound tag = stack.getOrCreateSubNbt("BlockEntityTag");
    if (tag != null) {
      boolean hasItems = tag.contains("LootTable", 8);
      if (!hasItems && tag.contains("Items", 9)) {
        final DefaultedList<ItemStack> contents = DefaultedList.ofSize(27, ItemStack.EMPTY);
        Inventories.readNbt(tag, contents);
        for (final ItemStack contentsStack : contents) {
          if (!contentsStack.isEmpty()) {
            hasItems = true;
            break;
          }
        }
        if (hasItems) {
          tooltip.add(Text.translatable("container." + Backpacks.ID + ".items").formatted(Formatting.GOLD));
        }
      }
    }
    
  }
  
  @Override
  public BlockRenderType getRenderType(final BlockState state) {
    return BlockRenderType.ENTITYBLOCK_ANIMATED;
  }
  
  @Nullable
  @Override
  public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return new BackpackBlockEntity(pos, state);
  }
  
  private int getBlendedColor(final Backpack backpack, final DyeItem dye) {
    if (backpack.hasColor()) {
      final ItemStack tmp = new ItemStack(this);
      final DyeableItem item = (DyeableItem) tmp.getItem();
      item.setColor(tmp, backpack.getColor());
      return item.getColor(DyeableItem.blendAndSetColor(tmp, ImmutableList.of(dye)));
    }
    //noinspection ConstantConditions
//        return ((DyeColorAccessor) (Object) dye.getColor()).getColor();
    return 12345;
  }
  
  private ItemStack getPickStack(
      final BlockEntity be, final BlockView world, final BlockPos pos, final BlockState state
  ) {
    final ItemStack stack = super.getPickStack(world, pos, state);
    if (be instanceof BackpackBlockEntity) {
      ((BackpackBlockEntity) be).writeToStack(stack);
    }
    return stack;
  }
  
  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
    return checkType(type, Backpacks.BLOCK_ENTITY, (entityWorld, pos, blockState, blockEntity) -> blockEntity.tick(entityWorld, pos, blockState, blockEntity));
  }
}
