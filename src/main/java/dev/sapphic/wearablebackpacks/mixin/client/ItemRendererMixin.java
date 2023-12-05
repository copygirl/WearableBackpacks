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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ItemRenderer.class, priority = 1100) //priority needed to keep it from crashing with sodium
abstract class ItemRendererMixin {
  private ItemStack stack;

  @Redirect(
    method = "renderBakedItemQuads",
    at = @At(value = "INVOKE", opcode = Opcodes.INVOKEVIRTUAL,
      target = "Lnet/minecraft/client/render/VertexConsumer;quad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;FFFII)V"),
    require = 1, allow = 1)
  private void swapOverlayPipeline(
          VertexConsumer pipeline, MatrixStack.Entry entry, BakedQuad quad, float red, float green, float blue, int light, int overlay) {
    if ((pipeline instanceof DualVertexConsumerAccessor) && (stack.getItem() == Backpacks.backpackItem) && !quad.hasColor()) {
//            ((DualVertexConsumerAccessor) pipeline).getSecond().quad(entry, quad, red, green, blue, light, overlay);
    } else {
      pipeline.quad(entry, quad, red, green, blue, light, overlay);
    }
  }

  //Done so the mixin doesn't crash from extra parameters
  @Inject(method = "renderBakedItemQuads", at = @At(value = "HEAD"))
  private void getStack(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, ItemStack stack, int light, int overlay, CallbackInfo ci) {
      this.stack = stack;
  }
}
