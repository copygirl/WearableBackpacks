package dev.sapphic.wearablebackpacks.client;

import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.function.Consumer;

public final class BackpackLid implements BlockEntityTicker<BackpackBlockEntity> {
  private static final float CLOSED_DELTA = 0.0F;
  private static final float OPENED_DELTA = 1.0F;
  private static final float DELTA_STEP = 0.2F;
  private final Consumer<BackpackLid> onChange;
  private LidState lidState = LidState.CLOSED;
  private float lidDelta;
  private float lastLidDelta;
  private int openCount = 0;

  public BackpackLid(final Consumer<BackpackLid> onChange) {
    this.onChange = onChange;
  }

  public boolean isOpen() {
    return this.openCount == 1;
  }

  public boolean isClosed() {
    return this.openCount <= 0;
  }

  public void opened() {
    if (this.openCount < 0) {
      this.openCount = 0;
    }
    ++this.openCount;
    this.onChange.accept(this);
  }

  public void closed() {
    --this.openCount;
    this.onChange.accept(this);
  }

  public float lidDelta(final float tickDelta) {
    return MathHelper.lerp(tickDelta, this.lastLidDelta, this.lidDelta);
  }

  public int openCount() {
    return this.openCount;
  }

  public boolean count(final int openCount) {
    this.openCount = openCount;
    if (this.openCount == 0) {
      this.lidState = LidState.CLOSING;
    }
    if (this.openCount == 1) {
      this.lidState = LidState.OPENING;
    }
    return true;
  }

  @Override
  public void tick(World world, BlockPos pos, BlockState state, BackpackBlockEntity blockEntity) {
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
