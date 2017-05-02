package net.mcft.copy.backpacks.client.config;

import java.util.List;
import java.util.Objects;

import net.minecraft.client.renderer.GlStateManager;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ListEntryBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.GuiTextureResource;
import net.mcft.copy.backpacks.config.Setting;

@SideOnly(Side.CLIENT)
public abstract class EntrySetting<T> extends ListEntryBase {
	
	private static final GuiTextureResource CONFIG_ICONS =
		new GuiTextureResource("config_icons", 16, 16);
	
	public final Setting<T> setting;
	
	protected T value;
	
	private final HoverChecker _hintHoverChecker;
	private List<String> _hintTooltip = null;
	
	@SuppressWarnings("unchecked")
	public EntrySetting(GuiConfig owningScreen, GuiConfigEntries owningEntryList, Setting<?> setting) {
		super(owningScreen, owningEntryList, new ConfigElement(setting.getProperty().setLanguageKey(
			"config." + WearableBackpacks.MOD_ID + "." + setting.getFullName())));
		this.setting = (Setting<T>)setting;
		this.setting.setEntry(this);
		value = this.setting.get();
		_hintHoverChecker = new HoverChecker(0, 0, 0, 0, 400);
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
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight,
	                      int mouseX, int mouseY, boolean isSelected) {
		super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);
		
		// Update the tooltip bounds slighty to not include the space left of the label ...
		tooltipHoverChecker.updateBounds(y, y + slotHeight,
			owningScreen.entryList.labelX - 2,
			owningScreen.entryList.controlX - 8);
		
		// ... because we need space for the recommendation hint icon.
		_hintHoverChecker.updateBounds(y, y + 16,
			owningScreen.entryList.labelX - 20,
			owningScreen.entryList.labelX - 4);
		
		_hintTooltip = setting.getRecommendationHint();
		if (_hintTooltip != null) {
			GlStateManager.color(1, 1, 1);
			CONFIG_ICONS.bind();
			CONFIG_ICONS.drawQuad(owningScreen.entryList.labelX - 20, y, 0, 0, 16, 16);
		}
	}
	
	@Override
	public void drawToolTip(int mouseX, int mouseY) {
		super.drawToolTip(mouseX, mouseY);
		if ((_hintTooltip != null) && _hintHoverChecker.checkHover(mouseX, mouseY))
			owningScreen.drawToolTip(_hintTooltip, mouseX, mouseY);
	}
	
	/** Returns the height used for this list entry. */
	public int getSlotHeight() { return 20; }
	
	/** Gets the entry's current internal value. */
	public T getValue() { return value; }
	/** Sets the entry's current internal value and calls onValueUpdated. */
	public void setValue(T value) { this.value = value; onValueChanged(); }
	
	/** Called when the entry's value is changed.
	 *  Overriden by implementing classes to update the control. */
	public void onValueChanged() {  }
	
	
	@Override
	public boolean enabled() { return super.enabled() && setting.isEnabledConfig(); }
	
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
