package dev.sapphic.wearablebackpacks.advancement;

import dev.sapphic.wearablebackpacks.Backpacks;
import dev.sapphic.wearablebackpacks.mixin.CriteriaTriggersAccessor;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class BackpackCriteriaTriggers implements ModInitializer {
  public static final SimpleCriteriaTrigger EQUIPPED = create("equipped");
  public static final SimpleCriteriaTrigger DYED = create("dyed");

  private static SimpleCriteriaTrigger create(final String name) {
    return new SimpleCriteriaTrigger(new Identifier(Backpacks.ID, name));
  }

  @Override
  public void onInitialize() {
    CriteriaTriggersAccessor.callRegister(EQUIPPED);
    CriteriaTriggersAccessor.callRegister(DYED);
  }
}
