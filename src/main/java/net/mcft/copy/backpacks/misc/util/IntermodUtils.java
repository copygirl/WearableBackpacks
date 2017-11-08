package net.mcft.copy.backpacks.misc.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

public final class IntermodUtils {
	
	private IntermodUtils() {  }
	
	private static boolean getRuneColorCached = false;
	private static Method setTargetStackMethod;
	private static Method getColorMethod;
	/** Returns the Quark rune color for this item or the default enchant glint
	 *  color if Quark isn't present or the item doesn't have a custom rune color. */
	public static int getRuneColor(ItemStack stack) {
		if (!getRuneColorCached) {
			if (Loader.isModLoaded("quark")) {
				try {
					Class<?> clazz = Class.forName("vazkii.quark.misc.feature.ColorRunes");
					setTargetStackMethod = clazz.getMethod("setTargetStack", ItemStack.class);
					getColorMethod = clazz.getMethod("getColor");
				} catch (ClassNotFoundException |
				         NoSuchMethodException ex) {
					WearableBackpacks.LOG.error("Error while fetching Quark ColorRunes methods", ex);
				}
			} else {
				setTargetStackMethod = null;
				getColorMethod = null;
			}
			getRuneColorCached = true;
		}
		if (setTargetStackMethod == null)
			return 0xFF8040CC;
		try {
			setTargetStackMethod.invoke(null, stack);
			return (int)getColorMethod.invoke(null);
		} catch (IllegalAccessException |
		         InvocationTargetException ex) {
			WearableBackpacks.LOG.error("Error while invoking Quark ColorRunes methods", ex);
			setTargetStackMethod = null;
			getColorMethod = null;
			return 0xFF8040CC;
		}
	}
	
}
