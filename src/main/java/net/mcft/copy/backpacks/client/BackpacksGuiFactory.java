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
	public Class<? extends GuiScreen> mainConfigGuiClass() { return BackpacksGuiConfig.class; }
	
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() { return null; }
	
	@SuppressWarnings("deprecation")
	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) { return null; }
	
}
