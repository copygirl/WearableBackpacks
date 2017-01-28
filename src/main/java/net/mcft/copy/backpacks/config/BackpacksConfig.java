package net.mcft.copy.backpacks.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.File;
import java.lang.reflect.Field;

import net.minecraftforge.common.config.Configuration;

import net.mcft.copy.backpacks.BackpacksContent;
import net.mcft.copy.backpacks.api.BackpackHelper;

// TODO: Implement a config screen thing.
public class BackpacksConfig implements Iterable<Setting<?>> {
	
	// == GENERAL ==
	
	public final Setting<Boolean> equipAsChestArmor = new SettingBoolean("general", "equipAsChestArmor", true)
		.setSynced((value) -> BackpackHelper.equipAsChestArmor = value)
		.setComment("If disabled, backpacks do not take up the player's chest armor equipment slot. Default: true.");
	
	public final Setting<Boolean> enableEquippedInteraction = new SettingBoolean("general", "enableEquippedInteraction", true)
		.setComment("If enabled, allows equipped backpacks to be opened by other players by right clicking the target's back. Default: true.");
	
	public final Setting<Boolean> enableSelfInteraction = new SettingBoolean("general", "enableSelfInteraction", false).setSynced()
		.setComment("If enabled, allows players to open their own equipped backpack without requiring it to be placed first using a keybind. Default: false.");
	
	public final Setting<Boolean> dropAsBlockOnDeath = new SettingBoolean("general", "dropAsBlockOnDeath", true)
		.setComment("If enabled, places equipped backpacks as a block on death, instead of scattering the items all around. Default: true.");
	
	// == BACKPACK ==
	
	public final Setting<Boolean> backpackEnabled = new SettingBoolean("backpack", "enabled", true);
	
	public final Setting<Integer> backpackDurability = new SettingInteger("backpack", "durability", 214)
		.setValidRange(0, Integer.MAX_VALUE)
		.setSynced((value) -> BackpacksContent.BACKPACK.setMaxDamage(value))
		.setComment("Durability of a normal backpack. Set to 0 for unbreakable. Default: 214.");
	
	// TODO: Allow specifying both colums and rows.
	public final Setting<Integer> backpackRows = new SettingInteger("backpack", "rows", 4).setValidRange(1, 6)
		.setComment("Number of rows of storage in a normal backpack. Valid values are 1 to 6. Default: 4.\n" +
		            "Changing this doesn't affect placed or equipped backpacks until turned back into an item.");
	
	
	private final Map<String, Setting<?>> _settings = new HashMap<String, Setting<?>>();
	private final File _file;
	private Configuration _config;
	
	public BackpacksConfig(File file) {
		_file = file;
		// Add settings to _settings list using reflection.
		try { for (Field field : getClass().getFields()) {
			if (!Setting.class.isAssignableFrom(field.getType())) continue;
			Setting<?> setting = (Setting<?>)field.get(this);
			_settings.put(setting.fullName, setting);
		} } catch (IllegalAccessException ex) { throw new RuntimeException(ex); }
	}
	
	
	/** Load all settings from configuration file. */
	public void load() {
		if (_config == null) _config = new Configuration(_file);
		else _config.load();
		for (Setting<?> setting : _settings.values())
			setting.load(_config);
	}
	
	/** Save all settings to configuration file. */
	public void save() {
		if (_config == null) _config = new Configuration(_file);
		for (Setting<?> setting : _settings.values())
			setting.save(_config);
		_config.save();
	}
	
	
	/** Returns a setting with the specified full name, or null if none could be found. */
	public Setting<?> find(String fullName) { return _settings.get(fullName); }
	
	@Override
	public Iterator<Setting<?>> iterator() { return _settings.values().iterator(); }
	
}
