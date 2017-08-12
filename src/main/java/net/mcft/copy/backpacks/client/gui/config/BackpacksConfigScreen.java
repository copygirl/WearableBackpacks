package net.mcft.copy.backpacks.client.gui.config;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentString;

import net.minecraftforge.fml.client.config.GuiMessageDialog;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.gui.*;
import net.mcft.copy.backpacks.client.gui.control.*;
import net.mcft.copy.backpacks.client.gui.test.GuiTestScreen;
import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;

@SideOnly(Side.CLIENT)
public class BackpacksConfigScreen extends BaseConfigScreen {
	
	private final GuiButton _buttonTest;
	protected final GuiButton buttonReset;
	protected final GuiButton buttonUndo;
	
	/** Creates a config GUI screen for Wearable Backpacks (and its GENERAL category). */
	public BackpacksConfigScreen(GuiScreen parentScreen) {
		this(parentScreen, (String)null);
		
		// Add all settings from the GENERAL category to the entry list.
		for (Setting<?> setting : WearableBackpacks.CONFIG.getSettingsGeneral())
			addEntry(CreateEntryFromSetting(setting));
		
		// After adding all settings from the GENERAL category, add its sub-categories.
		for (String cat : WearableBackpacks.CONFIG.getCategories())
			addEntry(new EntryCategory(this, cat));
	}
	
	/** Creates a config GUI screen for a sub-category. */
	public BackpacksConfigScreen(GuiScreen parentScreen, EntryCategory category) {
		this(parentScreen, category.getLanguageKey());
		
		// Add all settings for this category to the entry list.
		for (Setting<?> setting : WearableBackpacks.CONFIG.getSettings(category.category))
			addEntry(CreateEntryFromSetting(setting));
	}
	
	public BackpacksConfigScreen(GuiScreen parentScreen, String subtitle) {
		super(parentScreen, WearableBackpacks.MOD_NAME, subtitle);
		
		_buttonTest = new GuiButton(18, 18, "T");
		_buttonTest.setRight(3);
		_buttonTest.setTop(3);
		_buttonTest.setAction(() -> GuiElementBase.display(new GuiTestScreen(BackpacksConfigScreen.this)));
		container.add(_buttonTest);
		
		// Buttons
		
		buttonUndo = new GuiButtonGlyph(GuiUtils.UNDO_CHAR, I18n.format("fml.configgui.tooltip.undoChanges"));
		buttonUndo.setAction(() -> undoChanges());
		
		buttonReset = new GuiButtonGlyph(GuiUtils.RESET_CHAR, I18n.format("fml.configgui.tooltip.resetToDefault"));
		buttonReset.setAction(() -> setToDefault());
		
		layoutButtons.addFixed(buttonUndo);
		layoutButtons.addFixed(buttonReset);
	}
	
	
	public static <T> GuiElementBase CreateEntryFromSetting(Setting<T> setting) {
		String entryClassName = setting.getConfigEntryClass();
		if (entryClassName == null) throw new RuntimeException(
			"Setting '" + setting.getFullName() + "' has no entry class defined");
		try {
			Class<?> entryClass = Class.forName(entryClassName);
			if (!GuiElementBase.class.isAssignableFrom(entryClass))
				throw new Exception("Not a subclass of GuiElementBase");
			// Find a constructor that has exactly one parameter of a compatible setting type.
			Constructor<?> constructor = Arrays.stream(entryClass.getConstructors())
				.filter(c -> (c.getParameterCount() == 1) && c.getParameterTypes()[0].isAssignableFrom(setting.getClass()))
				.findFirst().orElseThrow(() -> new Exception("No compatible constructor found"));
			// Create and return a new instance of this entry class.
			return (GuiElementBase)constructor.newInstance(setting);
		} catch (Exception ex) { throw new RuntimeException(
			"Exception while instanciating setting entry for '" +
				setting.getFullName() + "' (entry class '" + entryClassName + "')", ex); }
	}
	
	
	/** Returns whether any of this screen's entries were changed from their previous values. */
	public boolean isChanged() { return listEntries.getEntries().anyMatch(IConfigEntry::isChanged); }
	/** Returns whether all of this screen's entries are equal to their default values. */
	public boolean isDefault() { return listEntries.getEntries().allMatch(IConfigEntry::isDefault); }
	/** Returns whether all of this screen's entries represent a valid value. */
	public boolean isValid() { return listEntries.getEntries().allMatch(IConfigEntry::isValid); }
	
	/** Sets all of this screen's entries back to their previous values. */
	public void undoChanges() { listEntries.getEntries().forEach(IConfigEntry::undoChanges); }
	/** Sets all of this screen's entries to their default values. */
	public void setToDefault() { listEntries.getEntries().forEach(IConfigEntry::setToDefault); }
	
	/** Applies changes made to this screen's entries.
	 *  Called when clicking "Done" on the main config screen. */
	public ChangeRequiredAction applyChanges() {
		return listEntries.getEntries()
			.map(e -> e.applyChanges())
			.max(ChangeRequiredAction::compareTo)
			.orElse(ChangeRequiredAction.None);
	}
	
	@Override
	protected void doneClicked() {
		GuiScreen nextScreen = parentScreen;
		// If this is the root config screen, apply the changes!
		if (!(parentScreen instanceof BackpacksConfigScreen)) {
			if (applyChanges() == ChangeRequiredAction.RestartMinecraft)
				nextScreen = new GuiMessageDialog(parentScreen,
					"fml.configgui.gameRestartTitle",
					new TextComponentString(I18n.format("fml.configgui.gameRestartRequired")),
					"fml.configgui.confirmRestartMessage");
			WearableBackpacks.CONFIG.save();
		}
		GuiElementBase.display(nextScreen);
	}
	
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		_buttonTest.setVisible(GuiContext.DEBUG);
		buttonDone.setEnabled(isValid());
		buttonUndo.setEnabled(isChanged());
		buttonReset.setEnabled(!isDefault());
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
}
