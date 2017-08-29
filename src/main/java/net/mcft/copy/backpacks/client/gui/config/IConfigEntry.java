package net.mcft.copy.backpacks.client.gui.config;

import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;

@SideOnly(Side.CLIENT)
public interface IConfigEntry {
	
	public static final int ENTRY_HEIGHT = 18;
	
	public static final String TOOLTIP_TITLE   = TextFormatting.GREEN.toString();
	public static final String TOOLTIP_TEXT    = TextFormatting.YELLOW.toString();
	public static final String TOOLTIP_DEFAULT = TextFormatting.AQUA.toString();
	public static final String TOOLTIP_WARN    = TextFormatting.RED.toString();
	
	/** Returns whether this entry was changed from its previous value. */
	public boolean isChanged();
	/** Returns whether this entry's value is equal to its default value. */
	public boolean isDefault();
	/** Returns whether the control for this entry represents a valid value. */
	public boolean isValid();
	
	/** Sets this entry's value to the previous value. */
	public void undoChanges();
	/** Sets this enrtry's value to its default value. */
	public void setToDefault();
	
	/** Applies the changes made to this entry globally.
	 *  Called when clicking "Done" on the main config screen. */
	public ChangeRequiredAction applyChanges();
	
}
