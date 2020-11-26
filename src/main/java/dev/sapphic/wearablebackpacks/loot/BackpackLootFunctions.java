package dev.sapphic.wearablebackpacks.loot;

import dev.sapphic.wearablebackpacks.Backpacks;
import net.fabricmc.api.ModInitializer;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class BackpackLootFunctions implements ModInitializer {
  public static final LootFunctionType COPY_COLOR = new LootFunctionType(CopyColorLootFunction.serializer());

  private static void register(final String name, final LootFunctionType type) {
    Registry.register(Registry.LOOT_FUNCTION_TYPE, new Identifier(Backpacks.ID, name), type);
  }

  @Override
  public void onInitialize() {
    register("copy_color", COPY_COLOR);
  }
}
