package dev.sapphic.wearablebackpacks.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public interface MenuScreenFactory<T extends ScreenHandler, U extends Screen & ScreenHandlerProvider<T>> {
  U create(T menu, PlayerInventory inventory, Text text);
}
