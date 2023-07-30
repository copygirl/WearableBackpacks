package dev.sapphic.wearablebackpacks.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BucketItem.class)
public interface BucketItemAccessor {
@Accessor
Fluid getFluid();

@Invoker
ItemStack callGetEmptiedStack(final ItemStack stack, final PlayerEntity player);

@Invoker
void invokePlayEmptyingSound(final PlayerEntity player, final WorldAccess world, final BlockPos pos);
}
