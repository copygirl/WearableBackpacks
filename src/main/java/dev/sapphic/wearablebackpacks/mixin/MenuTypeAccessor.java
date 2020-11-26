package dev.sapphic.wearablebackpacks.mixin;

import dev.sapphic.wearablebackpacks.inventory.MenuFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.screen.ScreenHandlerType")
public interface MenuTypeAccessor {
  @SuppressWarnings("InvokerTarget")
  @Dynamic(mixin = MenuTypeMixin.class)
  @Invoker(remap = false)
  static <T extends ScreenHandler> ScreenHandlerType<T> callCreate(final MenuFactory<T> factory) {
    throw new AssertionError();
  }
}
