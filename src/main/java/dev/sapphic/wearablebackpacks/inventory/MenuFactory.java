package dev.sapphic.wearablebackpacks.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;

public interface MenuFactory<T extends ScreenHandler> {
  T create(final int id, final PlayerInventory inventory);
}
