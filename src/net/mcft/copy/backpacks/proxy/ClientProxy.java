package net.mcft.copy.backpacks.proxy;

import net.mcft.copy.backpacks.block.tileentity.TileEntityBackpack;
import net.mcft.copy.backpacks.client.BackpackResources;
import net.mcft.copy.backpacks.client.model.ModelBackpack;
import net.mcft.copy.backpacks.content.BackpackBlocks;
import net.mcft.copy.core.client.renderer.ItemRendererTileEntity;
import net.mcft.copy.core.client.renderer.TileEntityRenderer;
import net.mcft.copy.core.util.RegistryUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	
	@Override
	public void init() {
		
		super.init();
		
		registerTileEntityRenderer(
				BackpackBlocks.backpack, TileEntityBackpack.class,
				new TileEntityRenderer(BackpackResources.textureBackpack,
				                       new ModelBackpack(BackpackResources.modelBackpack)))
				.setScale(1.5F).setOffset(-0.08F).setInventoryRotation(70.0F)
				                                 .setThirdPersonRotation(-45.0F);
		
	}
	
	/** Register tile entity and item renderers for this block / tile entity.
	 *  Returns the item renderer for setting attitional options. */
	private ItemRendererTileEntity registerTileEntityRenderer(
			Block block, Class<? extends TileEntity> tileEntityClass,
			TileEntitySpecialRenderer tileEntityRenderer) {
		ItemRendererTileEntity itemRenderer =
				new ItemRendererTileEntity(tileEntityClass, tileEntityRenderer);
		// Only register renderers if block is enabled.
		if (RegistryUtils.isEnabled(block)) {
			ClientRegistry.bindTileEntitySpecialRenderer(tileEntityClass, tileEntityRenderer);
			MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(block), itemRenderer);
		}
		return itemRenderer;
	}
	
}
