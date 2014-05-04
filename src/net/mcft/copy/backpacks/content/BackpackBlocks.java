package net.mcft.copy.backpacks.content;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.block.BlockBackpack;
import net.mcft.copy.backpacks.item.ItemBackpack;
import net.mcft.copy.core.util.RegistryUtils;

public final class BackpackBlocks {
	
	public static BlockBackpack backpack;
	
	private BackpackBlocks() {  }
	
	public static void register() {
		
		backpack = RegistryUtils.registerIfEnabled(WearableBackpacks.config, "blocks", new BlockBackpack(ItemBackpack.class));
		
	}
	
}
