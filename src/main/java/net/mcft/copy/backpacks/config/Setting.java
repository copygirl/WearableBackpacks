package net.mcft.copy.backpacks.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.function.Consumer;

import net.minecraft.nbt.NBTBase;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.config.BaseEntrySetting;
import net.mcft.copy.backpacks.config.Status.Severity;

/** Represents a single configuration setting. */
public abstract class Setting<T> {
	
	/** Controls whether get() returns getEntryValue() or the setting's value. */
	private static boolean _checkEntryValue = false;
	
	
	/** Default value, used when no config file is present. */
	private final T _defaultValue;
	/** Loaded "own" value, directly from the config file (or using the default). */
	private T _value;
	
	/** Holds the setting's current entry instance in the config GUI (if open). */
	@SideOnly(Side.CLIENT)
	private BaseEntrySetting<T> _entry;
	
	/** The setting category, for example "general". */
	private String _category;
	/** The setting name, for example "equipAsChestArmor". */
	private String _name;
	
	/** Stores the action required after changing this setting. */
	private ChangeRequiredAction _changeRequiredAction = ChangeRequiredAction.None;
	
	/** Stores a list of functions which get called to handle hints, warnings and errors. */
	private List<Supplier<Status>> _statusFuncs = new ArrayList<Supplier<Status>>();
	
	/** Stores whether the setting will be synced to players joining a world. */
	private boolean _doesSync = false;
	/** Stores whether the setting is currently synced (_syncedValue stores current value). */
	private boolean _isSynced = false;
	/** Current synced value, returned from get() if _isSynced is true. */
	private T _syncedValue = null;
	
	/** Action fired when setting is updated (synced or changed ingame), null if none. */
	private Consumer<T> _updateAction = null;
	
	/** Holds the setting's custom config entry class to use in place of the default, if any. */
	private String _configEntryClass = null;
	/** The setting's comment used in the config file, if any. */
	private String _comment = null;
	
	
	public Setting(T defaultValue) {
		_defaultValue = defaultValue;
	}
	
	/** Set the setting's category and name.
	 *  This is called automatically. Category and name are taken from
	 *  reflected field names. Keeps the constructor short and simple. */
	protected void init(String category, String name) {
		_category = category;
		_name = name;
	}
	
	/** Adds a function to this setting that may return a status to hint,
	 *  warn or error about the state of the setting or other factors. */
	public final Setting<T> addStatusFunc(Supplier<Status> func)
		{ _statusFuncs.add(func); return this; }
		/** Sets the specified setting to be required for this setting to be valid. */
	public final Setting<T> setRequired(Setting<Boolean> setting)
		{ return addStatusFunc(() -> !setting.get() ? Status.REQUIRED(setting) : Status.NONE); }
	/** Sets the specified setting to be recommended for this setting. */
	public final Setting<T> setRecommended(Setting<Boolean> setting, String key)
		{ return addStatusFunc(() -> !setting.get() ? Status.RECOMMENDED(setting, key) : Status.NONE); }
	
	/** Sets the setting to require rejoining the world after being changed. */
	public Setting<T> setRequiresWorldRejoin()
		{ _changeRequiredAction = ChangeRequiredAction.RejoinWorld; return this; }
	/** Sets the setting to require restarting the game after being changed. */
	public Setting<T> setRequiresMinecraftRestart()
		{ _changeRequiredAction = ChangeRequiredAction.RestartMinecraft; return this; }
	
	/** Sets the setting to be synchronized to players joining a world. */
	public Setting<T> setSynced() { _doesSync = true; return this; }
	/** Sets the setting to be synchronized to players joining a world.
	 *  The specified action is fired when the setting is synced on the receiving player's side. */
	public Setting<T> setSynced(Consumer<T> action) { setSynced(); return setUpdate(action); }
	
	/** Sets the update function to be fired when the setting is updated (config changed or syncronized). */
	public Setting<T> setUpdate(Consumer<T> action) { _updateAction = action; return this; }
	
	/** Sets the setting's config entry class, to be used in place of the default. */
	public Setting<T> setConfigEntryClass(String entryClass) { _configEntryClass = entryClass; return this; }
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
	public void set(T value) { _value = value; }
	
	/** Returns this setting's current status regarding its value / requirements. */
	public List<Status> getStatus()
		{ return _statusFuncs.stream().map(Supplier::get).collect(Collectors.toList()); }
	
	/** Returns if this setting is enabled based on its requirements / status functions. */
	public boolean isEnabled()
		{ return Severity.ERROR != Status.getSeverity(getStatus()); }
	/** Returns if this setting is enabled based on its requirements (uses config entry values). */
	@SideOnly(Side.CLIENT)
	public boolean isEnabledConfig() {
		_checkEntryValue = true;
		boolean enabled = isEnabled();
		_checkEntryValue = false;
		return enabled;
	}
	
	/** Returns the required action after changing this
	 *  setting (such as world rejoin or Minecraft restart). */
	public ChangeRequiredAction getChangeRequiredAction() { return _changeRequiredAction; }
	/** Returns whether changing the setting requires a world rejoin. */
	public boolean requiresWorldRejoin()
		{ return (getChangeRequiredAction() != ChangeRequiredAction.None); }
	/** Returns whether changing the setting requires Minecraft to be restarted. */
	public boolean requiresMinecraftRestart()
		{ return (getChangeRequiredAction() == ChangeRequiredAction.RestartMinecraft); }
	
	/** Returns if the setting is synced to players when they join a (multiplayer/LAN) world. */
	public boolean doesSync() { return _doesSync; }
	/** Returns if the setting is currently synced and get()
	 *  should return the synced instead of the "own" value. */
	public boolean isSynced() { return _isSynced; }
	
	/** Sets the setting's config entry class, to be used in place of the default. */
	public String getConfigEntryClass() { return _configEntryClass; }
	/** Returns the setting's comment, as used in the config file. */
	public String getComment() { return _comment; }
	
	/** Returns the setting's current entry value in the config GUI. */
	private T getEntryValue() { return _entry.getValue().get(); }
	/** Sets the setting's current config entry in the config GUI to the specified entry.
	 *  Used for disabling config entries dynamically based on which settings they require. */
	@SideOnly(Side.CLIENT)
	public void setEntry(BaseEntrySetting<T> entry) { _entry = entry; }
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
	
	/** Loads the setting's value from the specified Configuration. */
	protected abstract void loadFromConfiguration(Configuration config);
	/** Saves the setting's value to the specified Configuration. */
	protected abstract void saveToConfiguration(Configuration config);
	
	
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
	
	
	public enum ChangeRequiredAction {
		None,
		RejoinWorld,
		RestartMinecraft
	}
	
}
