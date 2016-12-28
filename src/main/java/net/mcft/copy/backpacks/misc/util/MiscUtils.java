package net.mcft.copy.backpacks.misc.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public final class MiscUtils {
	
	private MiscUtils() {  }
	
	/** Returns the block associated with the specified resource location. */
	public static Block getBlockFromName(ResourceLocation location) { return Block.REGISTRY.getObject(location); }
	/** Returns the block associated with the specified resource location name (for example "minecraft:cobblestone"). */
	public static Block getBlockFromName(String name) { return getBlockFromName(new ResourceLocation(name)); }
	/** Returns the block associated with the specified item. */
	public static Block getBlockFromItem(Item item) { return getBlockFromName(item.getRegistryName()); }
	
	/** Returns the item associated with the specified resource location. */
	public static Item getItemFromName(ResourceLocation location) { return Item.REGISTRY.getObject(location); }
	/** Returns the item associated with the specified resource location name (for example "minecraft:stick"). */
	public static Item getItemFromName(String name) { return getItemFromName(new ResourceLocation(name)); }
	/** Returns the item associated with the specified block. */
	public static Item getItemFromBlock(Block block) { return Item.REGISTRY.getObject(block.getRegistryName()); }
	
}
