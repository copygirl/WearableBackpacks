package dev.sapphic.wearablebackpacks.client.mixin;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(VertexConsumers.class) // Dual is private
// it should target VertexConsumers.Dual
public interface DualVertexConsumerAccessor extends VertexConsumer {
//    @Accessor
//    VertexConsumer getSecond();
}
