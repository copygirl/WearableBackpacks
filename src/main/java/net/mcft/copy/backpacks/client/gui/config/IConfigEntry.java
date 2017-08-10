package net.mcft.copy.backpacks.client.gui.config;

import net.mcft.copy.backpacks.client.gui.control.GuiLabel;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;

public interface IConfigEntry {
	
	/** Returns this entry's label, or null if none. */
	public GuiLabel getLabel();
	
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
