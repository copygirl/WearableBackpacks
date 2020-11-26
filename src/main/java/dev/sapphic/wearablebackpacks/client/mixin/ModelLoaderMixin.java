package dev.sapphic.wearablebackpacks.client.mixin;

import com.google.common.collect.ImmutableMap;
import dev.sapphic.wearablebackpacks.client.BackpacksClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelLoader.class)
@Environment(EnvType.CLIENT)
abstract class ModelLoaderMixin {
  @SuppressWarnings({ "StaticVariableMayNotBeInitialized", "NonConstantFieldWithUpperCaseName" })
  @Shadow @Final @Mutable
  private static Map<Identifier, StateManager<Block, BlockState>> STATIC_DEFINITIONS;

  @SuppressWarnings("UnresolvedMixinReference")
  @Inject(
    method = "<clinit>",
    at = @At(
      value = "FIELD",
      target = "Lnet/minecraft/client/render/model/ModelLoader;STATIC_DEFINITIONS:Ljava/util/Map;",
      opcode = Opcodes.PUTSTATIC,
      shift = Shift.AFTER),
    allow = 1)
  private static void addBackpackLidStates(final CallbackInfo ci) {
    final ImmutableMap.Builder<Identifier, StateManager<Block, BlockState>> builder = ImmutableMap.builder();
    BackpacksClient.addLidStates(builder.putAll(STATIC_DEFINITIONS));
    STATIC_DEFINITIONS = builder.build();
  }
}
