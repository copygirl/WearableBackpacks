package net.mcft.copy.backpacks.client.gui.config;

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
		
		childScreen = new BackpacksConfigScreen(owningScreen, this);
		super.onChanged();
	}
	
	public String getLanguageKey()
		{ return "config." + WearableBackpacks.MOD_ID + ".category." + category; }
	
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
	protected void onChanged() {
		super.onChanged();
		owningScreen.onChanged();
	}
	
	@Override
	public ChangeRequiredAction applyChanges()
		{ return childScreen.applyChanges(); }
	
	protected void onButtonPressed() {
		display(childScreen);
	}
	
}
