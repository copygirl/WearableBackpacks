package dev.sapphic.wearablebackpacks.mixin.client;

import dev.sapphic.wearablebackpacks.Backpacks;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(ItemRenderer.class)
abstract class ItemRendererMixin {
  @Redirect(
    method = "renderBakedItemQuads(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Ljava/util/List;Lnet/minecraft/item/ItemStack;II)V",
    at = @At(value = "INVOKE", opcode = Opcodes.INVOKEVIRTUAL,
      target = "Lnet/minecraft/client/render/VertexConsumer;quad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;FFFII)V"),
    require = 1, allow = 1)
  private void swapOverlayPipeline(
    final VertexConsumer pipeline, final MatrixStack.Entry entry, final BakedQuad quad, final float red, final float green, final float blue, final int light, final int overlay,
    final MatrixStack matrices, final VertexConsumer vertices, final List<BakedQuad> quads, final ItemStack stack, final int light1, final int overlay1
  ) {
    if ((pipeline instanceof DualVertexConsumerAccessor) && (stack.getItem() == Backpacks.backpackItem) && !quad.hasColor()) {
//            ((DualVertexConsumerAccessor) pipeline).getSecond().quad(entry, quad, red, green, blue, light, overlay);
    } else {
      pipeline.quad(entry, quad, red, green, blue, light, overlay);
    }
  }
}
