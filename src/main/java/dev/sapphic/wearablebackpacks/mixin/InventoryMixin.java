package dev.sapphic.wearablebackpacks.mixin;

import dev.sapphic.wearablebackpacks.item.BackpackItem;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
abstract class InventoryMixin implements Inventory, Nameable {
  @Shadow @Final public PlayerEntity player;

  @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
  private void disallowBackpacks(final int slot, final ItemStack stack, final CallbackInfoReturnable<Boolean> cir) {
    if (!this.player.abilities.creativeMode) {
      if ((slot != EquipmentSlot.CHEST.getEntitySlotId()) && (stack.getItem() instanceof BackpackItem)) {
        final @Nullable CompoundTag nbt = stack.getSubTag("BlockEntityTag");
        if (nbt == null) {
          return;
        }
        if (nbt.contains("Items", NbtType.LIST)) {
          final int size = nbt.getList("Items", NbtType.COMPOUND).size();
          final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
          Inventories.fromTag(nbt, stacks);
          for (final ItemStack st : stacks) {
            if (!st.isEmpty()) {
              ItemScatterer.spawn(this.player.world, this.player.getBlockPos(), stacks);
              stack.removeSubTag("BlockEntityTag");
              this.player.dropStack(stack);
              this.player.inventory.setStack(slot, ItemStack.EMPTY);
              cir.setReturnValue(false);
              break;
            }
          }
        }
      }
    }
  }
}
