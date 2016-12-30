package net.mcft.copy.backpacks.misc.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;

/** Contains useful client related utility methods. Allows them to
 *  be referenced in universal code, without causing client-only
 *  classes to be loaded until the methods are actually called. */
public final class ClientUtils {
	
	private ClientUtils() {  }
	
	
	/** Returns the local (client) player entity. Will crash if called on the server. */
	public static EntityPlayer getPlayer() { return getMC().player; }
	
	/** Returns the local (client) world. Will crash if called on the server. */
	public static World getWorld() { return getMC().world; }
	
	/** Returns the local (client) thread scheduler, which allows tasks to be
	 *  scheduled on the main game thread. Will crash if called on the server. */
	public static IThreadListener getScheduler() { return getMC(); }
	
	
	private static Minecraft getMC() { return Minecraft.getMinecraft(); }
	
}