package dev.sapphic.wearablebackpacks.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// SnooperListener,
@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor extends WindowEventHandler {
  @Accessor
  float getPausedTickDelta();
  
  @Accessor
  RenderTickCounter getRenderTickCounter();
}
