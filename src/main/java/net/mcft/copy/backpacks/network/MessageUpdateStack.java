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

public class MessageUpdateStack implements IMessage {
	
	private int _entityId;
	private ItemStack _stack;
	
	public MessageUpdateStack() {  }
	
	public MessageUpdateStack(Entity entity, ItemStack stack) {
		_entityId = entity.getEntityId();
		_stack = stack;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		try {
			_entityId = buffer.readInt();
			_stack = buffer.readItemStackFromBuffer();
		} catch (Exception ex) {
			_entityId = -1;
			_stack = null;
		}
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		buffer.writeInt(_entityId);
		buffer.writeItemStackToBuffer(_stack);
	}
	
	public static class Handler extends BackpacksMessageHandler<MessageUpdateStack> {
		@Override
		@SideOnly(Side.CLIENT)
		public void handle(MessageUpdateStack message, MessageContext ctx) {
			Entity entity = getWorld(ctx).getEntityByID(message._entityId);
			if (entity == null) return;
			BackpackCapability backpack = (BackpackCapability)entity
				.getCapability(IBackpack.CAPABILITY, null);
			if (backpack == null) return;
			backpack.stack = message._stack;
		}
	}
	
}
