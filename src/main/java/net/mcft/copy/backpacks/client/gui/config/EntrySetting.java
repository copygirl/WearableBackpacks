package net.mcft.copy.backpacks.client.gui.config;

import java.util.List;
import java.util.Objects;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.Status;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;

@SideOnly(Side.CLIENT)
public class EntrySetting<T> extends BaseEntry.Value<T> {
	
	public final Setting<T> setting;
	
	public EntrySetting(Setting<T> setting, IConfigValue<T> control) {
		super(setup(control, setting), setting.getDefault(), setting.getOwn());
		this.setting = setting;
		this.setting.setEntry(this);
		
		setLabelAndTooltip(setting.getFullName(),
			Objects.toString(setting.getDefault()),
			(setting.requiresMinecraftRestart() ? "fml.configgui.gameRestartTitle" : null));
	}
	@SuppressWarnings("unchecked") // Java is stupid.
	private static <T> IConfigValue<T> setup(IConfigValue<T> control, Setting<T> setting) {
		if (control instanceof IConfigValue.Setup)
			((IConfigValue.Setup<T>)control).setup(setting);
		return control;
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
	public ChangeRequiredAction applyChanges() {
		if (!isChanged()) return ChangeRequiredAction.None;
		setting.set(getValue().get());
		if (!setting.requiresMinecraftRestart()) setting.update();
		return setting.getChangeRequiredAction();
	}
	
}
