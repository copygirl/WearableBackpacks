package net.mcft.copy.backpacks.client.gui.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.resources.I18n;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.gui.GuiContainer;
import net.mcft.copy.backpacks.client.gui.control.GuiButton;
import net.mcft.copy.backpacks.config.Status;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;

@SideOnly(Side.CLIENT)
public class EntryCategory extends BaseEntry {
	
	public static final int BUTTON_WIDTH = 300;
	
	public final String category;
	public final BackpacksConfigScreen childScreen;
	
	public EntryCategory(BackpacksConfigScreen owningScreen, String category) {
		this.category = category;
		
		GuiButton button = new GuiButton(BUTTON_WIDTH);
		button.setText(I18n.format(getLanguageKey()));
		button.setAction(this::onButtonPressed);
		button.setTooltip(getCategoryTooltip());
		
		setSpacing(0, 6, 4);
		addWeighted(new GuiContainer()); // Filler
		addFixed(button);
		addFixed(buttonUndo);
		addFixed(buttonReset);
		
		childScreen = new BackpacksConfigScreen(owningScreen, this);
	}
	
	public String getLanguageKey()
		{ return "config." + WearableBackpacks.MOD_ID + ".category." + category; }
	private List<String> getCategoryTooltip() {
		String langKey = getLanguageKey();
		return formatTooltip(langKey, langKey + ".tooltip", null, null);
	}
	
	@Override
	public List<Status> getStatus() {
		return childScreen.isValid()
			? Collections.emptyList()
			: Arrays.asList(Status.INVALID);
	}
	
	protected void onButtonPressed()
		{ display(childScreen); }
	
	// IConfigEntry implementation
	
	@Override
	public boolean isChanged() { return childScreen.isChanged(); }
	@Override
	public boolean isDefault() { return childScreen.isDefault(); }
	
	@Override
	public void undoChanges() { childScreen.undoChanges(); }
	@Override
	public void setToDefault() { childScreen.setToDefault(); }
	
	@Override
	public ChangeRequiredAction applyChanges()
		{ return childScreen.applyChanges(); }
	
}
