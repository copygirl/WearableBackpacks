package dev.sapphic.wearablebackpacks.mixin;

import dev.sapphic.wearablebackpacks.Backpack;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
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
    if ((armorStack.getItem() instanceof BackpackItem) && !Backpack.isEmpty(armorStack)) {
      cir.setReturnValue(false);
    }
  }
}
