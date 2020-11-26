package dev.sapphic.wearablebackpacks.client.mixin;

import dev.sapphic.wearablebackpacks.client.screen.BackpackScreen;
import dev.sapphic.wearablebackpacks.inventory.BackpackMenu;
import dev.sapphic.wearablebackpacks.network.ClientboundOpenBackpackPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientPlayNetworkHandler.class)
@Environment(EnvType.CLIENT)
abstract class PacketListenerMixin {
  @Shadow private MinecraftClient client;

  @Inject(
    method = "onOpenScreen",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreens;open(Lnet/minecraft/screen/ScreenHandlerType;Lnet/minecraft/client/MinecraftClient;ILnet/minecraft/text/Text;)V",
      shift = Shift.BEFORE),
    allow = 1,
    cancellable = true)
  private void handleBackpackScreen(final OpenScreenS2CPacket packet, final CallbackInfo ci) {
    if (packet.getScreenHandlerType() == BackpackMenu.TYPE) {
      final int rows = ((ClientboundOpenBackpackPacket) packet).getRows();
      final int columns = ((ClientboundOpenBackpackPacket) packet).getColumns();
      final ClientPlayerEntity player = Objects.requireNonNull(this.client.player);
      final BackpackMenu menu = new BackpackMenu(packet.getSyncId(), player.inventory, rows, columns);
      final BackpackScreen screen = new BackpackScreen(menu, player.inventory, packet.getName());
      player.currentScreenHandler = menu;
      this.client.openScreen(screen);
      ci.cancel();
    }
  }
}
