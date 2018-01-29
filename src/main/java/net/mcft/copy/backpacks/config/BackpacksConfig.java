package net.mcft.copy.backpacks.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.BackpacksContent;
import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.config.custom.SettingBackpackSize;
import net.mcft.copy.backpacks.config.custom.SettingListEntities;
import net.mcft.copy.backpacks.config.custom.SettingPercent;
import net.mcft.copy.backpacks.misc.BackpackSize;
import net.mcft.copy.backpacks.network.MessageSyncSettings;

public class BackpacksConfig {
	
	// ==[ GENERAL ]==
	
	public final Setting<Boolean> equipAsChestArmor = new SettingBoolean(true)
		.setSynced(setting -> BackpackHelper.equipAsChestArmor = setting.get())
		.setComment("If disabled, backpacks do not take up the player's chest armor equipment slot. Default: true.");
	
	public final Setting<Boolean> enableEquippedInteraction = new SettingBoolean(true)
		.setComment("If enabled, allows equipped backpacks to be opened by other players by right clicking the target's back. Default: true.");
	
	public final Setting<Boolean> enableSelfInteraction = new SettingBoolean(false).setSynced()
		.setComment("If enabled, allows players to open their own equipped backpack without requiring it to be placed first using a keybind. Default: false.");
	
		public final Setting<Boolean> enableMachineInteraction = new SettingBoolean(false)
		.setComment("If enabled, allows machines to interact with placed backpacks. Default: false.");

	public final Setting<Boolean> dropAsBlockOnDeath = new SettingBoolean(true)
		.setComment("If enabled, places equipped backpacks as a block on death, instead of scattering the items all around. Default: true.");
	
	// ==[ BACKPACK ]==
	
	public BackpackCategory backpack;
	public class BackpackCategory {
		
		public final Setting<Boolean> enabled = new SettingBoolean(true)
			.setRequiresMinecraftRestart();
		
		public final Setting<Integer> durability = new SettingInteger(214).setValidRange(0, Integer.MAX_VALUE)
			.setRequired(enabled).setRecommended(equipAsChestArmor, "chestplate")
			.setSynced(setting -> BackpacksContent.BACKPACK.setMaxDamage(setting.get()))
			.setComment("Durability of a normal backpack. Set to 0 for unbreakable. Default: 214.");
		
		public final Setting<Integer> armor = new SettingInteger(2).setValidRange(0, 20)
			.setRequired(enabled).setRecommended(equipAsChestArmor, "chestplate").setSynced()
			.setConfigEntryClass("net.mcft.copy.backpacks.client.gui.config.custom.EntryValueArmor")
			.setComment("Armor points of a normal backpack. Valid values are 0 to 20. Default: 2.\n" +
			            "Has no effect if equipAsChestArmor is disabled.");
		
		public final Setting<BackpackSize> size = new SettingBackpackSize(9, 4).setRequired(enabled)
			.setComment("Storage size of a normal backpack. Valid values are [1x1] to [17x6]. Default: [9x4].\n" +
			            "Changing this doesn't affect placed or equipped backpacks until turned back into an item.");
		
	}
	
	// ==[ ENTITY ]==
	
	public EntityCategory entity;
	public class EntityCategory {
		
		public final Setting<Boolean> spawnNaturally = new SettingBoolean(true)
			.setComment("Controls whether mobs spawned naturally can randomly be wearing backpacks.");
		
		public final Setting<Boolean> spawnFromSpawners = new SettingBoolean(false)
			.setComment("Controls whether mobs spawned from spawners can randomly be wearing backpacks.");
		
		public final SettingListEntities list = new SettingListEntities();
		
	}
	
	// ==[ COSMETIC ]==
	
	@SideOnly(Side.CLIENT)
	public CosmeticCategory cosmetic;
	@SideOnly(Side.CLIENT)
	public class CosmeticCategory {
		
		public final Setting<Double> enchantEffectOpacity = new SettingPercent(0.80)
			.setConfigEntryClass("net.mcft.copy.backpacks.client.gui.config.custom.EntryValueEffectOpacity")
			.setComment("Controls the opacity / visibility of the enchantment effect on equipped and placed backpacks, if present. Default: 80%.");
		
	}
	
	
	private final Configuration _config;
	private List<String> _categories = new ArrayList<String>();
	private Map<String, Setting<?>> _settings = new LinkedHashMap<String, Setting<?>>();
	
