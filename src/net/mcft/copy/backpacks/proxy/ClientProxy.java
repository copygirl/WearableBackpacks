package net.mcft.copy.backpacks.proxy;

import net.mcft.copy.backpacks.block.tileentity.TileEntityBackpack;
import net.mcft.copy.backpacks.client.BackpackResources;
import net.mcft.copy.backpacks.client.model.ModelBackpack;
import net.mcft.copy.core.client.renderer.TileEntityRenderer;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	
	@Override
	public void init() {
		
		super.init();
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBackpack.class,
				new TileEntityRenderer(BackpackResources.textureBackpack,
				                       new ModelBackpack(BackpackResources.modelBackpack)));
		
	}
	
}
