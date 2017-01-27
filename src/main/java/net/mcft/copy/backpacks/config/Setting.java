package net.mcft.copy.backpacks.config;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.nbt.NBTBase;

import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Configuration;

import net.mcft.copy.backpacks.WearableBackpacks;

/** Represents a single configuration setting. */
public abstract class Setting<T> {
	
	/** The setting category, for example "general". */
	public final String category;
	/** The setting name, for example "equipAsChestArmor". */
	public final String name;
	/** The setting's full name, for example "general.equipAsChestArmor".
	 *  Used as the key in BackpacksConfig._settings map. */
	public final String fullName;
	
	/** Default value, used when no config file is present. */
	private final T _defaultValue;
	/** Loaded "own" value, directly from the config file (or using the default). */
	private T _value;
	
	/** Function which is used to validate the setting's value, null if none. */
	private Function<T, String> _validationFunc = null;
	
	/** Stores whether the setting will be synced to players joining a world. */
	private boolean _isSynced = false;
	/** Current synced value, returned from get() if setting does sync. */
	private T _syncedValue = null;
	/** Action fired when setting is synced on the receiving player's side, null if none. */
	private Consumer<T> _syncAction = null;
	
	/** The setting's comment used in the config file, if any. */
	private String _comment = null;
	
	
	public Setting(String category, String name, T defaultValue) {
		this.category = category;
		this.name = name;
		this.fullName = (category + "." + name);
		_defaultValue = _value = defaultValue;
	}
	
	/** Sets the setting's validation function, which returns null
	 *  if the passed-in value is valid, or an error string if not. */
	public Setting<T> setValidationFunc(Function<T, String> validationFunc) {
		_validationFunc = validationFunc;
		return this;
	}
	/** Sets the valid values for this setting, causing a validation
	 *  error if any value besides the specified ones are being used. */
	@SafeVarargs
	public final Setting<T> setValidValues(T... values) {
		return setValidationFunc((value) -> Arrays.asList(values).contains(value) ? null
			: String.format("Value %s is not one of the valid values (%s)",
			                value, Arrays.toString(values).replaceAll("^.|.$", ""))); // Removes first and last char.
	}
	
	/** Sets the setting to be synchronized to players joining a world. */
	public Setting<T> setSynced() { return setSynced(null); }
	/** Sets the setting to be synchronized to players joining a world.
	 *  The specified action is fired when the setting is synced on the receiving player's side. */
	public Setting<T> setSynced(Consumer<T> syncAction) {
		_isSynced = true;
		_syncedValue = _defaultValue;
		_syncAction = syncAction;
		return this;
	}
	
	/** Sets the setting's comment, to be used in the config file. */
	public Setting<T> setComment(String comment) {
		_comment = comment;
		return this;
	}
	
	
	/** Returns the setting's default value. */
	public T getDefault() { return _defaultValue; }
	/** Returns the setting's "own" value, directly from the config file. */
	public T getOwn() { return _value; }
	/** Returns the setting's current value. */
	public T get() { return (_isSynced ? _syncedValue : _value); }
	
	/** Returns if the setting is synced to players when they join a (multiplayer/LAN) world. */
	public boolean isSynced() { return _isSynced; }
	
	/** Returns the setting's comment, as used in the config file. */
	public String getComment() { return _comment; }
	
	
	// Loading / saving
	
	/** Returns the Forge config Property for this setting. */
	protected Property getProperty(Configuration config) {
		return config.get(category, name, String.valueOf(_defaultValue), _comment, getType());
	}
	
	/** Loads the setting from the Forge Configuration object
	 *  (after it has been loaded from file), validating it. */
	protected final void load(Configuration config) {
		_value = load(getProperty(config));
		// Validate the value.
		if (_validationFunc != null) {
			String validationError = _validationFunc.apply(_value);
			if (validationError != null) {
				WearableBackpacks.LOG.error("Error validating config option '{}': {}",
				                            fullName, validationError);
				_value = _defaultValue;
			}
		}
		// Update synced value.
		if (_isSynced) _syncedValue = _value;
	}
	/** Saves the setting to the Forge Configuration object. */
	protected final void save(Configuration config) {
		save(getProperty(config), _value);
	}
	
	/** Returns the Forge Property type used for this setting. */
	public abstract Property.Type getType();
	
	/** Loads the raw value from the specified config Property, returning it. */
	protected abstract T load(Property property);
	/** Saves the raw value to the specified config Property. */
	protected abstract void save(Property property, T value);
	
	
	// Synchronization
	
	/** Reads the synced value from the specified NBT tag. */
	public void readSynced(NBTBase tag) {
		_syncedValue = read(tag);
		if (_syncAction != null)
			_syncAction.accept(_syncedValue);
	}
	/** Writes the own value to an NBT tag, returning it. */
	public NBTBase writeSynced() { return write(_value); }
	
	public abstract T read(NBTBase tag);
	public abstract NBTBase write(T value);
	
}
