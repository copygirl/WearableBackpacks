package net.mcft.copy.backpacks.client.gui.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.GuiTextureResource;
import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;
import net.mcft.copy.backpacks.client.gui.GuiImage;
import net.mcft.copy.backpacks.client.gui.GuiLabel;
import net.mcft.copy.backpacks.client.gui.GuiLayout;
import net.mcft.copy.backpacks.client.gui.control.GuiButton;
import net.mcft.copy.backpacks.client.gui.control.GuiButtonGlyph;
import net.mcft.copy.backpacks.config.Status;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;
import net.mcft.copy.backpacks.config.Status.Severity;
import net.mcft.copy.backpacks.misc.util.LangUtils;

@SideOnly(Side.CLIENT)
public abstract class BaseEntry extends GuiLayout implements IConfigEntry {
	
	public static final GuiTextureResource TEXTURE_CONFIG_ICONS = new GuiTextureResource("config_icons", 64, 64);
	
	public final GuiImage iconStatus;
	public final GuiLabel label;
	public final GuiButton buttonUndo;
	public final GuiButton buttonReset;
	
	public BaseEntry() {
		super(Direction.HORIZONTAL);
		setFillHorizontal();
		
		iconStatus = new GuiImage(16, 16, TEXTURE_CONFIG_ICONS);
		iconStatus.setCenteredVertical();
		
		label = new GuiLabel("");
		label.setCenteredVertical();
		label.setShadowDisabled();
		
		buttonUndo = new GuiButtonGlyph(DEFAULT_ENTRY_HEIGHT, DEFAULT_ENTRY_HEIGHT, GuiUtils.UNDO_CHAR, 1.0f);
		buttonUndo.setCenteredVertical();
		buttonUndo.setAction(this::undoChanges);
		
		buttonReset = new GuiButtonGlyph(DEFAULT_ENTRY_HEIGHT, DEFAULT_ENTRY_HEIGHT, GuiUtils.RESET_CHAR, 1.0f);
		buttonReset.setCenteredVertical();
		buttonReset.setAction(this::setToDefault);
	}
	
	public boolean hasLabel() { return (label.getParent() == this); }
	
	public void setLabel(String languageKey)
		{ label.setText(I18n.format(languageKey)); }
	public void setLabelAndTooltip(String languageKey)
		{ setLabelAndTooltip(languageKey, null, null); }
	public void setLabelAndTooltip(String languageKey, String def, String warn) {
		languageKey = "config." + WearableBackpacks.MOD_ID + "." + languageKey;
		setLabel(languageKey);
		label.setTooltip(formatTooltip(languageKey, languageKey + ".tooltip", def, warn));
	}
	protected List<String> formatTooltip(String title, String text, String def, String warn) {
		List<String> tooltip = new ArrayList<String>();
		tooltip.add(TOOLTIP_TITLE + I18n.format(title));
		if (text != null) {
			LangUtils.format(tooltip, text);
			if (text.equals(tooltip.get(1))) tooltip.remove(1); // Remove non-translated text.
			else tooltip.set(1, TOOLTIP_TEXT + tooltip.get(1)); // Only first line should be yellow.
		}
		if (def != null)  tooltip.add(TOOLTIP_DEFAULT + I18n.format("fml.configgui.tooltip.default", def));
		if (warn != null) tooltip.add(TOOLTIP_WARN + "[" + I18n.format(warn) + "]");
		return tooltip;
	}
	
	/** Returns a list of statuses for this entry. These include
	 *  hints, warnings and errors about the current entry value. */
	public abstract List<Status> getStatus();
	
	@Override
	public final boolean isValid()
		{ return (Status.getSeverity(getStatus()) != Severity.ERROR); }
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		List<Status> status  = getStatus();
		Severity severity    = Status.getSeverity(status);
		List<String> tooltip = Status.getMessage(status);
		boolean isFine       = (severity == Severity.FINE);
		
		enableBlendAlphaStuffs();
		drawColoredRectARGB(16, -1, getWidth() - 12, getHeight() + 2, severity.backgroundColor);
		disableBlendAlphaStuffs();
		
		iconStatus.setTextureUV(severity.guiIconIndex * 16, 0);
		iconStatus.setTooltip(tooltip);
		
		if (hasLabel()) {
			String text = label.getText();
			if (text.startsWith(TextFormatting.ITALIC.toString())) text = text.substring(2);
			if (isChanged()) text = TextFormatting.ITALIC + text;
			label.setText(text);
			label.setColor(!isEnabled() ? GuiUtils.getColorCode('8', true)
			             : isFine       ? GuiUtils.getColorCode('7', true)
			                            : severity.foregroundColor);
		}
		
		buttonUndo.setEnabled(isChanged());
		buttonReset.setEnabled(!isDefault());
		
		super.draw(mouseX, mouseY, partialTicks);
	}
	
		
	public static class Value<T> extends BaseEntry {
		
		protected IConfigValue<T> control;
		public final Optional<T> previousValue;
		public final Optional<T> defaultValue;
		
		public Value(IConfigValue<T> control, T currentValue, T defaultValue)
			{ this(control, Optional.ofNullable(currentValue), Optional.ofNullable(defaultValue)); }
		public Value(IConfigValue<T> control, Optional<T> currentValue, Optional<T> defaultValue) {
			if (control == null) throw new NullPointerException("control must not be null");
			this.control       = control;
			this.previousValue = currentValue;
			this.defaultValue  = defaultValue;
			
			setSpacing(4, 8, 6, 4);
			addFixed(iconStatus);
			addFixed(label);
			addWeighted((GuiElementBase)control);
			if (previousValue.isPresent()) addFixed(buttonUndo);
			if (defaultValue.isPresent()) addFixed(buttonReset);
			
			currentValue.ifPresent(this::setValue);
		}
		
		public Optional<T> getValue() { return control.getValue(); }
		public void setValue(T value) { control.setValue(value); }
		
		@Override
		public List<Status> getStatus() {
			List<Status> status = new ArrayList<Status>();
			if (!getValue().isPresent()) status.add(Status.INVALID);
			return status;
		}
		
		@Override
		public void draw(int mouseX, int mouseY, float partialTicks) {
			if (control instanceof IConfigValue.ShowsStatus)
				((IConfigValue.ShowsStatus)control).setStatus(getStatus());
			super.draw(mouseX, mouseY, partialTicks);
		}
		
		// IConfigEntry implementation
		
		@Override
		public boolean isChanged() { return !previousValue.equals(getValue()); }
		@Override
		public boolean isDefault() { return defaultValue.equals(getValue()); }
		
		@Override
		public void undoChanges() { previousValue.ifPresent(this::setValue); }
		@Override
		public void setToDefault() { defaultValue.ifPresent(this::setValue); }
		
		@Override
		public ChangeRequiredAction applyChanges()
			{ return ChangeRequiredAction.None; }
		
	}
	
}
