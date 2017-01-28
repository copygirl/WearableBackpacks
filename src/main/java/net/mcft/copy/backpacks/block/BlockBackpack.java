package net.mcft.copy.backpacks.block;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.block.entity.TileEntityBackpack;
import net.mcft.copy.backpacks.misc.util.LangUtils;
import net.mcft.copy.backpacks.misc.util.MiscUtils;

// FIXME: Currently shows missing texture as break particle.
public class BlockBackpack extends BlockContainer {
	
	private final AxisAlignedBB[] _boundsFromFacing = new AxisAlignedBB[4];
	
	public BlockBackpack() {
		super(Material.CLOTH);
		setSoundType(SoundType.SNOW);
		setHardness(1.5F);
		initBlockBounds();
	}
	
	@Override
	public String getUnlocalizedName() {
		// Just use the item's unlocalized name for this block.
		return MiscUtils.getItemFromBlock(this).getUnlocalizedName();
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
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.INVISIBLE; }
	
	// Block bounds
	
	protected float getBoundsWidth() { return 12 / 16.0F; }
	protected float getBoundsHeight() { return 13 / 16.0F; }
	protected float getBoundsDepth() { return 10 / 16.0F; }
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		TileEntity entity = source.getTileEntity(pos);
		EnumFacing facing = ((entity instanceof TileEntityBackpack)
			? ((TileEntityBackpack)entity).facing : EnumFacing.NORTH);
		return _boundsFromFacing[facing.ordinal() - 2];
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
	
	// Block methods / events
	
	// FIXME: Fix crash when ctrl-middle-clicking.
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target,
	                              World world, BlockPos pos, EntityPlayer player) {
		IBackpack backpack = BackpackHelper.getBackpack(world.getTileEntity(pos));
		return (backpack != null) ? backpack.getStack()
			: super.getPickBlock(state, target, world, pos, player);
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
		if (!worldIn.isRemote && player.isSneaking())
			// On the server, try to equip the backpack
			// if the player is sneaking while breaking it.
			BackpackHelper.equipBackpack(player, worldIn.getTileEntity(pos));
	}
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
	                               EntityPlayer player, boolean willHarvest) {
		// The super method will set the block to air before getDrops.
		// But we need the tile entity there to drop the backpack item.
		
		// Fun fact:
		//   "willHarvest" depends on if the block can be harvested with the current tool.
		//   For example stone can only be harvested with a pick. Without, harvestBlock and
		//   therefore getDrops aren't called and no items are dropped. This will always be
		//   true since we don't require a specific tool, but for correctness' sake, ...
		
		if (willHarvest) {
			// Super method calls this usually, we need to do it manually.
			onBlockHarvested(world, pos, state, player);
			return true;
		} else return super.removedByPlayer(state, world, pos, player, willHarvest);
	}
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		IBackpack backpack = BackpackHelper.getBackpack(world.getTileEntity(pos));
		return ((backpack != null) && (backpack.getStack() != null))
			? Arrays.asList(backpack.getStack()) // Return the backpack's stack if broken normally.
			: Collections.emptyList(); // If backpack is equipped, stack is set to null: Don't drop anything.
	}
	
	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos,
	                         IBlockState state, TileEntity entity, ItemStack stack) {
		// The super method calls getDrops and spawns the items in-world.
		super.harvestBlock(worldIn, player, pos, state, entity, stack);
		worldIn.setBlockToAir(pos); // Set block to air, as it was delayed from removedByPlayer.
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		// This is called when the block is set to air by any means.
		// Afterward, the tile entity is also removed automatically.
		TileEntity entity = worldIn.getTileEntity(pos);
		IBackpack backpack = BackpackHelper.getBackpack(entity);
		if ((backpack != null) && (backpack.getType() != null))
			// This would drop the contents of a normal backpack.
			backpack.getType().onBlockBreak(entity, backpack);
	}
	
}