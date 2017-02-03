package net.mcft.copy.backpacks.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;

/** Sent to the server when the client presses the "open backpack" key. */
public class MessageOpenBackpack implements IMessage {
	
	public MessageOpenBackpack() {  }
	
	public static MessageOpenBackpack create() { return new MessageOpenBackpack(); }
	
	@Override
	public void fromBytes(ByteBuf buf) {  }
	@Override
	public void toBytes(ByteBuf buf) {  }
	
	public static class Handler extends BackpacksMessageHandler<MessageOpenBackpack> {
		@Override
		public void handle(MessageOpenBackpack message, MessageContext ctx) {
			EntityPlayer player = getPlayer(ctx);
			IBackpack backpack = BackpackHelper.getBackpack(player);
			if ((backpack != null) && player.isEntityAlive() &&
			    WearableBackpacks.CONFIG.enableSelfInteraction.get())
				backpack.getType().onEquippedInteract(player, player, backpack);
		}
	}
	
}
