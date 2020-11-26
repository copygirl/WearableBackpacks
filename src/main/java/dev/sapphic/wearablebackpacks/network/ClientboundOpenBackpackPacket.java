package dev.sapphic.wearablebackpacks.network;

import dev.sapphic.wearablebackpacks.inventory.BackpackMenu;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.text.Text;

import java.io.IOException;

public final class ClientboundOpenBackpackPacket extends OpenScreenS2CPacket {
  private int rows;
  private int columns;

  public ClientboundOpenBackpackPacket(final Text name, final BackpackMenu menu) {
    super(menu.syncId, menu.getType(), name);
    this.rows = menu.getRows();
    this.columns = menu.getColumns();
  }

  @Override
  public void read(final PacketByteBuf buf) throws IOException {
    super.read(buf);
    this.rows = buf.readByte();
    this.columns = buf.readByte();
  }

  @Override
  public void write(final PacketByteBuf buf) throws IOException {
    super.write(buf);
    buf.writeByte(this.rows);
    buf.writeByte(this.columns);
  }

  public int getRows() {
    return this.rows;
  }

  public int getColumns() {
    return this.columns;
  }
}
