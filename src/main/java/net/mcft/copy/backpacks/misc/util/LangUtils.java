package net.mcft.copy.backpacks.misc.util;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;

/** Contains utility methods related to language files / localization. */
public final class LangUtils {
	
	private LangUtils() {  }
	
	
	/** Formats a language key and returns it as a list. */
	@SideOnly(Side.CLIENT)
	public static List<String> format(String langKey, Object... args)
		{ return formatPrepend("", langKey, args); }
	/** Formats a language key and adds it to the specified
	 *  list, prepending each line with the specified string. */
	@SideOnly(Side.CLIENT)
	public static void format(List<String> list, String langKey, Object... args)
		{ formatPrepend(list, "", langKey, args); }
	
	/** Formats a language key and returns it as a list. */
	@SideOnly(Side.CLIENT)
	public static List<String> formatPrepend(String prepend, String langKey, Object... args) {
		ArrayList<String> list = new ArrayList<String>();
		formatPrepend(list, prepend, langKey, args);
		return list;
	}
	/** Formats a language key and adds it to the specified
	 *  list, prepending each line with the specified string. */
	@SideOnly(Side.CLIENT)
	public static void formatPrepend(List<String> list, String prepend, String langKey, Object... args) {
		String translated = I18n.format(langKey, args);
		for (String line : translated.split("\\\\n"))
			list.add(prepend + line);
	}
	
	
	/** Formats a tooltip translation key (<code> "tooltip.modid.key" </code>)
	 *  and adds it to the tooltip list. Used in {@link Item#addInformation}. */
	@SideOnly(Side.CLIENT)
	public static void formatTooltip(List<String> tooltip, String tooltipKey, Object... args)
		{ format(tooltip, "tooltip." + WearableBackpacks.MOD_ID + "." + tooltipKey, args); }
	/** Formats a tooltip translation key (<code> "tooltip.modid.key" </code>)
	 *  and adds it to the tooltip list, prepending each line with the specified
	 *  string. Used in {@link Item#addInformation}. */
	@SideOnly(Side.CLIENT)
	public static void formatTooltipPrepend(List<String> tooltip, String prepend, String tooltipKey, Object... args)
		{ formatPrepend(tooltip, prepend, "tooltip." + WearableBackpacks.MOD_ID + "." + tooltipKey, args); }
	
	/** Formats a tooltip translation key containing a single
	 *  key binding argument and adds it to the tooltip list.
	 *  Doesn't do anything if key is unbound. */
	@SideOnly(Side.CLIENT)
	public static void formatTooltipKey(List<String> tooltip, String langKey, KeyBinding keyBinding) {
		if (keyBinding.getKeyCode() == Keyboard.KEY_NONE) return;
		formatTooltip(tooltip, langKey, "\u00A76" + keyBinding.getDisplayName() + "\u00A77");
	}
	
	/** If shift is not held down, adds a formatted "Hold SHIFT for more info" tooltip
	 *  to the tooltip list and returns false. Otherwise returns true, adding nothing. */
	@SideOnly(Side.CLIENT)
	public static boolean tooltipIsShiftKeyDown(List<String> tooltip) {
		boolean shift = GuiScreen.isShiftKeyDown();
		if (!shift) formatTooltip(tooltip, "moreInfo", "\u00A76SHIFT\u00A77");
		return shift;
	}
	
	
	/** Formats a chat translation key (<code> "chat.modid.key" </code>) and sends
	 *  it to the player. Can also be used client-side to display a chat message. */
	public static void chatMessage(EntityPlayer player, String langKey, Object... args) {
		player.sendMessage(new TextComponentTranslation(
			"chat." + WearableBackpacks.MOD_ID + "." + langKey, args));
	}
	/** Formats a chat translation key (<code> "chat.modid.key" </code>) and displays it. */
	@SideOnly(Side.CLIENT)
	public static void displayChatMessage(String langKey, Object... args) {
		chatMessage(ClientUtils.getPlayer(), langKey, args);
	}
	
}
