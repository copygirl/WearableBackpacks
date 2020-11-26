package dev.sapphic.wearablebackpacks.mixin;

import dev.sapphic.wearablebackpacks.inventory.MenuFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ScreenHandlerType.class)
abstract class MenuTypeMixin {
  @SuppressWarnings("unused")
  private static <T extends ScreenHandler> ScreenHandlerType<T> create(final MenuFactory<? extends T> factory) {
    return new ScreenHandlerType<>(factory::create);
  }
}
