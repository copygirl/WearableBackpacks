package net.mcft.copy.backpacks.client.gui.config;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.Status;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;

@SideOnly(Side.CLIENT)
public class EntrySetting<T> extends BaseEntry.Value<T> {
	
	public final Setting<T> setting;
	
	private final T _previousValue;
	private final T _defaultValue;
	
	@SuppressWarnings("unchecked") // Java is stupid.
	public EntrySetting(Setting<T> setting, IConfigValue<T> control) {
		super(control);
		this.setting = setting;
		this.setting.setEntry(this);
		
		_previousValue = setting.get();
		_defaultValue  = setting.getDefault();
		
		setLabelAndTooltip(setting.getFullName(),
			Objects.toString(setting.getDefault()),
			(setting.requiresMinecraftRestart() ? "fml.configgui.gameRestartTitle" : null));
		
		if (control instanceof IConfigValue.Setup)
			((IConfigValue.Setup<T>)control).setup(setting);
		setValue(setting.get());
	}
	
	@Override
	public List<Status> getStatus() {
		List<Status> status = super.getStatus();
		status.addAll(0, setting.getStatusConfig());
		return status;
	}
	
	
	@Override
	public boolean isEnabled() { return (super.isEnabled() && setting.isEnabledConfig()); }
	
	// IConfigEntry implementation
	
	@Override
	public boolean isChanged() { return !getValue().equals(Optional.of(_previousValue)); }
	@Override
	public boolean isDefault() { return getValue().equals(Optional.of(_defaultValue)); }
	
	@Override
	public void undoChanges() { setValue(_previousValue); }
	@Override
	public void setToDefault() { setValue(_defaultValue); }
	
	@Override
	public ChangeRequiredAction applyChanges() {
		if (!isChanged()) return ChangeRequiredAction.None;
		setting.set(getValue().get());
		if (!setting.requiresMinecraftRestart()) setting.update();
		return setting.getChangeRequiredAction();
	}
	
}
