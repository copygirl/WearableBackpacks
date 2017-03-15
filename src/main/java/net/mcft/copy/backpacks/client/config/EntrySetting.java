package net.mcft.copy.backpacks.client.config;

import java.util.Objects;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ListEntryBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.config.Setting;

@SideOnly(Side.CLIENT)
public abstract class EntrySetting<T> extends ListEntryBase {
	
	public final Setting<T> setting;
	
	protected T value;
	
	@SuppressWarnings("unchecked")
	public EntrySetting(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<?> setting) {
		super(owningScreen, owningEntryList, new ConfigElement(setting.getProperty().setLanguageKey(
			"config." + WearableBackpacks.MOD_ID + "." + setting.getFullName())));
		this.setting = (Setting<T>)setting;
		value = this.setting.get();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> EntrySetting<T> Create(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<T> setting) {
		String entryClassName = setting.getConfigEntryClass();
		if (entryClassName == null) throw new RuntimeException(
			"Setting '" + setting.getFullName() + "' has no entry class defined");
		try {
			EntrySetting<T> entry = (EntrySetting<T>)Class.forName(entryClassName)
				.getConstructor(GuiConfig.class, GuiConfigEntries.class, Setting.class)
				.newInstance(owningScreen, owningEntryList, setting);
			entry.onValueChanged(); // Ugly hack to call onValueChanged after entry (and
			return entry;           // associated controls) have been fully constructed.
		} catch (Exception ex) { throw new RuntimeException(
			"Exception while instanciating setting entry for '" +
				setting.getFullName() + "' (entry class '" + entryClassName + "')", ex); }
	}
	
	/** Returns the height used for this list entry. */
	public int getSlotHeight() { return 20; }
	
	/** Sets the entry's internal value and calls onValueUpdated. */
	public void setValue(T value) { this.value = value; onValueChanged(); }
	
	/** Called when the entry's value is changed.
	 *  Overriden by implementing classes to update the control. */
	public void onValueChanged() {  }
	
	
	@Override
	public boolean isDefault() { return Objects.equals(value, setting.getDefault()); }
	@Override
	public void setToDefault() { if (!enabled()) return; setValue(setting.getDefault()); }
	
	@Override
	public boolean isChanged() { return !Objects.equals(value, setting.get()); }
	@Override
	public void undoChanges() { if (!enabled()) return; setValue(setting.get()); }
	
	@Override
	public boolean saveConfigElement() {
		if (!enabled() || !isChanged()) return false;
		setting.set(value);
		boolean reqRestart = setting.requiresMinecraftRestart();
		if (!reqRestart) setting.update();
		return reqRestart;
	}
	
	@Override
	public Object getCurrentValue() { return Objects.toString(value); }
	@Override
	public Object[] getCurrentValues() { return new Object[] { getCurrentValue() }; }
	
}
