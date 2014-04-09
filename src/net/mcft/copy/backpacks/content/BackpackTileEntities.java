package net.mcft.copy.backpacks.content;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.block.tileentity.TileEntityBackpack;
import cpw.mods.fml.common.registry.GameRegistry;

public final class BackpackTileEntities {
	
	private BackpackTileEntities() {  }
	
	public static void register() {
		
		GameRegistry.registerTileEntity(TileEntityBackpack.class, WearableBackpacks.MOD_ID + ":backpack");
		
	}
	
}
