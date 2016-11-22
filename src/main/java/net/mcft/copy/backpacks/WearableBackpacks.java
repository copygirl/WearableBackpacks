package net.mcft.copy.backpacks;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.SidedProxy;

import net.mcft.copy.backpacks.network.BackpacksChannel;

// TODO: Add more documentation / comments to source code.

@Mod(modid = WearableBackpacks.MOD_ID, name = WearableBackpacks.MOD_NAME, version = "${version}")
public class WearableBackpacks {
	
	public static final String MOD_ID   = "wearablebackpacks";
	public static final String MOD_NAME = "Wearable Backpacks";
	
	@Instance
	public static WearableBackpacks INSTANCE;
	
	@SidedProxy(serverSide = "net.mcft.copy.backpacks.ProxyCommon",
	            clientSide = "net.mcft.copy.backpacks.ProxyClient")
	public static ProxyCommon PROXY;
	
	public static Logger LOG;
	public static BackpacksConfig CONFIG;
	public static BackpacksChannel CHANNEL;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LOG = event.getModLog();
		CONFIG = new BackpacksConfig();
		CHANNEL = new BackpacksChannel();
		BackpacksContent.init();
		PROXY.preInit();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		PROXY.init();
		BackpacksContent.initRecipes();
	}
	
}
