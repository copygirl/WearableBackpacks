package dev.sapphic.wearablebackpacks.client.mixin;

import dev.sapphic.wearablebackpacks.Backpack;
import dev.sapphic.wearablebackpacks.Backpacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemColors.class)
@Environment(EnvType.CLIENT)
abstract class ItemColorsMixin {
  @Inject(method = "create", at = @At("RETURN"))
  private static void addWearableBackpacksColors(final CallbackInfoReturnable<? extends ItemColors> cir) {
    cir.getReturnValue().register((stack, tintIndex) -> Backpack.getColor(stack), Backpacks.ITEM);
  }
}
