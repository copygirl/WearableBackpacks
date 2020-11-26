package dev.sapphic.wearablebackpacks.client.mixin;

import dev.sapphic.wearablebackpacks.Backpacks;
import dev.sapphic.wearablebackpacks.client.render.BackpackBlockRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
@Environment(EnvType.CLIENT)
abstract class BERDispatcherMixin {
  @Shadow
  protected abstract <E extends BlockEntity> void register(
    final BlockEntityType<E> type, final BlockEntityRenderer<E> renderer
  );

  @Inject(method = "<init>", at = @At("TAIL"))
  private void registerWearableBackpacksRenderer(final CallbackInfo ci) {
    this.register(Backpacks.BLOCK_ENTITY, new BackpackBlockRenderer((BlockEntityRenderDispatcher) (Object) this));
  }
}
