package dev.sapphic.wearablebackpacks.mixin.client;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VertexConsumers.class) // Dual is private
// it should target VertexConsumers.Dual
public interface DualVertexConsumerAccessor extends VertexConsumer {
//    @Accessor
//    VertexConsumer getSecond();
}
