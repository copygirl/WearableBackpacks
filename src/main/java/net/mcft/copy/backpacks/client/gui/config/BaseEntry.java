package net.mcft.copy.backpacks.client.gui.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.GuiTextureResource;
import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.GuiImage;
import net.mcft.copy.backpacks.client.gui.GuiLabel;
import net.mcft.copy.backpacks.client.gui.GuiLayout;
import net.mcft.copy.backpacks.client.gui.control.GuiButton;
import net.mcft.copy.backpacks.client.gui.control.GuiButtonGlyph;
import net.mcft.copy.backpacks.config.Status;
import net.mcft.copy.backpacks.config.Status.Severity;
import net.mcft.copy.backpacks.misc.util.LangUtils;

@SideOnly(Side.CLIENT)
public abstract class BaseEntry extends GuiLayout implements IConfigEntry {
	
	public static final GuiTextureResource TEXTURE_CONFIG_ICONS = new GuiTextureResource("config_icons", 64, 64);
	
	public final GuiImage iconStatus;
	public final GuiButton buttonUndo;
	public final GuiButton buttonReset;
	
	public BaseEntry() {
		super(Direction.HORIZONTAL);
		setFillHorizontal();
		
		iconStatus = new GuiImage(16, 16, TEXTURE_CONFIG_ICONS);
		iconStatus.setCenteredVertical();
		
		buttonUndo = new GuiButtonGlyph(ENTRY_HEIGHT, ENTRY_HEIGHT, GuiUtils.UNDO_CHAR, 1.0f);
		buttonUndo.setCenteredVertical();
		buttonUndo.setAction(this::undoChanges);
		
		buttonReset = new GuiButtonGlyph(ENTRY_HEIGHT, ENTRY_HEIGHT, GuiUtils.RESET_CHAR, 1.0f);
		buttonReset.setCenteredVertical();
		buttonReset.setAction(this::setToDefault);
	}
	
	protected static List<String> formatTooltip(String title, String text, String def, String warn) {
		List<String> tooltip = new ArrayList<String>();
		tooltip.add(TOOLTIP_TITLE + I18n.format(title));
		if (text != null) {
			LangUtils.format(tooltip, text);
			tooltip.set(1, TOOLTIP_TEXT + tooltip.get(1)); // Only first line should be yellow.
		}
		if (def != null) tooltip.add(TOOLTIP_DEFAULT + def);
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
		
		enableBlendAlphaStuffs();
		drawColoredRectARGB(16, -1, getWidth() - 12, getHeight() + 2, severity.backgroundColor);
		disableBlendAlphaStuffs();
		
		iconStatus.setTextureUV(severity.guiIconIndex * 16, 0);
		iconStatus.setTooltip(tooltip);
		
		buttonUndo.setEnabled(isChanged());
		buttonReset.setEnabled(!isDefault());
		
		super.draw(mouseX, mouseY, partialTicks);
	}
	
	
	public static abstract class Labelled extends BaseEntry {
		
		public final GuiLabel label;
		private String _labelText = null;
		
		public Labelled() {
			label = new GuiLabel("");
			label.setCenteredVertical();
			label.setShadowDisabled();
		}
		
		protected String getFormatting()
			{ return (isChanged() ? TextFormatting.ITALIC.toString() : ""); }
		
		@Override
		public void draw(int mouseX, int mouseY, float partialTicks) {
			if (_labelText == null) _labelText = label.getText();
			label.setText(getFormatting() + _labelText);
			
			Severity severity = Status.getSeverity(getStatus());
			boolean isFine    = (severity == Severity.FINE);
			label.setColor(!isEnabled() ? GuiUtils.getColorCode('8', true)
			             : isFine       ? GuiUtils.getColorCode('7', true)
			                            : severity.foregroundColor);
			
			super.draw(mouseX, mouseY, partialTicks);
		}
		
	}
	
}
