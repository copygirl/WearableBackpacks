package dev.sapphic.wearablebackpacks.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.snooper.SnooperListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor extends SnooperListener, WindowEventHandler {
    @Accessor
    float getPausedTickDelta();

    @Accessor
    RenderTickCounter getRenderTickCounter();
}
