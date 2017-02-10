package net.mcft.copy.backpacks.config;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import net.minecraft.nbt.NBTBase;

import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Configuration;

/** Represents a single configuration setting. */
public abstract class Setting<T> {
	
	/** Default value, used when no config file is present. */
	private final T _defaultValue;
	/** Loaded "own" value, directly from the config file (or using the default). */
	private T _value;
	
	/** Holds the setting's Forge Configuration. */
	private Configuration _config;
	/** Holds the setting's Forge Configuration Property. */
	private Property _property = null;
	
	/** The setting category, for example "general". */
	private String _category;
	/** The setting name, for example "equipAsChestArmor". */
	private String _name;
	
	/** Stores a different setting which is required for this one to function. */
	private Setting<Boolean> _requiredSetting = null;
	/** Whether changing the setting requires rejoining the current world. */
	private boolean _requiresWorldRejoin = false;
	/** Whether changing the setting requires restarting the Minecraft instance. */
	private boolean _requiresMinecraftRestart = false;
	
	/** The setting's valid values as a string array, or null if none. */
	private T[] _validValues = null;
	
	/** Stores whether the setting will be synced to players joining a world. */
	private boolean _doesSync = false;
	/** Stores whether the setting is currently synced (_syncedValue stores current value). */
	private boolean _isSynced = false;
	/** Current synced value, returned from get() if _isSynced is true. */
	private T _syncedValue = null;
	
	/** Action fired when setting is updated (synced or changed ingame), null if none. */
	private Consumer<T> _updateAction = null;
	
	/** Holds the setting's custom config entry class to use in place of the default, if any. */
	private String _entryClass = null;
	/** The setting's comment used in the config file, if any. */
	private String _comment = null;
	
	
	public Setting(T defaultValue) {
		_defaultValue = defaultValue;
	}
	
	/** Set the setting's configuration, category and name.
	 *  This is called automatically. Category and name are taken from
	 *  reflected field names. Keeps the constructor short and simple. */
	protected void init(Configuration config, String category, String name) {
		_config = config;
		_category = category;
		_name = name;
	}
	
	/** Sets the specified setting to be required for this setting. */
	public Setting<T> setRequired(Setting<Boolean> setting) { _requiredSetting = setting; return this; }
	/** Sets the setting to require rejoining the world after being changed. */
	public Setting<T> setRequiresWorldRejoin() { _requiresWorldRejoin = true; return this; }
	/** Sets the setting to require restarting the game after being changed. */
	public Setting<T> setRequiresMinecraftRestart() { _requiresMinecraftRestart = true; return this; }
	
	/** Sets the valid values for this setting. */
	@SafeVarargs
	public final Setting<T> setValidValues(T... values) { _validValues = values; return this; }
	
	/** Sets the setting to be synchronized to players joining a world. */
	public Setting<T> setSynced() { _doesSync = true; return this; }
	/** Sets the setting to be synchronized to players joining a world.
	 *  The specified action is fired when the setting is synced on the receiving player's side. */
	public Setting<T> setSynced(Consumer<T> action) { setSynced(); return setUpdate(action); }
	
	/** Sets the update function to be fired when the setting is updated (config changed or syncronized). */
	public Setting<T> setUpdate(Consumer<T> action) { _updateAction = action; return this; }
	
	/** Sets the setting's config entry class, to be used in place of the default. */
	public Setting<T> setConfigEntryClass(String entryClass) { _entryClass = entryClass; return this; }
	/** Sets the setting's comment, to be used in the config file. */
	public Setting<T> setComment(String comment) { _comment = comment; return this; }
	
	
	/** Returns the setting's category, for example "general". */
	public String getCategory() { return _category; }
	/** Returns the setting's name, for example "equipAsChestArmor". */
	public String getName() { return _name; }
	/** Returns the setting's full name, for example "general.equipAsChestArmor". */
	public String getFullName() { return _category + "." + _name; }
	
	/** Returns the setting's default value. */
	public T getDefault() { return _defaultValue; }
	/** Returns the setting's current value. */
	public T get() { return (_isSynced ? _syncedValue : _value); }
	
	/** Returns the setting required for this setting, or null if none. */
	public Setting<?> getRequired() { return _requiredSetting; }
	/** Returns if there's no required setting or it is enabled. */
	public boolean isRequiredEnabled() { return ((_requiredSetting == null) || _requiredSetting.get()); }
	/** Returns whether changing the setting requires a world rejoin. */
	public boolean requiresWorldRejoin() { return _requiresWorldRejoin; }
	/** Returns whether changing the setting requires Minecraft to be restarted. */
	public boolean requiresMinecraftRestart() { return _requiresMinecraftRestart; }
	
	/** Returns if the setting is synced to players when they join a (multiplayer/LAN) world. */
	public boolean doesSync() { return _doesSync; }
	/** Returns if the setting is currently synced and get()
	 *  should return the synced instead of the "own" value. */
	public boolean isSynced() { return _isSynced; }
	
	/** Sets the setting's config entry class, to be used in place of the default. */
	public String getConfigEntryClass() { return _entryClass; }
	/** Returns the setting's comment, as used in the config file. */
	public String getComment() { return _comment; }
	
	
	/** Calls the update action if present
	 *  and any required setting is enabled. */
	public void update() {
		if ((_updateAction != null) && isRequiredEnabled())
			_updateAction.accept(_value);
	}
	
	
	// Forge Configuration related
	
	/** Grabs the Property object from the Forge Configuration object. */
	protected abstract Property getPropertyFromConfig(Configuration config);
	
	/** Returns the Forge config Property associated with this setting. */
	public Property getProperty() {
		if (_property == null) {
			// Initialize the property if it hasn't been already.
			_property = getPropertyFromConfig(_config);
			_property.setRequiresWorldRestart(_requiresWorldRejoin);
			_property.setRequiresMcRestart(_requiresMinecraftRestart);
			if (_validValues != null)
				_property.setValidValues(
					(String[])Arrays.stream(_validValues)
						.map(Object::toString).toArray());
		}
		return _property;
	}
	
	
	/** Returns the value from the Forge Property. */
	protected abstract T getFromProperty();
	
	/** Called when the Configuration is loaded. */
	protected void onPropertyLoaded() {
		_value = getFromProperty();
	}
	
	/** Called when the Property is changed,
	 *  for example through the config GUI. */
	protected void onPropertyChanged() {
		if (_requiresMinecraftRestart) return;
		T previous = _value;
		_value = getFromProperty();
		if (!Objects.equals(_value, previous)) update();
	}
	
	
	// Synchronization
	
	/** Reads the synced value from the specified NBT tag. */
	public void readSynced(NBTBase tag) {
		_isSynced = true;
		_syncedValue = read(tag);
		if (_updateAction != null)
			_updateAction.accept(_syncedValue);
	}
	/** Writes the own value to an NBT tag, returning it. */
	public NBTBase writeSynced() { return write(_value); }
	
	/** Resets the synced value when players exit the world / disconnect from a server. */
	protected void resetSynced() { _isSynced = false; _syncedValue = null; }
	
	public abstract T read(NBTBase tag);
	public abstract NBTBase write(T value);
	
}
