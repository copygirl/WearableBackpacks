package dev.sapphic.wearablebackpacks.client.mixin;

import dev.sapphic.wearablebackpacks.client.screen.MenuScreenFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HandledScreens.class)
@Environment(EnvType.CLIENT)
abstract class MenuScreensMixin {
  @Shadow
  @SuppressWarnings("LambdaUnfriendlyMethodOverload")
  private static <M extends ScreenHandler, U extends Screen & ScreenHandlerProvider<M>> void register(
    final ScreenHandlerType<? extends M> type, final HandledScreens.Provider<M, U> provider
  ) {
    throw new AssertionError();
  }

  @SuppressWarnings({ "unused", "LambdaUnfriendlyMethodOverload" })
  private static <M extends ScreenHandler, U extends Screen & ScreenHandlerProvider<M>> void register(
    final ScreenHandlerType<? extends M> type, final MenuScreenFactory<M, U> factory
  ) {
    register(type, (HandledScreens.Provider<M, U>) factory::create);
  }
}
