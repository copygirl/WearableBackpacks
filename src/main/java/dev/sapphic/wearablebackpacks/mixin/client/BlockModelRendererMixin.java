package dev.sapphic.wearablebackpacks.mixin.client;

import dev.sapphic.wearablebackpacks.Backpacks;
import dev.sapphic.wearablebackpacks.client.BackpacksClient;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(BlockModelRenderer.class)
abstract class BlockModelRendererMixin {
  //    @Shadow
  private static void renderQuad(final MatrixStack.Entry entry, final VertexConsumer pipeline, final float red, final float green, final float blue, final List<BakedQuad> quads, final int light, final int overlay) {
    throw new AssertionError();
  }
  
  //    @Redirect(
//            method = "render(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/block/BlockState;Lnet/minecraft/client/render/model/BakedModel;FFFII)V",
//            at = @At(value = "INVOKE", opcode = Opcodes.INVOKEVIRTUAL,
//                    target = "Lnet/minecraft/client/render/block/BlockModelRenderer;renderQuad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;FFFLjava/util/List;II)V"),
//            require = 2, allow = 2)
  private void swapOverlayPipeline(
    final MatrixStack.Entry entry, final VertexConsumer pipeline, final float red, final float green, final float blue, final List<BakedQuad> quads, final int light, final int overlay,
    final MatrixStack.Entry entry1, final VertexConsumer pipeline1, final @Nullable BlockState state, final BakedModel model, final float red1, final float green1, final float blue1, final int light1, final int overlay1
  ) {
    if (pipeline instanceof DualVertexConsumerAccessor) {
      final BlockModels models = MinecraftClient.getInstance().getBlockRenderManager().getModels();
      final boolean isLid = model == models.getModelManager().getModel(BackpacksClient.getLidModel(Direction.NORTH));
      if (isLid || (model == models.getModel(Backpacks.BLOCK.getDefaultState()))) {
        BackpacksClient.renderBackpackQuad(entry, pipeline, red, green, blue, quads, light, overlay);
        return;
      }
    }
    renderQuad(entry, pipeline, red, green, blue, quads, light, overlay);
  }
  
}
