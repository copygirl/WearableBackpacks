package net.mcft.copy.backpacks.client.gui.config;

import java.util.List;

import net.minecraft.client.resources.I18n;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.gui.control.GuiButton;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;

public class EntryCategory extends BaseEntry {
	
	public static final int BUTTON_WIDTH = 300;
	
	public final String category;
	public final BackpacksConfigScreen childScreen;
	
	public EntryCategory(BackpacksConfigScreen owningScreen, String category) {
		super(owningScreen, new GuiButton(BUTTON_WIDTH));
		this.category = category;
		
		GuiButton button = (GuiButton)control;
		button.setText(I18n.format(getLanguageKey()));
		button.setAction(() -> onButtonPressed());
		button.setTooltip(getCategoryTooltip());
		
		childScreen = new BackpacksConfigScreen(owningScreen, this);
	}
	
	public String getLanguageKey()
		{ return "config." + WearableBackpacks.MOD_ID + ".category." + category; }
	private List<String> getCategoryTooltip() {
		String langKey = getLanguageKey();
		return formatTooltip(langKey, langKey + ".tooltip", null, null);
	}
	
	@Override
	public boolean isChanged() { return childScreen.isChanged(); }
	@Override
	public boolean isDefault() { return childScreen.isDefault(); }
	@Override
	public boolean isValid() { return childScreen.isValid(); }
	
	@Override
	public void undoChanges() { childScreen.undoChanges(); }
	@Override
	public void setToDefault() { childScreen.setToDefault(); }
	
	@Override
	public ChangeRequiredAction applyChanges()
		{ return childScreen.applyChanges(); }
	
	protected void onButtonPressed() {
		display(childScreen);
	}
	
}
