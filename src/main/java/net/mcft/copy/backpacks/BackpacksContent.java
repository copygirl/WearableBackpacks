package net.mcft.copy.backpacks;

import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.Item;
import net.minecraft.world.storage.loot.LootTableList;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import net.mcft.copy.backpacks.api.BackpackRegistry;
import net.mcft.copy.backpacks.block.BlockBackpack;
import net.mcft.copy.backpacks.block.entity.TileEntityBackpack;
import net.mcft.copy.backpacks.item.ItemBackpack;
import net.mcft.copy.backpacks.item.recipe.RecipeDyeableItem;

public class BackpacksContent {
	
	public static ItemBackpack BACKPACK;
	
	public BackpacksContent() {
		if (WearableBackpacks.CONFIG.backpack.enabled.get())
			BACKPACK = new ItemBackpack();
	}
	
	@SubscribeEvent
	public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		if (BACKPACK != null) {
			event.getRegistry().register(
				new BlockBackpack().setRegistryName(WearableBackpacks.MOD_ID, "backpack"));
			GameRegistry.registerTileEntity(TileEntityBackpack.class, WearableBackpacks.MOD_ID + ":backpack");
		}
	}
	
	@SubscribeEvent
	public void onRegisterItems(RegistryEvent.Register<Item> event) {
		if (BACKPACK != null) {
			event.getRegistry().register(
				BACKPACK.setRegistryName(WearableBackpacks.MOD_ID, "backpack"));
			
			BackpackRegistry.registerBackpackEntity(EntityZombie.class, BACKPACK, 1.0 / 800);
			BackpackRegistry.registerBackpackEntity(EntitySkeleton.class, BACKPACK, 1.0 / 1200);
			BackpackRegistry.registerBackpackEntity(EntityPigZombie.class, BACKPACK, 1.0 / 1000);
			
			LootTableList.register(ItemBackpack.LOOT_TABLE);
		}
		
		// BackpackRegistry.registerBackpackEntity(EntityEnderman.class, ENDER_BACKPACK, 1.0 / 80);
	}
	
	public void registerRecipes() {
		ForgeRegistries.RECIPES.register(new RecipeDyeableItem()
			.setRegistryName(WearableBackpacks.MOD_ID, "dyeable_item"));
	}
	
}
