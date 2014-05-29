package net.mcft.copy.backpacks.client;

import net.mcft.copy.core.client.ModResourceLocation;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class BackpackResources {
	
	public static final ResourceLocation textureBackpack = new ModResourceLocation("textures/models/backpack.png");
	public static final ResourceLocation textureBackpackOverlay = new ModResourceLocation("textures/models/backpack_overlay.png");
	
	public static final ResourceLocation modelBackpack = new ModResourceLocation("models/backpack.obj");
	
	private BackpackResources() {  }
	
}
