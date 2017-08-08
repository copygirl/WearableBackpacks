package net.mcft.copy.backpacks.client.gui.config;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.client.resources.I18n;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;
import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;

public abstract class BaseEntrySetting<T> extends BaseEntry {
	
	public final Setting<T> setting;
	
	private final T _previousValue;
	private final T _defaultValue;
	private Optional<T> _value;
	
	public BaseEntrySetting(BackpacksConfigScreen owningScreen, Setting<T> setting, GuiElementBase control) {
		super(owningScreen, control);
		this.setting = setting;
		this.setting.setEntry(this);
		
		_previousValue = setting.get();
		_defaultValue  = setting.getDefault();
		_value = Optional.of(_previousValue);
		
		label.setText(I18n.format(getLanguageKey()));
		label.setTooltip(getSettingTooltip());
		
		onChanged();
	}
	
	@Override
	protected boolean hasLabel() { return true; }
	
	public String getLanguageKey()
		{ return "config." + WearableBackpacks.MOD_ID + "." + setting.getFullName(); }
	private List<String> getSettingTooltip() {
		String langKey = getLanguageKey();
		String def = I18n.format("fml.configgui.tooltip.default", setting.getDefault());
		String warn = setting.requiresMinecraftRestart() ? "fml.configgui.gameRestartTitle" : null;
		return formatTooltip(langKey, langKey + ".tooltip", def, warn);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> BaseEntrySetting<T> Create(BackpacksConfigScreen owningScreen, Setting<T> setting) {
		String entryClassName = setting.getConfigEntryClass();
		if (entryClassName == null) throw new RuntimeException(
			"Setting '" + setting.getFullName() + "' has no entry class defined");
		try {
			// Find a constructor with exactly two parameters:
			// - First one must be of type BackpacksConfigScreen.
			// - Second one must be of type Setting, compatible with the specified setting.
			Constructor<?> constructor = Arrays.stream(Class.forName(entryClassName).getConstructors())
				.filter(c -> (c.getParameterCount() == 2) &&
				             c.getParameterTypes()[0].equals(BackpacksConfigScreen.class) &&
				             c.getParameterTypes()[1].isAssignableFrom(setting.getClass()))
				.findFirst().orElseThrow(() -> new Exception("No compatible constructor found"));
			// Create and return a new instance of this entry class.
			return (BaseEntrySetting<T>)constructor.newInstance(owningScreen, setting);
		} catch (Exception ex) { throw new RuntimeException(
			"Exception while instanciating setting entry for '" +
				setting.getFullName() + "' (entry class '" + entryClassName + "')", ex); }
	}
	
	
	public Optional<T> getValue() { return _value; }
	
	public void setValue(T value) { setValue(Optional.of(value)); }
	public void setValue(Optional<T> value) {
		if (Objects.equals(value, _value)) return;
		_value = value;
		onChanged();
	}
	
	@Override
	public boolean isEnabled() { return (super.isEnabled() && setting.isEnabledConfig()); }
	
	@Override
	public boolean isChanged() { return !_value.equals(Optional.of(_previousValue)); }
	@Override
	public boolean isDefault() { return _value.equals(Optional.of(_defaultValue)); }
	@Override
	public boolean isValid() { return _value.isPresent(); }
	
	@Override
	public void undoChanges() { setValue(_previousValue); }
	@Override
	public void setToDefault() { setValue(_defaultValue); }
	
	@Override
	public ChangeRequiredAction applyChanges() {
		if (!isChanged()) return ChangeRequiredAction.None;
		setting.set(getValue().get());
		if (setting.requiresMinecraftRestart()) setting.update();
		return setting.getChangeRequiredAction();
	}
	
}
