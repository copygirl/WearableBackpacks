package net.mcft.copy.backpacks.client.model;

import net.mcft.copy.core.client.model.CoreModelBase;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelBackpack extends CoreModelBase {

	public ModelBackpack(ResourceLocation resource) {
		super(resource);
	}
	
}
