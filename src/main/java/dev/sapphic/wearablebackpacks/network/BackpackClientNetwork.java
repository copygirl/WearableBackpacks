package dev.sapphic.wearablebackpacks.network;

import dev.sapphic.wearablebackpacks.Backpacks;
import dev.sapphic.wearablebackpacks.client.BackpackWearer;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public final class BackpackClientNetwork implements ClientModInitializer {
  static final Identifier OPEN_OWN_BACKPACK = new Identifier(Backpacks.ID, "open_own_backpack");

  private static final PacketByteBuf EMPTY_BUFFER = new PacketByteBuf(Unpooled.EMPTY_BUFFER);

  public static void tryOpenOwnBackpack() {
    ClientPlayNetworking.send(OPEN_OWN_BACKPACK, EMPTY_BUFFER);
  }

  @Override
  public void onInitializeClient() {
    ClientPlayNetworking.registerGlobalReceiver(
            BackpackServerNetwork.BACKPACK_UPDATED, (client, handler, buf, sender) -> {
              final int entityId = buf.readInt();
              final int openCount = buf.readInt();

              client.execute(() -> {
                if (client.world != null) {
                  final Entity entity = client.world.getEntityById(entityId);
                  if (entity instanceof LivingEntity) {
                    BackpackWearer.getBackpackState((LivingEntity) entity).count(openCount);
                  }
                }
              });
            });
  }
}
