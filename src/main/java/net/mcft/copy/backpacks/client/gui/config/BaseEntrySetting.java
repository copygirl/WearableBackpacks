package net.mcft.copy.backpacks.client.gui.config;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;
import net.mcft.copy.backpacks.client.gui.control.GuiLabel;
import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;

public abstract class BaseEntrySetting<T> extends BaseEntry {
	
	public final Setting<T> setting;
	protected final GuiElementBase control;
	
	private final T _previousValue;
	private final T _defaultValue;
	private Optional<T> _value;
	
	private final String _labelText;
	private final GuiLabel _label;
	
	public BaseEntrySetting(Setting<T> setting, GuiElementBase control) {
		this.setting = setting;
		this.setting.setEntry(this);
		this.control = control;
		
		_previousValue = setting.get();
		_defaultValue  = setting.getDefault();
		_value = Optional.of(_previousValue);
		
		_labelText = I18n.format(getLanguageKey());
		_label = new GuiLabel(_labelText);
		_label.setCenteredVertical();
		_label.setShadowDisabled();
		_label.setTooltip(getSettingTooltip());
		
		control.setHeight(DEFAULT_HEIGHT);
		
		setSpacing(8, 6, 4);
		addFixed(_label);
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
	
	@SuppressWarnings("unchecked")
	public static <T> BaseEntrySetting<T> Create(BackpacksConfigScreen owningScreen, Setting<T> setting) {
		String entryClassName = setting.getConfigEntryClass();
		if (entryClassName == null) throw new RuntimeException(
			"Setting '" + setting.getFullName() + "' has no entry class defined");
		try {
			Constructor<?> constructor = Arrays.stream(Class.forName(entryClassName).getConstructors())
				.filter(c -> (c.getParameterCount() == 1) && c.getParameterTypes()[0].isAssignableFrom(setting.getClass()))
				.findFirst().orElseThrow(() -> new Exception("No compatible constructor found"));
			// Create and return a new instance of this entry class.
			return (BaseEntrySetting<T>)constructor.newInstance(setting);
		} catch (Exception ex) { throw new RuntimeException(
			"Exception while instanciating setting entry for '" +
				setting.getFullName() + "' (entry class '" + entryClassName + "')", ex); }
	}
	
	protected String getFormatting() {
		return (!isEnabled() ? TextFormatting.DARK_GRAY
		      : !isValid()   ? TextFormatting.RED
		      : isChanged()  ? TextFormatting.WHITE
		                     : TextFormatting.GRAY).toString()
			+ (isChanged() ? TextFormatting.ITALIC.toString() : "");
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
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		_label.setText(getFormatting() + _labelText);
		super.draw(mouseX, mouseY, partialTicks);
	}
	
	// IConfigEntry implementation
	
	@Override
	public GuiLabel getLabel() { return _label; }
	
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
