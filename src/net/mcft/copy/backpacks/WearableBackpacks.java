package net.mcft.copy.backpacks;

import net.mcft.copy.backpacks.content.BackpackBlocks;
import net.mcft.copy.backpacks.content.BackpackRecipes;
import net.mcft.copy.backpacks.proxy.CommonProxy;
import net.mcft.copy.core.config.ContentConfig;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = WearableBackpacks.MOD_ID, version = "${version}",
     dependencies = "required-after:copycore")
public class WearableBackpacks
{
	
	public static final String MOD_ID = "WearableBackpacks";
	
	@SidedProxy(clientSide = "net.mcft.copy.backpacks.proxy.ClientProxy",
	            serverSide = "net.mcft.copy.backpacks.proxy.CommonProxy")
	private static CommonProxy proxy;

	public static Logger log;
	public static ContentConfig config;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		
		log = event.getModLog();
		
		config = new BackpacksConfig(event.getSuggestedConfigurationFile());
		config.addContentSettingsViaReflection("blocks", BackpackBlocks.class);
		
		config.load();
		config.update();
		config.save();
		
		BackpackBlocks.register(config);
		
		proxy.init();
		
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		
		BackpackRecipes.register();
		
	}
	
}
