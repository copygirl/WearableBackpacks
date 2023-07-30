package dev.sapphic.wearablebackpacks.stat;

import dev.sapphic.wearablebackpacks.Backpacks;
import net.fabricmc.api.ModInitializer;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class BackpackStats implements ModInitializer {
public static final Identifier OPENED = new Identifier(Backpacks.ID, "backpacks_opened");
public static final Identifier CLEANED = new Identifier(Backpacks.ID, "backpacks_cleaned");

private static void register(final Identifier stat) {
  Stats.CUSTOM.getOrCreateStat(Registry.register(Registry.CUSTOM_STAT, stat, stat));
}

@Override
public void onInitialize() {
  register(OPENED);
  register(CLEANED);
}
}
