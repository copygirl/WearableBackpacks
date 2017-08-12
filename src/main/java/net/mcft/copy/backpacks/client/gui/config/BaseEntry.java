package net.mcft.copy.backpacks.client.gui.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import net.minecraftforge.fml.client.config.GuiUtils;

import net.mcft.copy.backpacks.client.gui.*;
import net.mcft.copy.backpacks.client.gui.control.*;
import net.mcft.copy.backpacks.misc.util.LangUtils;

public abstract class BaseEntry extends GuiLayout implements IConfigEntry {
	
	public final GuiButton buttonUndo;
	public final GuiButton buttonReset;
	
	public BaseEntry() {
		super(Direction.HORIZONTAL);
		setFillHorizontal();
		
		buttonUndo = new GuiButtonGlyph(DEFAULT_HEIGHT, DEFAULT_HEIGHT, GuiUtils.UNDO_CHAR, 1.0f);
		buttonUndo.setCenteredVertical();
		buttonUndo.setAction(() -> undoChanges());
		
		buttonReset = new GuiButtonGlyph(DEFAULT_HEIGHT, DEFAULT_HEIGHT, GuiUtils.RESET_CHAR, 1.0f);
		buttonReset.setCenteredVertical();
		buttonReset.setAction(() -> setToDefault());
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
	
	@Override
	public boolean isValid() { return true; }
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
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
