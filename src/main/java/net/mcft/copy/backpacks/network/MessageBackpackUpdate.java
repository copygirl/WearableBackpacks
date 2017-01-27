package net.mcft.copy.backpacks.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.misc.BackpackCapability;

/** Sends and updates equipped backpack capability data. */
public class MessageBackpackUpdate implements IMessage {
	
	private int _entityId;
	private UpdateType _type;
	private ItemStack _stack;
	private boolean _open;
	
	public MessageBackpackUpdate() {  }
	
	public static MessageBackpackUpdate stack(Entity entity, ItemStack stack) {
		MessageBackpackUpdate message = new MessageBackpackUpdate();
		message._entityId = entity.getEntityId();
		message._type = UpdateType.STACK;
		message._stack = stack;
		return message;
	}
	public static MessageBackpackUpdate open(Entity entity, boolean open) {
		MessageBackpackUpdate message = new MessageBackpackUpdate();
		message._entityId = entity.getEntityId();
		message._type = UpdateType.OPEN;
		message._open = open;
		return message;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		try {
			_entityId = buffer.readInt();
			_type = UpdateType.fromByte(buffer.readByte());
			switch (_type) {
				case STACK: _stack = buffer.readItemStack(); break;
				case OPEN: _open = buffer.readBoolean(); break;
				default: throw new RuntimeException("Invalid UpdateType");
			}
		} catch (Exception ex) {
			_entityId = -1;
			_type = UpdateType.INVALID;
			_stack = null;
			_open = false;
		}
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		buffer.writeInt(_entityId);
		buffer.writeByte(_type.ordinal());
		switch (_type) {
			case STACK: buffer.writeItemStack(_stack); break;
			case OPEN: buffer.writeBoolean(_open); break;
			default: throw new RuntimeException("Invalid UpdateType");
		}
	}
	
	public static class Handler extends BackpacksMessageHandler<MessageBackpackUpdate> {
		@Override
		@SideOnly(Side.CLIENT)
		public void handle(MessageBackpackUpdate message, MessageContext ctx) {
			Entity entity = getWorld(ctx).getEntityByID(message._entityId);
			if (entity == null) return;
			BackpackCapability backpack = (BackpackCapability)entity
				.getCapability(IBackpack.CAPABILITY, null);
			if (backpack == null) return;
			switch (message._type) {
				case STACK: backpack.stack = message._stack; break;
				case OPEN: backpack.playersUsing = (message._open ? 1 : 0); break;
				default: throw new RuntimeException("Invalid UpdateType");
			}
		}
	}
	
	private enum UpdateType {
		INVALID, STACK, OPEN;
		
		public static UpdateType fromByte(byte b) {
			switch (b) {
				default: return INVALID;
				case 1: return STACK;
				case 2: return OPEN;
			}
		}
	}
	
}
