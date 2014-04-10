package net.mcft.copy.backpacks.content;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;

public final class BackpackRecipes {
	
	private BackpackRecipes() {  }
	
	public static void register() {
		
		GameRegistry.addRecipe(new ItemStack(BackpackBlocks.backpack),
				"#i#",
				"#o#",
				"###", 'i', Items.gold_ingot,
				       'o', Blocks.wool,
				       '#', Items.leather);
		
	}
	
}
