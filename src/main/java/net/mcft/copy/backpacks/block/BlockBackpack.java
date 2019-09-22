package net.mcft.copy.backpacks.block;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.block.entity.TileEntityBackpack;
import net.mcft.copy.backpacks.misc.util.LangUtils;
import net.mcft.copy.backpacks.misc.util.MiscUtils;
import net.mcft.copy.backpacks.misc.util.WorldUtils;

import javax.annotation.Nullable;

public class BlockBackpack extends BlockContainer {
	
	/** Number of ticks a backpack will be resistant
	 *  to explosions for after being placed. */
	public static final int EXPLOSION_RESIST_TICKS = 10;
	
	private final AxisAlignedBB[] _boundsFromFacing = new AxisAlignedBB[4];
	
	public BlockBackpack() {
		super(Material.CLOTH);
		setSoundType(SoundType.SNOW);
		setHardness(1.5F);
		initBlockBounds();
	}
	
	@Override
	public String getTranslationKey() {
		// Just use the item's unlocalized name for this block.
		return MiscUtils.getItemFromBlock(this).getTranslationKey();
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityBackpack();
	}
	
	// Block properties
	
	@Override
	public int quantityDropped(Random random) { return 0; }
	
	@Override
	public boolean isOpaqueCube(IBlockState state) { return false; }
	
	@Override
	public boolean isFullCube(IBlockState state) { return false; }
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.INVISIBLE; }
	
	// Block bounds
	
	protected float getBoundsWidth() { return 12 / 16.0F; }
	protected float getBoundsHeight() { return 13 / 16.0F; }
	protected float getBoundsDepth() { return 10 / 16.0F; }
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		TileEntity entity = source.getTileEntity(pos);
		EnumFacing facing = (entity instanceof TileEntityBackpack)
			? ((TileEntityBackpack)entity).facing
			: EnumFacing.NORTH;
		return _boundsFromFacing[facing.ordinal() - 2];
	}
	
	private void initBlockBounds() {
		float w = getBoundsWidth();
		float h = getBoundsHeight();
		float d = getBoundsDepth();
		for (int i = 0; i < _boundsFromFacing.length; i++) {
			EnumFacing facing = EnumFacing.byIndex(i + 2);
			_boundsFromFacing[i] = ((facing.getAxis() == Axis.Z)
				? new AxisAlignedBB(0.5F - w / 2, 0.0F, 0.5F - d / 2, 0.5F + w / 2, h, 0.5F + d / 2)
				: new AxisAlignedBB(0.5F - d / 2, 0.0F, 0.5F - w / 2, 0.5F + d / 2, h, 0.5F + w / 2));
		}
	}
	
	// Block methods / events
	
	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		IBackpack backpack = BackpackHelper.getBackpack(world.getTileEntity(pos));
		return (backpack != null)
			? backpack.getStack().copy()
			: super.getItem(world, pos, state);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state,
	                            EntityLivingBase placer, ItemStack stack) {
		// Set the facing value of the backpack when placed.
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity instanceof TileEntityBackpack)
			((TileEntityBackpack)tileEntity).facing = placer.getHorizontalFacing();
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos) {
		// Equipping a backpack is faster than breaking it.
		// Trying to equip a backpack when not possible will make it appear unbreakable.
		float hardness = super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
		boolean sneaking = player.isSneaking();
		boolean canEquip = BackpackHelper.canEquipBackpack(player);
		return (sneaking && !canEquip)
			? -1.0F /* Unbreakable */
			: (hardness * (sneaking ? 4 : 1));
	}
	
	private long _lastHelpMessage = System.currentTimeMillis();
	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		// Show a help message if the local player is trying to equip a backpack when it's not possible.
		if (world.isRemote && player.isSneaking() && !BackpackHelper.canEquipBackpack(player) &&
		    (System.currentTimeMillis() > _lastHelpMessage + 10 * 1000)) {
			boolean backpack = (BackpackHelper.getBackpack(player) != null);
			LangUtils.displayChatMessage("cantEquip" + (!backpack ? ".chestplate" : ""));
			_lastHelpMessage = System.currentTimeMillis();
		}
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
	                                EntityPlayer player, EnumHand hand,
	                                EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) return true;
		TileEntity entity = worldIn.getTileEntity(pos);
		IBackpack backpack = BackpackHelper.getBackpack(entity);
		if (backpack != null) backpack.getType().onPlacedInteract(player, entity, backpack);
		return true;
	}
	
	// Equipping / block breaking / drops related
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (!worldIn.isRemote && player.isSneaking() &&
		    BackpackHelper.canEquipBackpack(player))
			// On the server, try to equip the backpack
			// if the player is sneaking while breaking it.
			BackpackHelper.equipBackpack(player, worldIn.getTileEntity(pos));
	}

	protected void dropBackpack(World worldIn, BlockPos pos) {
		dropBackpack(worldIn, pos, worldIn.getTileEntity(pos));
	}

	protected void dropBackpack(World worldIn, BlockPos pos, TileEntity entity) {
		if (!worldIn.isRemote) {
			IBackpack backpack = BackpackHelper.getBackpack(entity);
			if ((backpack != null) && (backpack.getType() != null) && !(backpack.getStack().isEmpty())) {
				// This would drop the contents of a normal backpack.
				backpack.getType().onBlockBreak(entity, backpack);
				// This will drop the backpack itself, as the above method clears it.
				WorldUtils.dropStackFromBlock(worldIn, pos, backpack.getStack());
			}
		}
	}

	protected boolean preventExplosionDestroy(World world, BlockPos pos) {
		// (Age is set to -EXPLOSION_RESIST_TICKS after being dropped on death.)
		TileEntity entity = world.getTileEntity(pos);
		return (entity instanceof TileEntityBackpack) &&
				(((TileEntityBackpack)entity).getAge() < 0);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		// No-op, we handle drops ourselves
	}

	//this is only called by explosion and harvest block (which we override)
	@Override
	public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
		// Only drop the backpack block if its age isn't negative.
		// Otherwise we would cause a dupe, as below, the block is kept if age is negative,
		if (!preventExplosionDestroy(world, pos) && chance > 0.0f) {
			dropBackpack(world, pos);
		}
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		player.addStat(StatList.getBlockStats(this));
		player.addExhaustion(0.005F);

		harvesters.set(player);
		dropBackpack(worldIn, pos, te);
		harvesters.set(null);
	}

	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		// Only destroy the backpack block if its age isn't negative.
		if (preventExplosionDestroy(world, pos)) return;
		super.onBlockExploded(world, pos, explosion);
	}
	
}
