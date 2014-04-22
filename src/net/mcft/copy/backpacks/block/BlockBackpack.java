package net.mcft.copy.backpacks.block;

import net.mcft.copy.backpacks.block.tileentity.TileEntityBackpack;
import net.mcft.copy.core.api.IRotatable4;
import net.mcft.copy.core.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBackpack extends Block implements ITileEntityProvider, IRotatable4 {
	
	public BlockBackpack(Material material) {
		super(material);
	}
	public BlockBackpack() {
		super(Material.cloth);
		setStepSound(Block.soundTypeCloth);
	}
	
	/** Returns the backpack's bounding box width. */
	public float getBoundsWidth() { return 12 / 16.0F; }
	/** Returns the backpack's bounding box height. */
	public float getBoundsHeight() { return 13 / 16.0F; }
	/** Returns the backpack's bounding box depth. */
	public float getBoundsDepth() { return 10 / 16.0F; }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		blockIcon = iconRegister.registerIcon("wool_colored_brown");
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		float w = getBoundsWidth(), h = getBoundsHeight(), d = getBoundsDepth();
		ForgeDirection orientation = getDirection(world, x, y, z);
		if ((orientation == ForgeDirection.NORTH) || (orientation == ForgeDirection.SOUTH))
			setBlockBounds(0.5F - w / 2, 0.0F, 0.5F - d / 2, 0.5F + w / 2, h, 0.5F + d / 2);
		else if ((orientation == ForgeDirection.WEST) || (orientation == ForgeDirection.EAST))
			setBlockBounds(0.5F - d / 2, 0.0F, 0.5F - w / 2, 0.5F + d / 2, h, 0.5F + w / 2);
		else setBlockBounds(0.5F - w / 2, 0.0F, 0.5F - w / 2, 0.5F + w / 2, h, 0.5F + w / 2);
	}
	
	@Override
	public boolean isOpaqueCube() { return false; }
	
	@Override
	public boolean renderAsNormalBlock() { return false; }
	
	@Override
	public int getRenderType() { return -1; }
	
	// ITileEntityProvider implementation
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityBackpack();
	}
	
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
	
}
