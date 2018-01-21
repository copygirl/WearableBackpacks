package net.mcft.copy.backpacks.misc.util;

import java.lang.reflect.Method;
import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import net.mcft.copy.backpacks.WearableBackpacks;

public final class IntermodUtils {
	
	private IntermodUtils() {  }
	
	private static final int DEFAULT_ENCHANTMENT_COLOR = 0xFF8040CC;
	private static boolean getRuneColorCached  = false;
	private static Method setTargetStackMethod = null;
	private static Method getColorMethod       = null;
	/** Returns the Quark rune color for this item or the default enchant glint
	 *  color if Quark isn't present or the item doesn't have a custom rune color. */
	public static int getRuneColor(ItemStack stack) {
		if (!getRuneColorCached) {
			if (Loader.isModLoaded("quark")) {
				try {
					Class<?> clazz = Class.forName("vazkii.quark.misc.feature.ColorRunes");
					setTargetStackMethod = clazz.getMethod("setTargetStack", ItemStack.class);
					getColorMethod = Arrays.stream(clazz.getMethods())
						.filter(m -> "getColor".equals(m.getName()))
						.findAny().orElse(null);
				} catch (ClassNotFoundException |
				         NoSuchMethodException ex) {
					WearableBackpacks.LOG.error("Error while fetching Quark ColorRunes methods", ex);
				}
			}
			getRuneColorCached = true;
		}
		if ((setTargetStackMethod == null) || (getColorMethod == null))
			return DEFAULT_ENCHANTMENT_COLOR;
		try {
			setTargetStackMethod.invoke(null, stack);
			return (getColorMethod.getParameterCount() == 0)
				? (int)getColorMethod.invoke(null)
				: (int)getColorMethod.invoke(null, DEFAULT_ENCHANTMENT_COLOR);
		} catch (Exception ex) {
			WearableBackpacks.LOG.error("Error while invoking Quark ColorRunes methods", ex);
			setTargetStackMethod = null;
			getColorMethod       = null;
			return DEFAULT_ENCHANTMENT_COLOR;
		}
	}
	
}
