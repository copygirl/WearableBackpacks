package dev.sapphic.wearablebackpacks.advancement;

import dev.sapphic.wearablebackpacks.Backpacks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.advancement.CriterionRegistry;
import net.minecraft.util.Identifier;

public class BackpackCriteriaTriggers implements ModInitializer {
  public static final SimpleCriteriaTrigger EQUIPPED = create("backpack_equipped");
  public static final SimpleCriteriaTrigger DYED = create("backpack_dyed");

  private static SimpleCriteriaTrigger create(final String name) {
    return new SimpleCriteriaTrigger(new Identifier(Backpacks.ID, name));
  }

  @Override
  public void onInitialize() {
    CriterionRegistry.register(EQUIPPED);
    CriterionRegistry.register(DYED);
  }
}
