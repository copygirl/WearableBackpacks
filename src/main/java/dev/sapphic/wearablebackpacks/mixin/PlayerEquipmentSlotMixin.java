package dev.sapphic.wearablebackpacks.mixin;

import dev.sapphic.wearablebackpacks.Backpacks;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.screen.PlayerScreenHandler$1")
abstract class PlayerEquipmentSlotMixin extends Slot {
  PlayerEquipmentSlotMixin(final Inventory inventory, final int index, final int x, final int y) {
    super(inventory, index, x, y);
  }

  @Inject(method = "canTakeItems(Lnet/minecraft/entity/player/PlayerEntity;)Z",
    at = @At(value = "RETURN", ordinal = 1), require = 1, allow = 1, cancellable = true)
  private void retainBackpackIfNonEmpty(final PlayerEntity player, final CallbackInfoReturnable<Boolean> cir) {
    final ItemStack armorStack = this.getStack(); // Cannot capture from LVT (Out of scope?)
    if (armorStack.getItem() == Backpacks.ITEM) {
      final @Nullable CompoundTag nbt = armorStack.getSubTag("BlockEntityTag");
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
