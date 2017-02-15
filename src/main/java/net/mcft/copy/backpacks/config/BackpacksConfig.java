package net.mcft.copy.backpacks.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.File;
import java.lang.reflect.Field;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

import net.mcft.copy.backpacks.BackpacksContent;
import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.misc.BackpackSize;
import net.mcft.copy.backpacks.network.MessageSyncSettings;

public class BackpacksConfig extends Configuration {
	
	// ==[ GENERAL ]==
	
	public final Setting<Boolean> equipAsChestArmor = new SettingBoolean(true)
		.setSynced((value) -> BackpackHelper.equipAsChestArmor = value)
		.setComment("If disabled, backpacks do not take up the player's chest armor equipment slot. Default: true.");
	
	public final Setting<Boolean> enableEquippedInteraction = new SettingBoolean(true)
		.setComment("If enabled, allows equipped backpacks to be opened by other players by right clicking the target's back. Default: true.");
	
	public final Setting<Boolean> enableSelfInteraction = new SettingBoolean(false).setSynced()
		.setComment("If enabled, allows players to open their own equipped backpack without requiring it to be placed first using a keybind. Default: false.");
	
	public final Setting<Boolean> dropAsBlockOnDeath = new SettingBoolean(true)
		.setComment("If enabled, places equipped backpacks as a block on death, instead of scattering the items all around. Default: true.");
	
	// ==[ BACKPACK ]==
	
	public final BackpackCategory backpack = new BackpackCategory();
	public static class BackpackCategory {
		
		public final Setting<Boolean> enabled = new SettingBoolean(true)
			.setRequiresMinecraftRestart();
		
		public final Setting<Integer> durability = new SettingInteger(214)
			.setValidRange(0, Integer.MAX_VALUE).setRequired(enabled)
			.setSynced((value) -> BackpacksContent.BACKPACK.setMaxDamage(value))
			.setComment("Durability of a normal backpack. Set to 0 for unbreakable. Default: 214.\n" +
			            "Lowering this (including setting to 0) can make damaged backpacks break.");
		
		public final Setting<BackpackSize> size = new SettingBackpackSize(9, 4).setRequired(enabled)
			.setComment("Storage size of a normal backpack. Valid values are [1x1] to [17x6]. Default: [9x4].\n" +
			            "Changing this doesn't affect placed or equipped backpacks until turned back into an item.");
		
	}
	
	
	private Map<String, Setting<?>> _settings = new LinkedHashMap<String, Setting<?>>();
	
	public BackpacksConfig(File file) {
		super(file);
		
		// Add settings from this class as general category.
		addSettingsFromClass(this, Configuration.CATEGORY_GENERAL);
		
		// Iterate over category fields and add their settings.
		for (Field field : getClass().getFields()) {
			if ((field.getDeclaringClass() != getClass()) ||
			    !field.getType().getName().endsWith("Category")) continue;
			try { addSettingsFromClass(field.get(this), field.getName()); }
			catch (IllegalAccessException ex) { throw new RuntimeException(ex); }
		}
	}
	
	/** Iterates over the Setting fields on the specified
	 *  instance, initializing and adding them to _settings. */
	private void addSettingsFromClass(Object instance, String category) {
		for (Field field : instance.getClass().getFields()) {
			if (!Setting.class.isAssignableFrom(field.getType())) continue;
			
			Setting<?> setting;
			try { setting = (Setting<?>)field.get(instance); }
			catch (IllegalAccessException ex) { throw new RuntimeException(ex); }
			
			setting.init(this, category, field.getName());
			_settings.put(setting.getFullName(), setting);
		}
	}
	
	
	/** Returns a setting from this configuration with the specified full name. */
	public Setting<?> getSetting(String fullName) { return _settings.get(fullName); }
	
	/** Returns a collection containing all settings from this configuration. */
	public Collection<Setting<?>> getSettings() { return _settings.values(); }
	
	/** Returns a collection containing all settings from the specified category. */
	public Collection<Setting<?>> getSettings(String category) {
		return getSettings().stream()
			.filter(setting -> setting.getCategory().equals(category))
			.collect(Collectors.toList());
	}
	
	
	// Loading / saving / initializing
	
	@Override
	public void load() {
		// By default, Configuration calls load when constructed.
		// At that time, members of this class are not yet initialized, so
		// instead we call load manually afterwards from the main mod class.
		if (_settings == null) return;
		super.load();
		
		// Update config settings from old versions.
		if (getCategory("backpack").containsKey("rows")) {
			int rows = get("backpack", "rows", 4).getInt();
			get("backpack", "size", "").set("[9," + rows +"]");
			getCategory("backpack").remove("rows");
		}
		
		getSettings().forEach(Setting::onPropertyLoaded);
	}
	
	@Override
	public void save() {
		// Our settings are very deliberately sorted, so group our settings by
		// category and set the property order to how we have ordered the fields.
		// This is also why we use LinkedHashMap: Keeps elements in insertion order.
		Map<String, List<Setting<?>>> byCategory = getSettings().stream().collect(
			Collectors.groupingBy(setting -> setting.getCategory(), LinkedHashMap::new, Collectors.toList()));
		for (Map.Entry<String, List<Setting<?>>> entry : byCategory.entrySet()) {
			String category = entry.getKey();
			if (category.equals(Configuration.CATEGORY_GENERAL)) continue;
			List<String> order = entry.getValue().stream().map(Setting::getName).collect(Collectors.toList());
			setCategoryPropertyOrder(category, order);
		}
		// Unfortunately ordering is not possible for categories in the config file itself.
		
		// Remove old config properties.
		getCategory(Configuration.CATEGORY_GENERAL).remove("enableHelpTooltips");
		
		super.save();
	}
	
	/** Called once after content has been initialized to call setting update methods. */
	public void init() {
		getSettings().forEach(Setting::update);
	}
	
	
	// Event handling
	
	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event) {
		if (!event.getModID().equals(WearableBackpacks.MOD_ID)) return;
		getSettings().forEach(Setting::onPropertyChanged);
		// Resyncronize the settings to all players.
		if (event.isWorldRunning())
			WearableBackpacks.CHANNEL.sendToAll(MessageSyncSettings.create());
		save();
	}
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event) {
		// Synchronize settings with players when they join the world / server.
		WearableBackpacks.CHANNEL.sendTo(MessageSyncSettings.create(), event.player);
	}
	
	@SubscribeEvent
	public void onDisconnectedFromServer(ClientDisconnectionFromServerEvent event) {
		// Reset all synced values of the settings.
		getSettings().forEach(Setting::resetSynced);
		// Note that this will cause get() to return null. This is
		// to make sure that only a properly synced value is used.
	}
	
}
