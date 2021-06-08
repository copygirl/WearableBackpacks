package dev.sapphic.wearablebackpacks.mixin;

import dev.sapphic.wearablebackpacks.Backpacks;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ MobEntity.class, PiglinEntity.class })
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
    if (oldStack.getItem() == Backpacks.ITEM) {
      final @Nullable CompoundTag nbt = oldStack.getSubTag("BlockEntityTag");
      if ((nbt != null) && nbt.contains("Items", NbtType.LIST)) {
        final int size = nbt.getList("Items", NbtType.COMPOUND).size();
        final DefaultedList<ItemStack> contents = DefaultedList.ofSize(size, ItemStack.EMPTY);
        Inventories.fromTag(nbt, contents);
        for (final ItemStack stack : contents) {
          if (!stack.isEmpty()) {
            cir.setReturnValue(false);
            return;
          }
        }
      }
    }
  }
}
