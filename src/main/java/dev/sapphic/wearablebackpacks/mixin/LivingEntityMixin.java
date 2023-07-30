package dev.sapphic.wearablebackpacks.mixin;

import dev.sapphic.wearablebackpacks.client.BackpackLid;
import dev.sapphic.wearablebackpacks.client.BackpackWearer;
import dev.sapphic.wearablebackpacks.network.BackpackServerNetwork;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity implements BackpackWearer {
  @Unique
  private final BackpackLid backpackState = new BackpackLid(backpack -> {
    BackpackServerNetwork.backpackUpdated((LivingEntity) (Object) this);
  });
  
  LivingEntityMixin(final EntityType<?> type, final World world) {
    super(type, world);
  }
  
  @Unique
  @Override
  public final BackpackLid getBackpackState() {
    return this.backpackState;
  }
  
  @Inject(method = "baseTick()V", at = @At("TAIL"))
  private void tickBackpackState(final CallbackInfo ci) {
    this.backpackState.tick(null, null, null, null);
  }
}
