package net.mcft.copy.backpacks.client;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.config.BackpacksGuiConfig;

@SideOnly(Side.CLIENT)
public class BackpacksGuiFactory implements IModGuiFactory {
		
	@Override
	public void initialize(Minecraft minecraftInstance) {  }
	
	@Override
	public boolean hasConfigGui() { return true; }
	
	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) { return new BackpacksGuiConfig(parentScreen); }
	
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() { return null; }
	
}
