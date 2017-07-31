package net.mcft.copy.backpacks.client.gui.config;

import java.util.Objects;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;
import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;

public abstract class BaseEntrySetting<T> extends BaseEntry {
	
	public final Setting<T> setting;
	
	private final T _previousValue;
	private final T _defaultValue;
	private T value;
	
	public BaseEntrySetting(BackpacksConfigScreen owningScreen, Setting<T> setting, GuiElementBase control) {
		super(owningScreen, getLanguageKey(setting), control);
		this.setting   = setting;
		_previousValue = setting.get();
		_defaultValue  = setting.getDefault();
		setValue(_previousValue);
	}
	private static String getLanguageKey(Setting<?> setting)
		{ return "config." + WearableBackpacks.MOD_ID + "." + setting.getFullName(); }
	
	
	public T getValue() { return value; }
	
	public void setValue(T value) {
		if (Objects.equals(value, this.value)) return;
		this.value = value;
		onChanged();
	}
	
	
	@Override
	public boolean isChanged() { return !Objects.equals(value, _previousValue); }
	@Override
	public boolean isDefault() { return Objects.equals(value, _defaultValue); }
	
	@Override
	public void undoChanges() { setValue(_previousValue); }
	@Override
	public void setToDefault() { setValue(_defaultValue); }
	
	@Override
	public ChangeRequiredAction applyChanges() {
		if (!isChanged()) return ChangeRequiredAction.None;
		setting.set(getValue());
		if (setting.requiresMinecraftRestart()) setting.update();
		return setting.getChangeRequiredAction();
	}
	
}
