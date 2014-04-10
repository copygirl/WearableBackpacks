package net.mcft.copy.backpacks.client;

import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class BackpackResources {
	
	public static final ResourceLocation textureBackpack = new BackpacksResLoc("textures/models/backpack.png");
	
	public static final ResourceLocation modelBackpack = new BackpacksResLoc("models/backpack.obj");
	
	private BackpackResources() {  }
	
}
