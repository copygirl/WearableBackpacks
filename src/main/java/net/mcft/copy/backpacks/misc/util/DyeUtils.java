package net.mcft.copy.backpacks.misc.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

/** Contains utility methods related to dyes. */
public final class DyeUtils {
	
	private DyeUtils() {  }
	
	
	private static final Map<String, Integer> dyes = new HashMap<String, Integer>();
	static {
		// Collect dye colors into map using the ore dictionary name as a key.
		for (EnumDyeColor color : EnumDyeColor.values()) {
			String name = color.getUnlocalizedName();
			name = "dye" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
			float[] values = EntitySheep.getDyeRgb(color);
			int r = (int)(values[0] * 255);
			int g = (int)(values[1] * 255);
			int b = (int)(values[2] * 255);
			dyes.put(name, ((r << 16) | (g << 8) | b));
		}
	};
	
	
	/** Gets the dye color of the item stack by checking the ore dictionary.
	 *  Return -1 if the stack is not a dye. */
	public static int getDyeColor(ItemStack stack) {
		if (stack.isEmpty()) return -1;
		int[] oreIds = OreDictionary.getOreIDs(stack);
		for (int ore : oreIds) {
			String name = OreDictionary.getOreName(ore);
			Integer color = dyes.get(name);
			if (color != null) return color;
		}
		return -1;
	}
	
	/** Returns if the item stack is a dye. */
	public static boolean isDye(ItemStack stack) {
		return (getDyeColor(stack) >= 0);
	}
	
	/** Returns the combined color of all the dyes and the base color. */
	public static int getColorFromDyes(int color, Collection<ItemStack> dyes) {
		int number = dyes.size();
		if (number < 1) return color;
		int r = 0, g = 0, b = 0;
		if (color >= 0) {
			r = (color >> 16);
			g = ((color >> 8) & 0xFF);
			b = (color & 0xFF);
			number++;
		}
		for (ItemStack dye : dyes) {
			color = getDyeColor(dye);
			if (color < 0) continue;
			r += (color >> 16);
			g += ((color >> 8) & 0xFF);
			b += (color & 0xFF);
		}
		r /= number;
		g /= number;
		b /= number;
		return ((r << 16) | (g << 8) | b);
	}
	/** Returns the combined color of all the dyes. */
	public static int getColorFromDyes(Collection<ItemStack> dyes) {
		return getColorFromDyes(-1, dyes);
	}
	
}
