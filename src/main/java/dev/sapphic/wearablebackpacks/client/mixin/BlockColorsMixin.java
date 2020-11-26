package dev.sapphic.wearablebackpacks.client.mixin;

import dev.sapphic.wearablebackpacks.Backpack;
import dev.sapphic.wearablebackpacks.Backpacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockColors.class)
@Environment(EnvType.CLIENT)
abstract class BlockColorsMixin {
  @Inject(method = "create", at = @At("RETURN"))
  private static void addWearableBackpacksColors(final CallbackInfoReturnable<? extends BlockColors> cir) {
    cir.getReturnValue().registerColorProvider((state, world, pos, tint) -> {
      return ((world != null) && (pos != null)) ? Backpack.getColor(world, pos) : Backpack.DEFAULT_COLOR;
    }, Backpacks.BLOCK);
  }
}
