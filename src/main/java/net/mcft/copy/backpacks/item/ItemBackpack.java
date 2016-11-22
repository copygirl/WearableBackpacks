package net.mcft.copy.backpacks.item;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
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
import net.mcft.copy.backpacks.block.BlockBackpack;
import net.mcft.copy.backpacks.container.ContainerBackpack;
import net.mcft.copy.backpacks.item.recipe.IDyeableItem;
import net.mcft.copy.backpacks.misc.BackpackDataItems;
import net.mcft.copy.backpacks.misc.util.WorldUtils;

public class ItemBackpack extends ItemBlock implements IBackpackType, IDyeableItem {
	
	public static final int DEFAULT_COLOR = 0xA06540;
	
	public ItemBackpack(BlockBackpack block) {
		super(block);
		setMaxStackSize(1);
		// TODO: Implement item data / protection / enchantments.
	}
	
	@Override
	public boolean canDye(ItemStack stack) { return true; }
	
	// Item properties
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		// TODO: Move tooltip adding code into helper class. DRY!
		boolean enableHelpTooltips = true;
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
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn,
	                                  World worldIn, BlockPos pos,
	                                  EnumHand hand, EnumFacing facing,
	                                  float hitX, float hitY, float hitZ) {
		
		// If block clicked is replacable, (like for example
		// tall grass or snow), check the block below instead.
		IBlockState state = worldIn.getBlockState(pos);
		if (state.getMaterial().isReplaceable()) {
			pos = pos.offset(EnumFacing.DOWN);
			state = worldIn.getBlockState(pos);
			facing = EnumFacing.UP;
		// If block isn't replacable, make sure the top side is clicked.
		} else if (facing != EnumFacing.UP) return EnumActionResult.FAIL;
		// Check if top of block is solid.
		if (!state.isSideSolid(worldIn, pos, EnumFacing.UP))
			return EnumActionResult.FAIL;
		
		EnumActionResult result = super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
		if (result != EnumActionResult.SUCCESS) return result;
		
		TileEntity tileEntity = worldIn.getTileEntity(pos.offset(EnumFacing.UP));
		if (tileEntity == null) return EnumActionResult.SUCCESS;
		IBackpack tileEntityBackpack = BackpackHelper.getBackpack(tileEntity);
		if (tileEntityBackpack == null) return EnumActionResult.SUCCESS;
		
		IBackpack backpack = BackpackHelper.getBackpack(playerIn);
		boolean isEquipped = ((backpack != null) && (backpack.getStack() == stack));
		
		stack = ItemStack.copyItemStack(stack);
		stack.stackSize = 1;
		tileEntityBackpack.setStack(stack);
		
		// If the backpack was equipped on the player, transfer data and unequip.
		if (isEquipped) {
			
			IBackpackType type = backpack.getType();
			IBackpackData data = backpack.getData();
			if ((data == null) && !worldIn.isRemote) {
				WearableBackpacks.LOG.error("Backpack data was null when placing down backpack");
				data = type.createBackpackData();
			}
			
			tileEntityBackpack.setData(data);
			
			if (!worldIn.isRemote)
				BackpackHelper.setEquippedBackpack(playerIn, null, null);
			
			type.onUnequip(playerIn, tileEntity, tileEntityBackpack);
		
		// Otherwise create a fresh backpack data on the server.
		} else if (!worldIn.isRemote) tileEntityBackpack.setData(
			tileEntityBackpack.getType().createBackpackData());
		
		return EnumActionResult.SUCCESS;
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
		return new BackpackDataItems(36); // TODO: Get configurable backpack size.
	}
	
	@Override
	public int getLidMaxTicks() { return 5; }
	
	@Override
	public float getLidAngle(int prevLidTicks, int lidTicks, float partialTicks) {
		float progress = lidTicks + (lidTicks - prevLidTicks) * partialTicks;
		progress = Math.max(0, Math.min(getLidMaxTicks(), progress)) / getLidMaxTicks();
		return (1.0F - (float)Math.pow(1.0F - progress, 2)) * 45;
	}
	
}
