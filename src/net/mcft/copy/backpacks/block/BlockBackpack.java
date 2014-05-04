package net.mcft.copy.backpacks.block;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.block.tileentity.TileEntityBackpack;
import net.mcft.copy.backpacks.item.ItemBackpack;
import net.mcft.copy.core.base.BlockTileEntityBase;
import net.mcft.copy.core.base.TileEntityBase;
import net.mcft.copy.core.misc.BlockLocation;
import net.mcft.copy.core.misc.rotatable.IRotatableBounds;
import net.mcft.copy.core.util.ClientUtils;
import net.mcft.copy.core.util.NameUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBackpack extends BlockTileEntityBase implements IRotatableBounds {
	
	private final Class<? extends ItemBackpack> itemClass;
	
	public BlockBackpack(Class<? extends ItemBackpack> itemClass, Material material) {
		super(material);
		this.itemClass = itemClass;
	}
	public BlockBackpack(Class<? extends ItemBackpack> itemClass) {
		this(itemClass, Material.cloth);
		setStepSound(Block.soundTypeCloth);
	}
	
	@Override
	public Class<? extends ItemBlock> getItemClass() { return itemClass; }
	
	@Override
	protected String getBlockNameInternal() { return NameUtils.getGameItemName(itemClass); }
	
	// Block methods
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		blockIcon = iconRegister.registerIcon("wool_colored_brown");
	}
	
	@Override
	public boolean isOpaqueCube() { return false; }
	
	@Override
	public boolean renderAsNormalBlock() { return false; }
	
	@Override
	public int getRenderType() { return -1; }
	
	@Override
	public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int x, int y, int z) {
		// Equipping a backpack is faster than breaking it.
		// Trying to equip a backpack when not possible will make it appear unbreakable.
		float hardness = super.getPlayerRelativeBlockHardness(player, world, x, y, z);
		boolean sneaking = player.isSneaking();
		boolean canEquip = BackpackHelper.canEquipBackpack(player);
		boolean stoppedSneaking = localPlayerStoppedSneaking(player);
		return ((stoppedSneaking || (sneaking && !canEquip)) ? -1.0F : (hardness * (sneaking ? 4 : 1)));
	}
	
	boolean lastSneaking = false;
	private boolean localPlayerStoppedSneaking(EntityPlayer player) {
		if (!player.worldObj.isRemote || (player != ClientUtils.getLocalPlayer())) return false;
		boolean stoppedSneaking = (!player.isSneaking() && lastSneaking);
		lastSneaking = player.isSneaking();
		return stoppedSneaking;
	}
	
	// BlockTileEntityBase methods
	
	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() { return TileEntityBackpack.class; }
	
	// IRotatable4 implementation
	
	@Override
	public ForgeDirection getDirection(BlockLocation block) {
		return block.getTileEntityStrict(TileEntityBackpack.class).orientation;
	}
	
	@Override
	public void setDirection(BlockLocation block, ForgeDirection direction) {
		block.getTileEntityStrict(TileEntityBackpack.class).orientation = direction;
	}
	
	// IRotatableBounds implementation
	
	@Override
	public float getBoundsWidth() { return 12 / 16.0F; }
	@Override
	public float getBoundsHeight() { return 13 / 16.0F; }
	@Override
	public float getBoundsDepth() { return 10 / 16.0F; }
	
}
