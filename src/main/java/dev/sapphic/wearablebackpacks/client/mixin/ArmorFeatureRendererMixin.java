package dev.sapphic.wearablebackpacks.client.mixin;

import dev.sapphic.wearablebackpacks.client.BackpacksClient;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ArmorFeatureRenderer.class)
abstract class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends
  FeatureRenderer<T, M> {
  ArmorFeatureRendererMixin(final FeatureRendererContext<T, M> context) {
    super(context);
  }

  @Inject(
    method = "renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V",
    at = @At(shift = At.Shift.BEFORE, value = "INVOKE", opcode = Opcodes.INVOKEVIRTUAL,
      target = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;getContextModel()Lnet/minecraft/client/render/entity/model/EntityModel;"),
    locals = LocalCapture.CAPTURE_FAILHARD,
    require = 1, allow = 1, cancellable = true)
  private void renderBackpack(final MatrixStack stack, final VertexConsumerProvider pipelines, final T entity, final EquipmentSlot slot, final int light, final A model, final CallbackInfo ci, final ItemStack itemStack) {
    if (itemStack.getItem() instanceof BackpackItem) {
      BackpacksClient.renderBackpack(stack, pipelines, itemStack, entity, light, this.getContextModel());
      ci.cancel();
    }
  }
}
