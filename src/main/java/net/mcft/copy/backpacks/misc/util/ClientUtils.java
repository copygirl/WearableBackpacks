package net.mcft.copy.backpacks.misc.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;

public final class ClientUtils {
	
	private ClientUtils() {  }
	
	public static EntityPlayer getPlayer() { return getMC().thePlayer; }
	
	public static World getWorld() { return getMC().theWorld; }
	
	public static IThreadListener getScheduler() { return getMC(); }
	
	private static Minecraft getMC() { return Minecraft.getMinecraft(); }
	
}