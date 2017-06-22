package net.mcft.copy.backpacks;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.SidedProxy;

import net.mcft.copy.backpacks.config.BackpacksConfig;
import net.mcft.copy.backpacks.network.BackpacksChannel;

// TODO: Add achievement(s)! <3

@Mod(modid = WearableBackpacks.MOD_ID, name = WearableBackpacks.MOD_NAME,
     version = WearableBackpacks.VERSION,
     guiFactory = "net.mcft.copy.backpacks.client.BackpacksGuiFactory")
public class WearableBackpacks {
	
	public static final String MOD_ID   = "wearablebackpacks";
	public static final String MOD_NAME = "Wearable Backpacks";
	public static final String VERSION  = "@VERSION@";
	
	@Instance
	public static WearableBackpacks INSTANCE;
	
	@SidedProxy(serverSide = "net.mcft.copy.backpacks.ProxyCommon",
	            clientSide = "net.mcft.copy.backpacks.ProxyClient")
	public static ProxyCommon PROXY;
	
	public static Logger LOG;
	public static BackpacksChannel CHANNEL;
	public static BackpacksConfig CONFIG;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LOG = event.getModLog();
		CHANNEL = new BackpacksChannel();
		
		CONFIG = new BackpacksConfig(event.getSuggestedConfigurationFile());
		CONFIG.load();
		CONFIG.save();
		
		BackpacksContent.init();
		CONFIG.init();
		PROXY.preInit();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		PROXY.init();
	}
	
}
