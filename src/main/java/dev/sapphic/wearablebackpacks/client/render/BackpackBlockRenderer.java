package dev.sapphic.wearablebackpacks.client.render;

import dev.sapphic.wearablebackpacks.block.BackpackBlock;
import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import dev.sapphic.wearablebackpacks.client.BackpacksClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public final class BackpackBlockRenderer implements BlockEntityRenderer<BackpackBlockEntity> {
private final MinecraftClient client = MinecraftClient.getInstance();

public BackpackBlockRenderer(final BlockEntityRenderDispatcher dispatcher) {
}

@Override
public void render(
        final BackpackBlockEntity backpack, final float tickDelta, final MatrixStack stack,
        final VertexConsumerProvider pipelines, final int light, final int overlay
) {
  final Direction facing = backpack.getCachedState().get(BackpackBlock.FACING);
  final BlockRenderManager manager = this.client.getBlockRenderManager();
  final BlockModels models = manager.getModels();
  final BlockModelRenderer renderer = manager.getModelRenderer();
  final VertexConsumer pipeline = ItemRenderer.getDirectItemGlintConsumer(pipelines, TexturedRenderLayers.getEntityCutout(), true, backpack.hasGlint());
  final BakedModel backpackModel = models.getModel(backpack.getCachedState());
  final BakedModel lidModel = models.getModelManager().getModel(BackpacksClient.getLidModel(facing));
  final Vec3f unitVector = facing.rotateYClockwise().getUnitVector();
  final Quaternion rotation = unitVector.getDegreesQuaternion(45.0F * backpack.getLidDelta(tickDelta));

  final int color = backpack.getColor();
  final float red = ((color >> 16) & 0xFF) / 255.0F;
  final float green = ((color >> 8) & 0xFF) / 255.0F;
  final float blue = (color & 0xFF) / 255.0F;

  final boolean xAxis = facing.getAxis() == Direction.Axis.X;
  final boolean inverse = facing.getDirection() == AxisDirection.NEGATIVE;
  final double xPivot = (inverse ? (1.0 - 0.3125) : 0.3125) * (xAxis ? 1.0 : 0.0);
  final double yPivot = 0.5625;
  final double zPivot = (inverse ? (1.0 - 0.3125) : 0.3125) * (xAxis ? 0.0 : 1.0);

  renderer.render(stack.peek(), pipeline, null, backpackModel, red, green, blue, light, OverlayTexture.DEFAULT_UV);

  stack.push();
  stack.translate(xPivot, yPivot, zPivot);
  stack.multiply(rotation);
  stack.translate(-xPivot, -yPivot, -zPivot);
  renderer.render(stack.peek(), pipeline, null, lidModel, red, green, blue, light, OverlayTexture.DEFAULT_UV);
  stack.pop();
}
}
