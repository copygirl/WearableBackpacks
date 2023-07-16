package dev.sapphic.wearablebackpacks.mixin;

import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DyeColor.class)
public interface DyeColorAccessor {
    @Accessor
    int getColor();
}
