package net.mcft.copy.backpacks.client.model;

import java.util.HashMap;
import java.util.Map;

import net.mcft.copy.backpacks.api.IBackpackModel;
import net.mcft.copy.core.client.model.CoreModelBase;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelBackpack extends CoreModelBase implements IBackpackModel {
	
	private static Map<ResourceLocation, ModelBackpack> cached =
			new HashMap<ResourceLocation, ModelBackpack>();
	
	private ModelBackpack(ResourceLocation resource) {
		super(resource);
	}
	
	/** Gets or creates a cached backpack model from this resource location. */
	public static ModelBackpack getModel(ResourceLocation location) {
		ModelBackpack model = cached.get(location);
		return ((model != null) ? model : cached.put(location, new ModelBackpack(location)));
	}
	
}
