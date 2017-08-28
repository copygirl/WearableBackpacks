package net.mcft.copy.backpacks.client.gui.config;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;
import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;

@SideOnly(Side.CLIENT)
public abstract class BaseEntrySetting<T> extends BaseEntry.Labelled {
	
	public final Setting<T> setting;
	protected final GuiElementBase control;
	
	private final T _previousValue;
	private final T _defaultValue;
	private Optional<T> _value;
	
	public BaseEntrySetting(Setting<T> setting, GuiElementBase control) {
		this.setting = setting;
		this.setting.setEntry(this);
		this.control = control;
		
		_previousValue = setting.get();
		_defaultValue  = setting.getDefault();
		_value = Optional.of(_previousValue);
		
		label.setText(I18n.format(getLanguageKey()));
		label.setTooltip(getSettingTooltip());
		
		control.setHeight(ENTRY_HEIGHT);
		
		setSpacing(8, 6, 4);
		addFixed(label);
		addWeighted(control);
		addFixed(buttonUndo);
		addFixed(buttonReset);
		
		onChanged();
	}
	
	public String getLanguageKey()
		{ return "config." + WearableBackpacks.MOD_ID + "." + setting.getFullName(); }
	private List<String> getSettingTooltip() {
		String langKey = getLanguageKey();
		String def = I18n.format("fml.configgui.tooltip.default", setting.getDefault());
		String warn = setting.requiresMinecraftRestart() ? "fml.configgui.gameRestartTitle" : null;
		return formatTooltip(langKey, langKey + ".tooltip", def, warn);
	}
	
	
	public Optional<T> getValue() { return _value; }
	
	public void setValue(T value) { setValue(Optional.of(value)); }
	public void setValue(Optional<T> value) {
		if (Objects.equals(value, _value)) return;
		_value = value;
		onChanged();
	}
	
	/** Called when this entry's value changes, updating its control's state. */
	protected void onChanged() {  }
	
	@Override
	public boolean isEnabled() { return (super.isEnabled() && setting.isEnabledConfig()); }
	
	// IConfigEntry implementation
	
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
