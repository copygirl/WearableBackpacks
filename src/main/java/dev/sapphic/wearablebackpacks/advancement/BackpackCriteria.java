package dev.sapphic.wearablebackpacks.advancement;

import dev.sapphic.wearablebackpacks.Backpacks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.advancement.CriterionRegistry;
import net.minecraft.util.Identifier;

public final class BackpackCriteria implements ModInitializer {
  public static final SimpleCriterion EQUIPPED = criterion("backpack_equipped");
  public static final SimpleCriterion DYED = criterion("backpack_dyed");

  private static SimpleCriterion criterion(final String name) {
    return new SimpleCriterion(new Identifier(Backpacks.ID, name));
  }

  @Override
  public void onInitialize() {
    CriterionRegistry.register(EQUIPPED);
    CriterionRegistry.register(DYED);
  }
}
