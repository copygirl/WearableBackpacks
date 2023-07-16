package dev.sapphic.wearablebackpacks.client.mixin;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VertexConsumers.Dual.class)
public interface DualVertexConsumerAccessor extends VertexConsumer {
    @Accessor
    VertexConsumer getSecond();
}
