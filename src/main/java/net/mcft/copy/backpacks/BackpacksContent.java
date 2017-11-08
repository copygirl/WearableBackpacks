package net.mcft.copy.backpacks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.world.storage.loot.LootTableList;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import net.mcft.copy.backpacks.api.BackpackRegistry;
import net.mcft.copy.backpacks.api.BackpackRegistry.ColorRange;
import net.mcft.copy.backpacks.api.BackpackRegistry.RenderOptions;
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
			
			BackpackRegistry.registerEntity("minecraft:zombie",        RenderOptions.DEFAULT);
			BackpackRegistry.registerEntity("minecraft:skeleton",      RenderOptions.DEFAULT);
			BackpackRegistry.registerEntity("minecraft:zombie_pigman", RenderOptions.DEFAULT);
			
			String backpack  = BACKPACK.getRegistryName().toString();
			String idDefault = WearableBackpacks.MOD_ID + ":default";
			String idColored = WearableBackpacks.MOD_ID + ":colored";
			String lootTable = ItemBackpack.LOOT_TABLE.toString();
			
			BackpackRegistry.registerBackpack("minecraft:zombie",        idDefault, backpack,   800, lootTable, null);
			BackpackRegistry.registerBackpack("minecraft:zombie",        idColored, backpack,  8000, lootTable, ColorRange.DEFAULT);
			BackpackRegistry.registerBackpack("minecraft:skeleton",      idDefault, backpack,  1200, lootTable, null);
			BackpackRegistry.registerBackpack("minecraft:skeleton",      idColored, backpack, 12000, lootTable, ColorRange.DEFAULT);
			BackpackRegistry.registerBackpack("minecraft:zombie_pigman", idColored, backpack,  1000, lootTable, ColorRange.DEFAULT);
			
			// TODO: Register all loot tables mentioned in config file?
			LootTableList.register(ItemBackpack.LOOT_TABLE);
		}
		
		// BackpackRegistry.registerBackpackEntity(EntityEnderman.class, ENDER_BACKPACK, 1.0 / 80);
	}
	
	public void registerRecipes() {
		ForgeRegistries.RECIPES.register(new RecipeDyeableItem()
			.setRegistryName(WearableBackpacks.MOD_ID, "dyeable_item"));
	}
	
}
