package net.mcft.copy.backpacks.client.gui.config;

import java.util.Optional;

import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.control.GuiButton;

@SideOnly(Side.CLIENT)
public abstract class EntryValueButton<T> extends GuiButton implements IConfigValue<T> {
	
	protected T value;
	
	public EntryValueButton() { setHeight(IConfigEntry.DEFAULT_ENTRY_HEIGHT); }
	
	@Override
	public Optional<T> getValue() { return Optional.of(value); }
	@Override
	public void setValue(T value) {
		this.value = value;
		setText(getButtonText(value));
	}
	
	protected String getButtonText(T value)
		{ return value.toString(); }
	
	public static class Switch extends EntryValueButton<Boolean> {
		@Override
		public void onPressed(int mouseX, int mouseY) { setValue(!value); }
		@Override
		protected String getButtonText(Boolean value){
			return (value ? TextFormatting.DARK_GREEN
			              : TextFormatting.DARK_RED)
				+ super.getButtonText(value);
		}
	}
	
}
