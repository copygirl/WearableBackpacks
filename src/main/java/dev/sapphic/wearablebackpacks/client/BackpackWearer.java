package dev.sapphic.wearablebackpacks.client;

import net.minecraft.entity.LivingEntity;

public interface BackpackWearer {
  static BackpackLid getBackpackState(final LivingEntity entity) {
    //noinspection CastToIncompatibleInterface
    return ((BackpackWearer) entity).getBackpackState();
  }
  
  BackpackLid getBackpackState();
}
