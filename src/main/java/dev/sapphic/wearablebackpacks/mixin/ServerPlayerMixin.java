package dev.sapphic.wearablebackpacks.mixin;

import dev.sapphic.wearablebackpacks.inventory.BackpackMenu;
import dev.sapphic.wearablebackpacks.network.ClientboundOpenBackpackPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.OptionalInt;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerMixin extends PlayerEntity implements ScreenHandlerListener {
  @Shadow public ServerPlayNetworkHandler networkHandler;
  @Shadow private int screenHandlerSyncId;

  ServerPlayerMixin() {
    //noinspection ConstantConditions // TODO Suppress on super calls within a mixin stub constructor?
    super(null, null, Float.NaN, null);
  }

  @Inject(
    method = "openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;",
    at = @At(
      value = "INVOKE",
      // TODO Shift a few steps before argument loading and constructor invocation?
      target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"),
    locals = LocalCapture.CAPTURE_FAILHARD,
    allow = 1,
    cancellable = true)
  private void openBackpackMenu(
    final NamedScreenHandlerFactory factory, final CallbackInfoReturnable<OptionalInt> cir, final ScreenHandler menu
  ) {
    // Such overkill. Really need locals capture for Redirect/ModifyArg for rewriting the constructor call when viable
    if (menu.getType() == BackpackMenu.TYPE) {
      this.networkHandler.sendPacket(new ClientboundOpenBackpackPacket(factory.getDisplayName(), (BackpackMenu) menu));
      menu.addListener(this);
      this.currentScreenHandler = menu;
      cir.setReturnValue(OptionalInt.of(this.screenHandlerSyncId));
    }
    // else {
    //   this.networkHandler.sendPacket(new OpenScreenS2CPacket(
    //     menu.syncId, menu.getType(), factory.getDisplayName()
    //   ));
    //   screenHandler.addListener(this);
    //   this.currentScreenHandler = menu;
    //   return OptionalInt.of(this.screenHandlerSyncId);
    // }
  }
}
