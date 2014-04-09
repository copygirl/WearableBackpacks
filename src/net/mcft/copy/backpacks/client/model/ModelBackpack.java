package net.mcft.copy.backpacks.client.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.mcft.copy.backpacks.client.BackpackResources;
import net.mcft.copy.core.client.model.CoreModelBase;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

@SideOnly(Side.CLIENT)
public class ModelBackpack extends CoreModelBase {

	public ModelBackpack(ResourceLocation resource) {
		super(resource);
	}
	
}
