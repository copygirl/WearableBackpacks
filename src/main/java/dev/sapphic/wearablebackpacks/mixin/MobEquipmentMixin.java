package dev.sapphic.wearablebackpacks.mixin;

import dev.sapphic.wearablebackpacks.Backpack;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MobEntity.class, PiglinEntity.class})
abstract class MobEquipmentMixin extends LivingEntity {
  MobEquipmentMixin(final EntityType<? extends LivingEntity> type, final World world) {
    super(type, world);
  }

  @Inject(method = "prefersNewEquipment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
          at = @At(shift = At.Shift.BEFORE, value = "INVOKE", opcode = Opcodes.INVOKESTATIC,
                  target = "Lnet/minecraft/enchantment/EnchantmentHelper;hasBindingCurse(Lnet/minecraft/item/ItemStack;)Z"),
          require = 1, allow = 1, cancellable = true)
  private void retainBackpackIfNonEmpty(
          final ItemStack newStack, final ItemStack oldStack, final CallbackInfoReturnable<Boolean> cir
  ) {
    if ((oldStack.getItem() instanceof BackpackItem) && !Backpack.isEmpty(oldStack)) {
      cir.setReturnValue(false);
    }
  }
}
