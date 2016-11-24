package net.mcft.copy.backpacks;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;

import net.mcft.copy.backpacks.block.BlockBackpack;
import net.mcft.copy.backpacks.block.entity.TileEntityBackpack;
import net.mcft.copy.backpacks.item.ItemBackpack;
import net.mcft.copy.backpacks.item.recipe.RecipeDyeableItem;

public final class BackpacksContent {
	
	private BackpacksContent() {  }
	
	
	public static BlockBackpack BACKPACK;
	
	
	public static void init() {
		
		BACKPACK = register("backpack", BlockBackpack.class, ItemBackpack.class,
		                    WearableBackpacks.CONFIG.backpackEnabled.getValue());
		
		if (BACKPACK != null)
			GameRegistry.registerTileEntity(TileEntityBackpack.class, "wearablebackpacks:backpack");
		
		// TODO: Register entities to spawn with backpacks.
		// BackpackRegistry.registerBackpackEntity(EntityZombie.class, BACKPACK, 1.0 / 800);
		// BackpackRegistry.registerBackpackEntity(EntitySkeleton.class, BACKPACK, 1.0 / 1200);
		// BackpackRegistry.registerBackpackEntity(EntityPigZombie.class, BACKPACK, 1.0 / 1000);
		// BackpackRegistry.registerBackpackEntity(EntityEnderman.class, BACKPACK, 1.0 / 80);
		
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
	
	
	@SuppressWarnings("unchecked")
	private static <T extends Block> T register(String registryName, Class<? extends Block> blockClass,
	                                            Class<? extends ItemBlock> itemClass, boolean doRegister) {
		if (!doRegister) return null;
		try {
			T block = (T)blockClass.newInstance();
			return register(registryName, block, (ItemBlock)itemClass.getConstructors()[0].newInstance(block));
		} catch (InstantiationException | IllegalAccessException |
		         InvocationTargetException ex) { throw new RuntimeException(ex); }
	}
	private static <T extends Block> T register(String registryName, T block, ItemBlock itemBlock) {
		block.setRegistryName(registryName);
		GameRegistry.register(block);
		if (itemBlock != null)
			register(registryName, itemBlock);
		return block;
	}
	
	private static <T extends Item> T register(String registryName, T item) {
		item.setRegistryName(registryName);
		GameRegistry.register(item);
		return item;
	}
	
}
