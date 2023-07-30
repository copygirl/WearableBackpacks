package dev.sapphic.wearablebackpacks.client.mixin;

import dev.sapphic.wearablebackpacks.Backpacks;
import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;

// implements SnooperListener,
@Mixin(MinecraftClient.class)
@Environment(EnvType.CLIENT)
abstract class MinecraftClientMixin implements WindowEventHandler {
  //    @ModifyVariable(
//            method = "addBlockEntityNbt",
//            at = @At(
//                    value = "INVOKE_ASSIGN",
//                    target = "Lnet/minecraft/block/entity/BlockEntity;writeNbt(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/nbt/NbtCompound;",
//                    opcode = Opcodes.INVOKEVIRTUAL,
//                    shift = Shift.AFTER
//            ),
//            allow = 1)
  private NbtCompound stripRedundantColor(final NbtCompound nbt, final ItemStack stack, final BlockEntity be) {
    if ((be instanceof BackpackBlockEntity) && nbt.contains("Color", NbtType.INT)) {
      nbt.remove("Color"); // We don't need this, color is defined by DyeableItem
    }
    return nbt;
  }
  
  //
//    @Redirect(
//            method = "addBlockEntityNbt",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/item/ItemStack;putSubTag(Ljava/lang/String;Lnet/minecraft/nbt/NbtElement;)V",
//                    opcode = Opcodes.INVOKEVIRTUAL
//            ),
//            slice = @Slice(from = @At(
//                    value = "CONSTANT",
//                    args = "stringValue=display"
//            )),
//            allow = 1, require = 0) // If someone else is redirecting this they likely have similar intentions
  private void patchDisplayNbt(final ItemStack stack, final String key, final NbtElement nbt) {
    final NbtCompound existing = stack.getOrCreateSubNbt(key);
    if ((existing == null) || (stack.getItem() != Backpacks.ITEM)) {
      // Vanilla logic
//            stack.putSubTag(key, nbt);
      return;
    }
    // Don't overwrite the existing tag, instead merge into it, so that our color is removed
    for (final String subKey : ((NbtCompound) nbt).getKeys()) {
      // We overwrite sub tags; to account for this too would be out of scope for our need
      existing.put(subKey, ((NbtCompound) nbt).get(subKey));
    }
  }
}
