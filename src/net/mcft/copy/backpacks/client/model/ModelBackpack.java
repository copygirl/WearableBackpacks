package net.mcft.copy.backpacks.client.model;

import java.util.HashMap;
import java.util.Map;

import net.mcft.copy.core.client.model.CoreModelBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelBackpack extends CoreModelBase {
	
	private static Map<ResourceLocation, ModelBackpack> cached =
			new HashMap<ResourceLocation, ModelBackpack>();
	
	private float lidAngle = 0.0F;
	
	private ModelBackpack(ResourceLocation resource) {
		super(resource);
	}
	
	/** Gets or creates a cached backpack model from this resource location. */
	public static ModelBackpack getModel(ResourceLocation location) {
		ModelBackpack model = cached.get(location);
		return ((model != null) ? model : cached.put(location, new ModelBackpack(location)));
	}
	
	/** Sets the lid angle to use for the backpack's lid. */
	public void setLidAngle(float lidAngle) {
		this.lidAngle = lidAngle;
	}
	
	@Override
	protected void renderModel(Object entity, float partialTick) {
		model.renderAllExcept("lid");
		
		GL11.glPushMatrix();
		// TODO: Generalize lid positioning.
		GL11.glTranslatef(0, 9 / 16.0F, -3 / 16.0F);
		GL11.glRotatef(lidAngle, -1, 0, 0);
		GL11.glTranslatef(0, -9 / 16.0F, 3 / 16.0F);
		model.renderOnly("lid");
		GL11.glPopMatrix();
	}
	
}
