package net.mcft.copy.backpacks.content;

import net.mcft.copy.backpacks.block.BlockBackpack;
import net.mcft.copy.backpacks.item.ItemBackpack;
import net.mcft.copy.core.config.ContentConfig;
import net.mcft.copy.core.util.RegistryUtils;

public final class BackpackBlocks {
	
	public static BlockBackpack backpack = new BlockBackpack(ItemBackpack.class);
	
	private BackpackBlocks() {  }
	
	public static void register(ContentConfig config) {
		backpack = RegistryUtils.registerIfEnabled(backpack, config);
	}
	
}
