package dev.sapphic.wearablebackpacks.mixin;

import dev.sapphic.wearablebackpacks.item.BackpackItem;
import dev.sapphic.wearablebackpacks.stat.BackpackStats;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CauldronBlock.class)
abstract class CauldronBlockMixin {
//    @Inject(
//            method = "onUse",
//            at = @At(
//                    value = "FIELD",
//                    target = "Lnet/minecraft/stat/Stats;CLEAN_ARMOR:Lnet/minecraft/util/Identifier;",
//                    shift = Shift.BEFORE
//            ),
//            cancellable = true,
//            allow = 1)
private void tryCleanBackpack(
        final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand,
        final BlockHitResult hit, final CallbackInfoReturnable<? super ActionResult> cir
) {
  if (player.getStackInHand(hand).getItem() instanceof BackpackItem) {
    player.incrementStat(BackpackStats.CLEANED);
    cir.setReturnValue(ActionResult.SUCCESS);
  }
}
}
