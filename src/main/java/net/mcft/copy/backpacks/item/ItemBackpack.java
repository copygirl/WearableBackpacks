package net.mcft.copy.backpacks.item;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.api.IBackpackType;
import net.mcft.copy.backpacks.container.ContainerBackpack;
import net.mcft.copy.backpacks.item.recipe.IDyeableItem;
import net.mcft.copy.backpacks.misc.BackpackDataItems;
import net.mcft.copy.backpacks.misc.util.WorldUtils;

// TODO: Turn this into ItemArmor?
// TODO: Support armor enchantments like on BetterStorage backpacks?
// TODO: Implement additional enchantments?
public class ItemBackpack extends Item implements IBackpackType, IDyeableItem {
	
	public static final int DEFAULT_COLOR = 0xA06540;
	
	
	public ItemBackpack() {
		setMaxStackSize(1);
	}
	
	@Override
	public boolean canDye(ItemStack stack) { return true; }
	
	// Item properties
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		// TODO: Move tooltip adding code into helper class. DRY!
		boolean enableHelpTooltips = WearableBackpacks.CONFIG.enableHelpTooltips.getValue();
		// Check if the stack is the player's currently equipped backpack.
		IBackpack backpack = BackpackHelper.getBackpack(playerIn);
		if ((backpack != null) && (backpack.getStack() == stack)) {
			// If someone's using the player's backpack, display it in the tooltip.
			// As long as someone's accessing the backpack, it can't be placed down.
			if (backpack.getPlayersUsing() > 0)
				tooltip.addAll(Arrays.asList(I18n.format(
					"tooltip.wearablebackpacks.backpack.used").split("\\\\n")));
			// Otherwise, if help tooltips are enabled, display the unequip hint.
			else if (enableHelpTooltips)
				tooltip.addAll(Arrays.asList(I18n.format(
					"tooltip.wearablebackpacks.backpack.unequipHint").split("\\\\n")));
		} else if (enableHelpTooltips)
			// Display the equip hint. If the chestplate setting is off, use the
			// extended tooltip, which also explains how to unequip the backpack.
			tooltip.addAll(Arrays.asList(I18n.format(
				"tooltip.wearablebackpacks.backpack.equipHint" +
				(!BackpackHelper.equipAsChestArmor ? ".extended" : "")).split("\\\\n")));
	}
	
	// Item events
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos,
	                                  EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IBlockState state = worldIn.getBlockState(pos);
		// If the block is replaceable, keep the placing position
		// the same but check the block below for solidity.
		if (state.getBlock().isReplaceable(worldIn, pos))
			state = worldIn.getBlockState(pos.offset(EnumFacing.DOWN));
		// Otherwise make sure the top side is used, and
		// change the placing position to the block above.
		else if (facing == EnumFacing.UP)
			pos = pos.offset(EnumFacing.UP);
		else return EnumActionResult.FAIL;
		
		// Check if the side is solid and try to place the backpack.
		return (state.isSideSolid(worldIn, pos, EnumFacing.UP) &&
		        BackpackHelper.placeBackpack(worldIn, pos, stack, playerIn))
			? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
	}
	
	// IBackpackType implementation
	
	@Override
	public void onSpawnedWith(EntityLivingBase entity, IBackpack backpack) {
		// TODO: Fill backpack with random items.
	}
	
	@Override
	public void onEquip(EntityLivingBase entity, TileEntity tileEntity, IBackpack backpack) {  }
	
	@Override
	public void onUnequip(EntityLivingBase entity, TileEntity tileEntity, IBackpack backpack) {  }
	
	@Override
	public void onPlacedInteract(EntityPlayer player, TileEntity tileEntity, IBackpack backpack) {
		if (player.worldObj.isRemote) return;
		new ContainerBackpack(player, backpack) {
			@Override public boolean canInteractWith(EntityPlayer player) {
				return (!player.isDead && !tileEntity.isInvalid() &&
						(player.worldObj.getTileEntity(tileEntity.getPos()) == tileEntity) &&
						(player.getDistanceSq(tileEntity.getPos()) <= 64));
			}
		}.open();
	}
	
	@Override
	public void onEquippedInteract(EntityPlayer player, EntityLivingBase target, IBackpack backpack) {
		if (player.worldObj.isRemote) return;
		new ContainerBackpack(player, backpack) {
			@Override public boolean canInteractWith(EntityPlayer player) {
				return BackpackHelper.canInteractWithEquippedBackpack(player, target);
			}
		}.open();
	}
	
	@Override
	public void onEquippedTick(EntityLivingBase entity, IBackpack backpack) {  }
	
	@Override
	public void onDeath(EntityLivingBase entity, IBackpack backpack) {
		onFaultyRemoval(entity, backpack);
	}
	
	@Override
	public void onFaultyRemoval(EntityLivingBase entity, IBackpack backpack) {
		if (!(backpack.getData() instanceof BackpackDataItems)) return;
		BackpackDataItems dataItems = (BackpackDataItems)backpack.getData();
		WorldUtils.dropStacksFromEntity(entity, dataItems.items, 4.0F);
	}
	
	@Override
	public void onBlockBreak(TileEntity tileEntity, IBackpack backpack) {
		if (!(backpack.getData() instanceof BackpackDataItems)) return;
		BackpackDataItems dataItems = (BackpackDataItems)backpack.getData();
		WorldUtils.dropStacksFromBlock(tileEntity, dataItems.items);
	}
	
	@Override
	public IBackpackData createBackpackData() {
		return new BackpackDataItems(WearableBackpacks.CONFIG.backpackRows.getValue() * 9);
	}
	
}
