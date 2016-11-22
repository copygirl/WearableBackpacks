package net.mcft.copy.backpacks.network;

import net.mcft.copy.backpacks.misc.util.ClientUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class BackpacksMessageHandler<T extends IMessage> implements IMessageHandler<T, IMessage> {
	
	public boolean isScheduled() { return true; }
	
	public abstract void handle(T message, MessageContext ctx);
	
	// IMessageHandler implementation
	
	@Override
	public final IMessage onMessage(T message, MessageContext ctx) {
		if (!isScheduled()) handle(message, ctx);
		else getScheduler(ctx).addScheduledTask(() -> handle(message, ctx));
		return null;
	}
	
	// Utility methods
	
	public static EntityPlayer getPlayer(MessageContext ctx) {
		return (ctx.side.isServer() ? ctx.getServerHandler().playerEntity : ClientUtils.getPlayer());
	}
	
	public static World getWorld(MessageContext ctx) {
		return getPlayer(ctx).worldObj;
	}
	
	public static IThreadListener getScheduler(MessageContext ctx) {
		return (ctx.side.isServer() ? (WorldServer)getWorld(ctx) : ClientUtils.getScheduler());
	}
	
}
