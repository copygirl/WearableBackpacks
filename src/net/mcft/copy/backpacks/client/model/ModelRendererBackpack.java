package net.mcft.copy.backpacks.client.model;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.core.client.Color;
import net.mcft.copy.core.client.model.CoreModelBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModelRendererBackpack extends ModelRenderer {
	
	private EntityLivingBase entity;
	private ItemStack backpack;
	
	public ModelRendererBackpack(ModelBase modelBase) {
		super(modelBase);
	}
	
	/** Sets the entity and backpack item used for this render iteration. */
	public void setEntityAndBackpack(EntityLivingBase entity, ItemStack backpack) {
		this.entity = entity;
		this.backpack = backpack;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void render(float partialTicks) {
		if (backpack == null) return;
		
		GL11.glPushAttrib(GL11.GL_CURRENT_BIT | GL11.GL_TEXTURE_BIT);
		GL11.glPushMatrix();
		
		GL11.glScalef(0.8F, 0.8F, 0.8F);
		GL11.glTranslatef(0, 12 / 16.0F, 5.5F / 16.0F);
		GL11.glRotatef(180, 0, 0, 1.0F);
		
		IBackpack backpackType = BackpackHelper.getBackpackType(backpack);
		CoreModelBase modelBackpack = ModelBackpack.getModel(backpackType.getModel(backpack));
		int passes = backpack.getItem().getRenderPasses(backpack.getItemDamage());
		for (int pass = 0; pass < passes; pass++) {
			Minecraft.getMinecraft().renderEngine.bindTexture(backpackType.getTexture(backpack, pass));
			Color.fromRGB(backpack.getItem().getColorFromItemStack(backpack, pass)).setActiveGLColor();
			modelBackpack.render(entity, 0, 0, 0, 0);
		}
		
		GL11.glPopMatrix();
		GL11.glPopAttrib();
		
		backpack = null;
	}
	
}
