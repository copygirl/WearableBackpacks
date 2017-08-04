package net.mcft.copy.backpacks.client.gui.config;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.client.config.GuiUtils;

import net.mcft.copy.backpacks.client.gui.*;
import net.mcft.copy.backpacks.client.gui.control.*;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;

public abstract class BaseEntry extends GuiLayout {
	
	public static final int DEFAULT_HEIGHT = 18;
	public static final int BACKGROUND_INVALID = 0x40D00000;
	
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
		this.labelText = (labelText != null) ? I18n.format(labelText) : null;
		this.control = control;
		control.setHeight(DEFAULT_HEIGHT);
		setFillHorizontal();
		
		if (labelText != null) {
			label = new GuiLabel(this.labelText);
			label.setCenteredVertical();
			label.setShadowDisabled();
			
			setSpacing(8, 6, 4);
			addFixed(label);
			addWeighted(control);
		} else {
			label = null;
			setSpacing(0, 6, 4);
			addWeighted(new GuiContainer()); // Filler
			addFixed(control);
		}
		
		buttonUndo = new GuiButtonGlyph(DEFAULT_HEIGHT, DEFAULT_HEIGHT, GuiUtils.UNDO_CHAR, 1.0f);
		buttonUndo.setCenteredVertical();
		buttonUndo.setAction(() -> undoChanges());
		addFixed(buttonUndo);
		
		buttonReset = new GuiButtonGlyph(DEFAULT_HEIGHT, DEFAULT_HEIGHT, GuiUtils.RESET_CHAR, 1.0f);
		buttonReset.setCenteredVertical();
		buttonReset.setAction(() -> setToDefault());
		addFixed(buttonReset);
	}
	
	public boolean hasLabel() { return (label != null); }
	public int getLabelWidth() { return (hasLabel() ? label.getWidth() : 0); }
	public void setLabelWidth(int value) { if (hasLabel()) label.setWidth(value); }
	
	protected String getFormatting() {
		return (!isEnabled() ? TextFormatting.DARK_GRAY
		      : !isValid()   ? TextFormatting.RED
		      : isChanged()  ? TextFormatting.WHITE
		                     : TextFormatting.GRAY).toString()
			+ (isChanged() ? TextFormatting.ITALIC.toString() : "");
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
	
	/** Called when this entry's value changes, updating its control's state. */
	protected void onChanged() {  }
	
	/** Applies the changes made to this entry globally.
	 *  Called when clicking "Done" on the main config screen. */
	public abstract ChangeRequiredAction applyChanges();
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		if (label != null) label.setText(getFormatting() + labelText);
		buttonUndo.setEnabled(isChanged());
		buttonReset.setEnabled(!isDefault());
		
		if (!isValid()) {
			GlStateManager.disableTexture2D();
			drawColoredRectARGB(-4, -1, getWidth() + 8, getHeight() + 2, BACKGROUND_INVALID);
			GlStateManager.enableTexture2D();
		}
		
		super.draw(mouseX, mouseY, partialTicks);
	}
	
}
