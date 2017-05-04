package net.mcft.copy.backpacks.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.config.EntrySetting;

/** Represents a single configuration setting. */
public abstract class Setting<T> {
	
	/** Controls whether get() returns getEntryValue() or the setting's value. */
	private static boolean _checkEntryValue = false;
	
	
	/** Default value, used when no config file is present. */
	private final T _defaultValue;
	/** Loaded "own" value, directly from the config file (or using the default). */
	private T _value;
	
	/** Holds the setting's Forge Configuration. */
	private Configuration _config;
	/** Holds the setting's Forge Configuration Property. */
	private Property _property = null;
	
	/** Holds the setting's current entry instance in the config GUI (if open). */
	@SideOnly(Side.CLIENT)
	private EntrySetting<T> _entry;
	
	/** The setting category, for example "general". */
	private String _category;
	/** The setting name, for example "equipAsChestArmor". */
	private String _name;
	
	/** Stores a function which is called to determine if requirements are met. */
	private BooleanSupplier _requireFunc = () -> true;
	/** Whether changing the setting requires rejoining the current world. */
	private boolean _requiresWorldRejoin = false;
	/** Whether changing the setting requires restarting the Minecraft instance. */
	private boolean _requiresMinecraftRestart = false;
	
	/** Stores a function which is called to determine if recommendations are met (returns hint). */
	private Supplier<List<String>> _recommendFunc = () -> null;
	
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
	
	/** Sets a function that determines if the setting's requirements are met. */
	public final Setting<T> setRequirement(BooleanSupplier requireFunc)
		{ _requireFunc = requireFunc; return this; }
	/** Sets the specified settings to be required for this setting. */
	@SafeVarargs
	public final Setting<T> setRequired(Setting<Boolean>... settings)
		{ return setRequirement(() -> Arrays.asList(settings).stream().allMatch(setting -> setting.get())); }
	/** Sets the setting to require rejoining the world after being changed. */
	public Setting<T> setRequiresWorldRejoin() { _requiresWorldRejoin = true; return this; }
	/** Sets the setting to require restarting the game after being changed. */
	public Setting<T> setRequiresMinecraftRestart() { _requiresMinecraftRestart = true; return this; }
	
	/** Sets a function that determines if the setting's recommendations are met. */
	public final Setting<T> setRecommendation(Supplier<List<String>> recommendFunc)
		{ _recommendFunc = recommendFunc; return this; }
	/** Sets the specified setting to be recommended for this setting. */
	@SafeVarargs
	public final Setting<T> setRecommended(Setting<Boolean>... settings) {
		// TODO: This should probably be moved somewhere else.
		return setRecommendation(() -> {
			if (Arrays.asList(settings).stream().allMatch(setting -> setting.get())) return null;
			String key = "config." + WearableBackpacks.MOD_ID + "." + getFullName() + ".hint";
			List<String> tooltip = new ArrayList<String>(Arrays.asList(
				(TextFormatting.YELLOW + I18n.format(key)).split("\\\\n")));
			for (Setting<Boolean> setting : settings) if (!setting.get())
				tooltip.add(TextFormatting.AQUA + "[" + setting.getFullName() + " = false]");
			return tooltip;
		});
	}
	
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
	public T get() { return (_checkEntryValue ? getEntryValue() : (_isSynced ? _syncedValue : _value)); }
	/** Sets the setting's current value. */
	public void set(T value) { _value = value; _property.set(Objects.toString(value)); }
	
	/** Returns if this setting is enabled based on its requirements. */
	public boolean isEnabled() { return _requireFunc.getAsBoolean(); }
	/** Returns if this setting is enabled based on its requirements (uses config entry values). */
	@SideOnly(Side.CLIENT)
	public boolean isEnabledConfig() {
		_checkEntryValue = true;
		boolean enabled = isEnabled();
		_checkEntryValue = false;
		return enabled;
	}
	
	/** Returns whether changing the setting requires a world rejoin. */
	public boolean requiresWorldRejoin() { return _requiresWorldRejoin; }
	/** Returns whether changing the setting requires Minecraft to be restarted. */
	public boolean requiresMinecraftRestart() { return _requiresMinecraftRestart; }
	
	/** Returns a recommendation hint for this setting if
	 *  not all recommendations are met, or null otherwise. */
	@SideOnly(Side.CLIENT)
	public List<String> getRecommendationHint() {
		_checkEntryValue = true;
		List<String> hintTooltip = _recommendFunc.get();
		_checkEntryValue = false;
		return hintTooltip;
	}
	
	/** Returns if the setting is synced to players when they join a (multiplayer/LAN) world. */
	public boolean doesSync() { return _doesSync; }
	/** Returns if the setting is currently synced and get()
	 *  should return the synced instead of the "own" value. */
	public boolean isSynced() { return _isSynced; }
	
	/** Sets the setting's config entry class, to be used in place of the default. */
	public String getConfigEntryClass() { return _entryClass; }
	/** Returns the setting's comment, as used in the config file. */
	public String getComment() { return _comment; }
	
	/** Returns the setting's current entry value in the config GUI. */
	private T getEntryValue() { return _entry.getValue(); }
	/** Sets the setting's current config entry in the config GUI to the specified entry.
	 *  Used for disabling config entries dynamically based on which settings they require. */
	@SideOnly(Side.CLIENT)
	public void setEntry(EntrySetting<T> entry) { _entry = entry; }
	/** Resets the setting's current config entry in the config GUI. */
	@SideOnly(Side.CLIENT)
	public void resetEntry() { _entry = null; }
	
	
	/** Calls the update action if present
	 *  and any required setting is enabled. */
	public void update() {
		if ((_updateAction != null) && isEnabled())
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
	
	
	/** Attempts to parse the specified string as a value of this setting.
	 *  Throws an exception if the specified string is not valid. */
	public abstract T parse(String str);
	
	/** Called when the Configuration is loaded. */
	protected void onPropertyLoaded() {
		_value = parse(getProperty().getString());
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
