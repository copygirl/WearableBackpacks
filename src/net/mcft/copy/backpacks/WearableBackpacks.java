package net.mcft.copy.backpacks;

import net.mcft.copy.backpacks.content.BackpackBlocks;
import net.mcft.copy.backpacks.content.BackpackEntities;
import net.mcft.copy.backpacks.content.BackpackItems;
import net.mcft.copy.backpacks.content.BackpackRecipes;
import net.mcft.copy.backpacks.content.BackpackTileEntities;
import net.mcft.copy.backpacks.proxy.CommonProxy;
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
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		
		proxy.init();
		
		BackpackBlocks.register();
		BackpackItems.register();
		BackpackEntities.register();
		BackpackTileEntities.register();
		
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		
		BackpackRecipes.register();
		
	}
	
}