	public BackpacksConfig(File file) {
		_config = new Configuration(file);
		
		// Add settings from this class as general category.
		addSettingsFromClass(this, Configuration.CATEGORY_GENERAL);
		
		// Iterate over category fields and add their settings.
		for (Field field : getClass().getFields()) {
			if ((field.getDeclaringClass() != getClass()) ||
			    !field.getType().getName().endsWith("Category")) continue;
			try {
				String category = field.getName();
				field.set(this, field.getType().getConstructors()[0].newInstance(this));
				addSettingsFromClass(field.get(this), category);
				if (!_categories.contains(category)) _categories.add(category);
			} catch (InstantiationException |
			         InvocationTargetException |
			         IllegalAccessException ex) { throw new RuntimeException(ex); }
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
			
			setting.init(category, field.getName());
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
	/** Returns a collection containing all settings from the GENERAL category. */
	public Collection<Setting<?>> getSettingsGeneral()
		{ return getSettings(Configuration.CATEGORY_GENERAL); }
	
	/** Returns a list of all root categories (not including GENERAL). */
	public List<String> getCategories()
		{ return Collections.unmodifiableList(_categories); }
	
	
	// Loading / saving / initializing
	
	public void load() {
		// By default, Configuration calls load when constructed.
		// At that time, members of this class are not yet initialized, so
		// instead we call load manually afterwards from the main mod class.
		if (_settings == null) return;
		_config.load();
		
		// Update config settings from old versions.
		if (_config.getCategory("backpack").containsKey("rows")) {
			int rows = _config.get("backpack", "rows", 4).getInt();
			_config.get("backpack", "size", "").set(new BackpackSize(9, rows).toString());
			_config.getCategory("backpack").remove("rows");
		}
		
		getSettings().forEach(setting -> setting.loadFromConfiguration(_config));
	}
	
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
			_config.setCategoryPropertyOrder(category, order);
		}
		// Unfortunately ordering is not possible for categories in the config file itself.
		
		// Remove old config properties.
		_config.getCategory(Configuration.CATEGORY_GENERAL).remove("enableHelpTooltips");
		
		getSettings().forEach(setting -> setting.saveToConfiguration(_config));
		
		_config.save();
	}
	
	/** Called once after content has been initialized to call setting update methods. */
	public void init() { getSettings().forEach(Setting::update); }
	
	
	// Event handling
	
	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event) {
		if (!event.getModID().equals(WearableBackpacks.MOD_ID)) return;
		// Resyncronize the settings to all players.
		if (event.isWorldRunning()) WearableBackpacks.CHANNEL.sendToAll(
			MessageSyncSettings.create(), BackpacksConfig::isNotServerOwner);
		// TODO: Show chat message when settings are updated?
		save();
	}
	
	/** Synchronizes settings with players when they join the world / server. */
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event) {
		if (isNotServerOwner(event.player)) return;
		WearableBackpacks.CHANNEL.sendTo(MessageSyncSettings.create(), event.player);
	}
	
	
	@SideOnly(Side.CLIENT)
	private boolean _connected;
	
	/** Resets all synced values of the settings after disconnecting. */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onDisconnectedFromServer(ClientConnectedToServerEvent event)
		{ _connected = true; }
	
	/** Syncronizes the client settings to the ones specified
	 *  in the compound tag. (Called by MessageSyncSettings.) */
	@SideOnly(Side.CLIENT)
	public void syncSettings(NBTTagCompound data) {
		if (!_connected) return;
		for (String key : data.getKeySet()) {
			NBTBase tag = data.getTag(key);
			Setting<?> setting = getSetting(key);
			if ((setting != null) && setting.doesSync())
				setting.readSynced(tag);
		}
	}
	
	/** Resets all synced values of the settings after disconnecting. */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onDisconnectedFromServer(ClientDisconnectionFromServerEvent event) {
		if (!_connected) return;
		getSettings().forEach(Setting::resetSynced);
		_connected = false;
	}
	
	
	private static MinecraftServer getServerInstance()
		{ return FMLCommonHandler.instance().getMinecraftServerInstance(); }
	private static boolean isNotServerOwner(EntityPlayer player)
		{ return player.getName().equalsIgnoreCase(getServerInstance().getServerOwner()); }
	
}
