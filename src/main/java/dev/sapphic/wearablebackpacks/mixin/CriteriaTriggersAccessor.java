package dev.sapphic.wearablebackpacks.mixin;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Criteria.class)
public interface CriteriaTriggersAccessor {
  @Invoker
  static <T extends Criterion<?>> T callRegister(final T criterion) {
    throw new AssertionError();
  }
}
