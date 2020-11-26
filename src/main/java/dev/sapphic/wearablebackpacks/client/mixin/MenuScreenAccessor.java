package dev.sapphic.wearablebackpacks.client.mixin;

import dev.sapphic.wearablebackpacks.client.screen.MenuScreenFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.client.gui.screen.ingame.HandledScreens")
@Environment(EnvType.CLIENT)
public interface MenuScreenAccessor {
  @SuppressWarnings("InvokerTarget")
  @Dynamic(mixin = MenuScreensMixin.class)
  @Invoker(remap = false)
  static <M extends ScreenHandler, U extends Screen & ScreenHandlerProvider<M>> void callRegister(
    final ScreenHandlerType<? extends M> type, final MenuScreenFactory<M, U> factory
  ) {
    throw new AssertionError();
  }
}
