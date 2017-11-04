package net.mcft.copy.backpacks.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import net.mcft.copy.backpacks.misc.util.ClientUtils;

public abstract class BackpacksMessageHandler<T extends IMessage> implements IMessageHandler<T, IMessage> {
	
	/** Returns whether the message handling should be scheduled, meaning it
	 *  will be executed on the world thread instead of the network thread. */
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
	
	/** Returns the player associated with this message context.
	 *  On the server, returns the player who sent the message.
	 *  On the client, returns the local player entity. */
	public static EntityPlayer getPlayer(MessageContext ctx)
		{ return (ctx.side.isServer() ? ctx.getServerHandler().player : ClientUtils.getPlayer()); }
	
	/** Returns the world associated with this message context.
	 *  On the server, returns the world the player is in who sent the message.
	 *  On the client, returns the local world. */
	public static World getWorld(MessageContext ctx)
		{ return getPlayer(ctx).world; }
	
	/** Returns the appropriate thread scheduler for this message context.
	 *  On the server, returns the world the player is in (world's thread).
	 *  On the client, returns the Minecraft instance (main game thread). */
	public static IThreadListener getScheduler(MessageContext ctx)
		{ return (ctx.side.isServer() ? (WorldServer)getWorld(ctx) : ClientUtils.getScheduler()); }
	
}
