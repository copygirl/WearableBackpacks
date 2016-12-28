package net.mcft.copy.backpacks;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;

import net.mcft.copy.backpacks.block.BlockBackpack;
import net.mcft.copy.backpacks.block.entity.TileEntityBackpack;
import net.mcft.copy.backpacks.item.ItemBackpack;
import net.mcft.copy.backpacks.item.recipe.RecipeDyeableItem;

public final class BackpacksContent {
	
	private BackpacksContent() {  }
	
	
	public static ItemBackpack BACKPACK;
	
	
	public static void init() {
		
		if (WearableBackpacks.CONFIG.backpackEnabled.getValue()) {
			BACKPACK = new ItemBackpack();
			Block backpackBlock = new BlockBackpack();
			GameRegistry.register(BACKPACK.setRegistryName("backpack"));
			GameRegistry.register(backpackBlock.setRegistryName("backpack"));
			GameRegistry.registerTileEntity(TileEntityBackpack.class, "wearablebackpacks:backpack");
		}
		
		// TODO: Register entities to spawn with backpacks.
		//BackpackRegistry.registerBackpackEntity(EntityZombie.class, BACKPACK, 1.0 / 800);
		//BackpackRegistry.registerBackpackEntity(EntitySkeleton.class, BACKPACK, 1.0 / 1200);
		//BackpackRegistry.registerBackpackEntity(EntityPigZombie.class, BACKPACK, 1.0 / 1000);
		//BackpackRegistry.registerBackpackEntity(EntityEnderman.class, BACKPACK, 1.0 / 80);
		
	}
	
	public static void initRecipes() {
		
		if (BACKPACK != null)
			GameRegistry.addRecipe(new ShapedOreRecipe(BACKPACK,
				"LGL",
				"LWL",
				"LLL", 'L', "leather",
				       'G', "ingotGold",
				       'W', Blocks.WOOL));
		
		GameRegistry.addRecipe(new RecipeDyeableItem());
		RecipeSorter.register("wearablebackpacks:dyeable",
			RecipeDyeableItem.class, RecipeSorter.Category.SHAPELESS, "");
		
	}
	
}
