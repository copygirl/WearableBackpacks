package net.mcft.copy.backpacks.block;

import net.mcft.copy.backpacks.block.tileentity.TileEntityBackpack;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockBackpack extends Block implements ITileEntityProvider {
	
	public BlockBackpack(Material material) {
		super(material);
	}
	public BlockBackpack() {
		super(Material.cloth);
		setStepSound(Block.soundTypeCloth);
	}
	
	@Override
	public boolean isOpaqueCube() { return false; }
	
	@Override
	public boolean renderAsNormalBlock() { return false; }
	
	@Override
	public int getRenderType() { return -1; }
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityBackpack();
	}
	
}
