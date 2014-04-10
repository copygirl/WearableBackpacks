package net.mcft.copy.backpacks.content;

import net.mcft.copy.backpacks.block.BlockBackpack;
import net.mcft.copy.backpacks.item.ItemBackpack;
import cpw.mods.fml.common.registry.GameRegistry;

public final class BackpackBlocks {
	
	public static final BlockBackpack backpack = new BlockBackpack();
	
	private BackpackBlocks() {  }
	
	public static void register() {
		
		GameRegistry.registerBlock(backpack, ItemBackpack.class, "block.backpack");
		
	}
	
}
