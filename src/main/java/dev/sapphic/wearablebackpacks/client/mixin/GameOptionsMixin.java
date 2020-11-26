package dev.sapphic.wearablebackpacks.client.mixin;

import dev.sapphic.wearablebackpacks.client.BackpacksClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.File;

@Mixin(GameOptions.class)
@Environment(EnvType.CLIENT)
abstract class GameOptionsMixin {
  @Shadow @Final @Mutable public KeyBinding[] keysAll;

  @ModifyVariable(
    method = "<init>",
    at = @At(
      value = "FIELD",
      target = "Lnet/minecraft/client/options/GameOptions;keysAll:[Lnet/minecraft/client/options/KeyBinding;",
      opcode = Opcodes.PUTFIELD,
      shift = Shift.AFTER))
  private File addWearableBackpacksKey(final File file) {
    // this.keysAll = (KeyBinding[])ArrayUtils.addAll(...
    this.keysAll = BackpacksClient.addBackpackKeyBinding(this.keysAll);
    // this.difficulty = Difficulty.NORMAL;
    return file; // (ignore)
  }
}
