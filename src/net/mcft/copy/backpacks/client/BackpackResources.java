package net.mcft.copy.backpacks.client;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.core.client.ModResource;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class BackpackResources {
	
	public static final ResourceLocation textureBackpack =
			new ModResource(WearableBackpacks.MOD_ID, "textures/models/backpack.png");
	public static final ResourceLocation textureBackpackOverlay =
			new ModResource(WearableBackpacks.MOD_ID, "textures/models/backpack_overlay.png");
	
	public static final ResourceLocation modelBackpack =
			new ModResource(WearableBackpacks.MOD_ID, "models/backpack.obj");
	
	private BackpackResources() {  }
	
}
