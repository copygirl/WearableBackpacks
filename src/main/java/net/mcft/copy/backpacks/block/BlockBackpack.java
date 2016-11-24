package net.mcft.copy.backpacks.block;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.block.entity.TileEntityBackpack;

public class BlockBackpack extends BlockContainer {
	
	// TODO: Move this over to the tile entity instead and render the whole backpack in the TileEntitySpecialRenderer.
	public static final PropertyDirection FACING =
		PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	
	private final AxisAlignedBB[] _boundsFromFacing = new AxisAlignedBB[4];
	
	public BlockBackpack() {
		super(Material.CLOTH);
		setUnlocalizedName("wearablebackpacks.backpack");
		setSoundType(SoundType.SNOW);
		setHardness(1.5F);
		initBlockBounds();
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityBackpack();
	}
	
	// Block properties
	
	@Override
	public boolean isOpaqueCube(IBlockState state) { return false; }
	
	@Override
	public boolean isFullCube(IBlockState state) { return false; }
	
	// Block bounds
	
	protected float getBoundsWidth() { return 12 / 16.0F; }
	protected float getBoundsHeight() { return 13 / 16.0F; }
	protected float getBoundsDepth() { return 10 / 16.0F; }
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return _boundsFromFacing[state.getValue(FACING).ordinal() - 2];
	}
	
	private void initBlockBounds() {
		float w = getBoundsWidth();
		float h = getBoundsHeight();
		float d = getBoundsDepth();
		for (int i = 0; i < _boundsFromFacing.length; i++) {
			EnumFacing facing = EnumFacing.getFront(i + 2);
			_boundsFromFacing[i] = ((facing.getAxis() == Axis.Z)
				? new AxisAlignedBB(0.5F - w / 2, 0.0F, 0.5F - d / 2, 0.5F + w / 2, h, 0.5F + d / 2)
				: new AxisAlignedBB(0.5F - d / 2, 0.0F, 0.5F - w / 2, 0.5F + d / 2, h, 0.5F + w / 2));
		}
	}
	
	// Rendering related
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
	
	// Block methods / events
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target,
	                              World world, BlockPos pos, EntityPlayer player) {
		IBackpack backpack = BackpackHelper.getBackpack(world.getTileEntity(pos));
		return (backpack != null) ? backpack.getStack()
			: super.getPickBlock(state, target, world, pos, player);
	}
	
	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing,
	                                 float hitX, float hitY, float hitZ,
	                                 int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos) {
		// Equipping a backpack is faster than breaking it.
		// Trying to equip a backpack when not possible will make it appear unbreakable.
		float hardness = super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
		boolean sneaking = player.isSneaking();
		boolean canEquip = BackpackHelper.canEquipBackpack(player);
		// TODO: Is this needed still?
		//       Backpack breaks instantly after harvesting it while
		//       sneaking when already having one equipped, so yes.
		boolean stoppedSneaking = localPlayerStoppedSneaking(player);
		return ((stoppedSneaking || (sneaking && !canEquip))
			? -1.0F /* Unbreakable */
			: (hardness * (sneaking ? 4 : 1)));
	}
	
	boolean lastSneaking = false;
	private boolean localPlayerStoppedSneaking(EntityPlayer player) {
		if (!player.worldObj.isRemote) return false;
		boolean stoppedSneaking = (!player.isSneaking() && lastSneaking);
		lastSneaking = player.isSneaking();
		return stoppedSneaking;
	}
	
	private long _lastHelpMessage = System.currentTimeMillis();
	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (world.isRemote && player.isSneaking() && !BackpackHelper.canEquipBackpack(player) &&
		    // TODO: Implement "enable help tooltips" config option.
		    //BetterStorage.globalConfig.getBoolean(GlobalConfig.enableHelpTooltips) &&
		    (System.currentTimeMillis() > _lastHelpMessage + 10 * 1000)) {
			boolean backpack = (BackpackHelper.getBackpack(player) != null);
			player.addChatMessage(new TextComponentTranslation(
				"notice.wearablebackpacks.backpack.cantEquip." + (backpack ? "backpack" : "chestplate")));
			_lastHelpMessage = System.currentTimeMillis();
		}
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
	                                EntityPlayer player, EnumHand hand, ItemStack heldItem,
	                                EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) return true;
		TileEntity entity = worldIn.getTileEntity(pos);
		IBackpack backpack = BackpackHelper.getBackpack(entity);
		if (backpack != null) backpack.getType().onPlacedInteract(player, entity, backpack);
		return true;
	}
	
	// Equipping / item dropping logic
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (!worldIn.isRemote && player.isSneaking() &&
		    BackpackHelper.equipBackpack(player, worldIn.getTileEntity(pos)))
			worldIn.setTileEntity(pos, null);
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity entity = worldIn.getTileEntity(pos);
		IBackpack backpack = BackpackHelper.getBackpack(entity);
		if ((backpack != null) && (backpack.getType() != null))
			backpack.getType().onBlockBreak(entity, backpack);
		// Don't call the super method, as it removes
		// the tile entity, which we need in getDrops.
	}
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		IBackpack backpack = BackpackHelper.getBackpack(world.getTileEntity(pos));
		return Arrays.asList((backpack != null)
			? (backpack.getStack() != null) // If we have a backpack entity, drop either its stack ..
				? backpack.getStack() : new ItemStack(this) // .. or in case it's missing, create a new one.
			: null); // No backpack entity likely means the backpack was equipped, so don't drop anything.
	}
	
	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos,
	                         IBlockState state, TileEntity entity, ItemStack stack) {
		super.harvestBlock(worldIn, player, pos, state, entity, stack);
		worldIn.removeTileEntity(pos); // Remove the tile entity, which was delayed.
	}
	
	// Blockstates
	
	@Override
	protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, FACING); }
	
	@Override
	public int getMetaFromState(IBlockState state) { return state.getValue(FACING).ordinal() - 2; }
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getFront((meta & 3) + 2));
	}
	
}