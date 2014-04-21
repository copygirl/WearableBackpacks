package net.mcft.copy.backpacks.block;

import net.mcft.copy.backpacks.block.tileentity.TileEntityBackpack;
import net.mcft.copy.core.api.IRotatable4;
import net.mcft.copy.core.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockBackpack extends Block implements IRotatable4, ITileEntityProvider {
	
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
	
	// IRotatable4 implementation
	
	@Override
	public ForgeDirection getDirection(IBlockAccess world, int x, int y, int z) {
		TileEntityBackpack backpack = WorldUtils.getTileEntity(world, x, y, z, TileEntityBackpack.class);
		return ((backpack != null) ? backpack.orientation : ForgeDirection.UNKNOWN);
	}
	
	@Override
	public void setDirection(IBlockAccess world, int x, int y, int z, ForgeDirection direction) {
		TileEntityBackpack backpack = WorldUtils.getTileEntity(world, x, y, z, TileEntityBackpack.class);
		if (backpack != null)
			backpack.orientation = direction;
	}
	
	// ITileEntityProvider implementation
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityBackpack();
	}
	
}
