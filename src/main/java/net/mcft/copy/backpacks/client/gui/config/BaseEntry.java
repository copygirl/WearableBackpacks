package net.mcft.copy.backpacks.client.gui.config;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.client.config.GuiUtils;

import net.mcft.copy.backpacks.client.gui.*;
import net.mcft.copy.backpacks.client.gui.control.*;

public abstract class BaseEntry extends GuiLayout {
	
	public static final int DEFAULT_HEIGHT = 18;
	
	protected final BackpacksConfigScreen owningScreen;
	
	public final String labelText;
	public final GuiLabel label;
	public final GuiElementBase control;
	public final GuiButton buttonUndo;
	public final GuiButton buttonReset;
	
	public BaseEntry(BackpacksConfigScreen owningScreen, GuiElementBase control)
		{ this(owningScreen, null, control); }
	public BaseEntry(BackpacksConfigScreen owningScreen, String labelText, GuiElementBase control) {
		super(Direction.HORIZONTAL);
		this.owningScreen = owningScreen;
		setFillHorizontal();
		control.setHeight(DEFAULT_HEIGHT);
		
		if (labelText != null) {
			setSpacing(8, 6, 4);
			
			this.labelText = I18n.format(labelText);
			addFixed(label = new GuiLabel(this.labelText));
			label.setCenteredVertical();
			label.setShadowDisabled();
			
			addWeighted(this.control = control);
		} else {
			setSpacing(0, 6, 4);
			
			this.labelText = null;
			this.label = null;
			
			addWeighted(new GuiContainer()); // Filler
			addFixed(this.control = control);
		}
		
		addFixed(buttonUndo = new GuiButtonGlyph(DEFAULT_HEIGHT, DEFAULT_HEIGHT, GuiUtils.UNDO_CHAR, 1.0f));
		addFixed(buttonReset = new GuiButtonGlyph(DEFAULT_HEIGHT, DEFAULT_HEIGHT, GuiUtils.RESET_CHAR, 1.0f));
		buttonUndo.setAction(() -> undoChanges());
		buttonReset.setAction(() -> setToDefault());
	}
	
	public boolean hasLabel() { return (label != null); }
	public int getLabelWidth() { return (hasLabel() ? label.getWidth() : 0); }
	public void setLabelWidth(int value) { if (hasLabel()) label.setWidth(value); }
	
	protected String getFormatting() {
		String formatting = (
					!isValid()  ? TextFormatting.RED
				: isChanged() ? TextFormatting.WHITE
				: TextFormatting.GRAY
			).toString();
		if (isChanged()) formatting += TextFormatting.ITALIC;
		return formatting;
	}
	
	/** Returns whether this entry was changed from its previous value. */
	public abstract boolean isChanged();
	/** Returns whether this entry's value is equal to its default value. */
	public abstract boolean isDefault();
	/** Returns whether the control for this entry represents a valid value. */
	public boolean isValid() { return true; }
	
	/** Sets this entry's value to the previous value. */
	public abstract void undoChanges();
	/** Sets this enrtry's value to its default value. */
	public abstract void setToDefault();
	
	/** Called when this entry changes, updating its state. */
	protected void onChanged() {
		if (label != null) label.setText(getFormatting() + labelText);
		buttonUndo.setEnabled(isChanged());
		buttonReset.setEnabled(!isDefault());
		owningScreen.onChanged();
	}
	
}
