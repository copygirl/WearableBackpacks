package net.mcft.copy.backpacks.client.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.GuiConfigEntries.IConfigEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.config.Setting;

@SideOnly(Side.CLIENT)
public class BackpacksGuiConfig extends GuiConfig {
	
	public BackpacksGuiConfig(GuiScreen parent) {
		super(parent, getConfigElements(), WearableBackpacks.MOD_ID,
		      false, false, WearableBackpacks.MOD_NAME);
	}
	
	/** Gets the root config elements for this config GUI. */
	private static List<IConfigElement> getConfigElements() {
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		// Add all config elements from the general category into the main config screen.
		list.addAll(getElements(Configuration.CATEGORY_GENERAL));
		// Add category elements leading to config sub-screens
		// for all other categories (except the general category).
		for (String category : WearableBackpacks.CONFIG.getCategoryNames()) {
			if (category.equals(Configuration.CATEGORY_GENERAL)) continue;
			String tooltipKey = "config." + WearableBackpacks.MOD_ID + ".category." + category;
			list.add(new DummyCategoryElement(category, tooltipKey, getElements(category), BackpacksCategoryEntry.class));
		}
		return list;
	}
	
	/** Creates and returns config elements for the specified category. */
	private static List<IConfigElement> getElements(String category) {
		return WearableBackpacks.CONFIG.getSettings(category).stream()
			.map((setting) -> new BackpacksConfigElement(setting))
			.collect(Collectors.toList());
	}
	
	
	public static class BackpacksConfigElement extends ConfigElement {
		private final Setting<?> _setting;
		
		@SuppressWarnings("unchecked")
		public BackpacksConfigElement(Setting<?> setting) {
			super(setting.getProperty());
			_setting = setting;
			// Set the property's entry class. We do this here because IConfigEntry is
			// client-side only and so is this GUI but Setting also exists on the server.
			String entryClass = setting.getConfigEntryClass();
			if (entryClass != null)
				try { setting.getProperty().setConfigEntryClass((Class<? extends IConfigEntry>)Class.forName(entryClass)); }
				catch (ClassNotFoundException ex) { throw new RuntimeException(ex); }
		}
		
		@Override public String getName() { return I18n.format(getLanguageKey()); }
		@Override public String getComment() { return I18n.format(getLanguageKey() + ".tooltip"); }
		@Override public String getLanguageKey() {
			return "config." + WearableBackpacks.MOD_ID + "." +
			       _setting.getCategory() + "." + _setting.getName();
		}
	}
	
	public static class BackpacksCategoryEntry extends CategoryEntry {
		public BackpacksCategoryEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
			super(owningScreen, owningEntryList, configElement); }
		@Override
		protected GuiScreen buildChildScreen() {
			return new GuiConfigExt(owningScreen, configElement.getChildElements(), owningScreen.modID,
			                        owningScreen.allRequireWorldRestart || configElement.requiresWorldRestart(),
			                        owningScreen.allRequireMcRestart || configElement.requiresMcRestart(), owningScreen.title,
			                        ((owningScreen.titleLine2 != null) ? owningScreen.titleLine2 : "") + " > " + this.name);
		}
	}
	
}
