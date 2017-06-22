package net.mcft.copy.backpacks;

import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.storage.loot.LootTableList;

import net.minecraftforge.fml.common.registry.GameRegistry;

import net.mcft.copy.backpacks.api.BackpackRegistry;
import net.mcft.copy.backpacks.block.BlockBackpack;
import net.mcft.copy.backpacks.block.entity.TileEntityBackpack;
import net.mcft.copy.backpacks.item.ItemBackpack;

public final class BackpacksContent {
	
	private BackpacksContent() {  }
	
	
	public static ItemBackpack BACKPACK;
	
	
	public static void init() {
		
		if (WearableBackpacks.CONFIG.backpack.enabled.get()) {
			BACKPACK = new ItemBackpack();
			Block backpackBlock = new BlockBackpack();
			
			GameRegistry.register(BACKPACK.setRegistryName("backpack"));
			GameRegistry.register(backpackBlock.setRegistryName("backpack"));
			GameRegistry.registerTileEntity(TileEntityBackpack.class, "wearablebackpacks:backpack");
			
			BackpackRegistry.registerBackpackEntity(EntityZombie.class, BACKPACK, 1.0 / 800);
			BackpackRegistry.registerBackpackEntity(EntitySkeleton.class, BACKPACK, 1.0 / 1200);
			BackpackRegistry.registerBackpackEntity(EntityPigZombie.class, BACKPACK, 1.0 / 1000);
			
			LootTableList.register(ItemBackpack.LOOT_TABLE);
		}
		
		// BackpackRegistry.registerBackpackEntity(EntityEnderman.class, ENDER_BACKPACK, 1.0 / 80);
		
	}
	
	// FIXME: Add JSON recipe.
	
}
