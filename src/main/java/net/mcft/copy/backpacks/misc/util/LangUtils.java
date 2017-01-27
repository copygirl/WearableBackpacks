package net.mcft.copy.backpacks.misc.util;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;

/** Contains utility methods related to language files / localization. */
public final class LangUtils {
	
	private LangUtils() {  }
	
	/** Formats a tooltip translation key (<code> "tooltip.modid.key" </code>)
	 *  and adds it to the tooltip list. Used in {@link Item#addInformation}. */
	@SideOnly(Side.CLIENT)
	public static void formatTooltip(List<String> tooltip, String key, Object... args) {
		String translated = I18n.format(
			"tooltip." + WearableBackpacks.MOD_ID + "." + key, args);
		tooltip.addAll(Arrays.asList(translated.split("\\\\n")));
	}
	
	/** Formats a chat translation key (<code> "chat.modid.key" </code>) and sends
	 *  it to the player. Can also be used client-side to display a chat message. */
	public static void chatMessage(EntityPlayer player, String key, Object... args) {
		player.sendMessage(new TextComponentTranslation(
			"chat." + WearableBackpacks.MOD_ID + "." + key, args));
	}
	/** Formats a chat translation key (<code> "chat.modid.key" </code>) and displays it. */
	@SideOnly(Side.CLIENT)
	public static void displayChatMessage(String key, Object... args) {
		chatMessage(ClientUtils.getPlayer(), key, args);
	}
	
}
