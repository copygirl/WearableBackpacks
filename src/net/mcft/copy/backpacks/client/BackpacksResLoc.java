package net.mcft.copy.backpacks.client;

import java.util.Locale;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.minecraft.util.ResourceLocation;

public class BackpacksResLoc extends ResourceLocation {
	
	public BackpacksResLoc(String location) {
		super(WearableBackpacks.MOD_ID.toLowerCase(Locale.ENGLISH), location);
	}
	
}
