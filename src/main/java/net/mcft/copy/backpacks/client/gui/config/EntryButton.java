package net.mcft.copy.backpacks.client.gui.config;

import net.minecraftforge.fml.client.config.GuiUtils;

import net.mcft.copy.backpacks.client.gui.control.GuiButton;
import net.mcft.copy.backpacks.config.Setting;

public abstract class EntryButton<T> extends BaseEntrySetting<T> {
	
	public EntryButton(BackpacksConfigScreen owningScreen, Setting<T> setting)
		{ this(owningScreen, setting, new GuiButton()); }
	public EntryButton(BackpacksConfigScreen owningScreen, Setting<T> setting, GuiButton button) {
		super(owningScreen, setting, button);
		button.setAction(this::onButtonPressed);
	}
	
	public GuiButton getButton() { return (GuiButton)control; }
	
	@Override
	protected void onChanged()
		{ getButton().setText(getValue().map(Object::toString).orElse("<empty>")); }
	
	protected void onButtonPressed() {  }
	
	
	public static class Switch extends EntryButton<Boolean> {
		public Switch(BackpacksConfigScreen owningScreen, Setting<Boolean> setting)
			{ super(owningScreen, setting); }
		@Override
		protected void onChanged() {
			super.onChanged();
			char chr = getValue().map(v -> v ? '2' : '4').orElse('7');
			getButton().setTextColor(GuiUtils.getColorCode(chr, true));
		}
		@Override
		protected void onButtonPressed() { setValue(!getValue().get()); }
	}
	
}
