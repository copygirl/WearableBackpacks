package net.mcft.copy.backpacks.network;

import java.util.List;

import net.minecraft.world.WorldServer;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import net.mcft.copy.backpacks.WearableBackpacks;

public class BackpacksChannel extends SimpleNetworkWrapper {
	
	public BackpacksChannel() {
		super(WearableBackpacks.MOD_ID);
		
		registerMessage(MessageOpenGui.Handler.class, MessageOpenGui.class, 0, Side.CLIENT);
		registerMessage(MessageBackpackUpdate.Handler.class, MessageBackpackUpdate.class, 1, Side.CLIENT);
	}
	
	/** Sends a message to a player. */
	public void sendTo(IMessage message, EntityPlayer player) {
		sendTo(message, (EntityPlayerMP)player);
	}
	
	/** Sends a message to everyone around a point. */
	public void sendToAllAround(IMessage message, World world, double x, double y, double z, double distance) {
		sendToAllAround(message, new TargetPoint(world.provider.getDimension(), x, y, z, distance));
	}
	
	/** Sends a message to everyone around a point except a specific player. */
	public void sendToAllAround(IMessage message, World world, double x, double y, double z,
	                            double distance, EntityPlayer except) {
		for (EntityPlayer player : (List<EntityPlayer>)world.playerEntities) {
			if (player == except) continue;
			double dx = x - player.posX;
			double dy = y - player.posY;
			double dz = z - player.posZ;
			if ((dx * dx + dy * dy + dz * dz) < (distance * distance))
				sendTo(message, player);
		}
	}
	
	/** Sends a message to a everyone tracking an entity. If sendToEntity is
	 *  true and the entity is a player, also sends the message to them. */
	public void sendToAllTracking(IMessage message, Entity entity, boolean sendToEntity) {
		((WorldServer)entity.worldObj).getEntityTracker()
			.sendToAllTrackingEntity(entity, getPacketFrom(message));
		if (sendToEntity && (entity instanceof EntityPlayer))
			sendTo(message, (EntityPlayer)entity);
	}
	
}
