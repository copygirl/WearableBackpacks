package net.mcft.copy.backpacks.api;

import net.minecraft.util.EnumHand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Support is almost non-existent when WearableBackpacks isn't installed.
    It's recommended to either have it be required, or only add backpacks
    when the mod is present (check with <code>Loader.isModLoaded</code>). */ 
public final class BackpackHelper {
	
	private BackpackHelper() {  }
	
	
	/** The maximum distance from which an equipped backpack can be opened. */
	public static double INTERACT_MAX_DISTANCE = 1.8;
	/** The maximum angle from which an equipped backpack can be opened. */
	public static double INTERACT_MAX_ANGLE = 110;
	
	/** Controlled by a WearableBackpacks config setting. Don't change this, please. */
	public static boolean equipAsChestArmor = true;
	
	public static Logger LOG = LogManager.getLogger("wearablebackpacks:api");
	
	
	/** Returns the entity's backpack capability, or null if the
	 *  entity either can't or currently doesn't have one equipped. */
	public static IBackpack getBackpack(Entity entity) {
		if (entity == null) return null;
		IBackpack backpack = entity.getCapability(IBackpack.CAPABILITY, null);
		return ((backpack != null) && !backpack.getStack().isEmpty()) ? backpack : null;
	}
	
	/** Returns the tile entity's backpack capability. */
	public static IBackpack getBackpack(TileEntity entity)
		{ return (entity != null) ? entity.getCapability(IBackpack.CAPABILITY, null) : null; }
	
	/** Returns the backpack type of an item stack, or null if it isn't a backpack. */
	public static IBackpackType getBackpackType(ItemStack stack)
		{ return !stack.isEmpty() ? getBackpackType(stack.getItem()) : null; }
	/** Returns the backpack type of an item, or null if it doesn't implement IBackpackType. */
	public static IBackpackType getBackpackType(Item item)
		{ return (item instanceof IBackpackType) ? (IBackpackType)item : null; }
	
	/** Returns if the entity can equip a backpack right now.
	 *  Requires the entity to be able to wear backpacks, not currently have a backpack equipped, and
	 *  if {@link equipAsChestArmor} is true and the entity is a player, an empty chest armor slot. */
	public static boolean canEquipBackpack(EntityLivingBase entity) {
		return (entity.getCapability(IBackpack.CAPABILITY, null) != null) // Has backpack capability.
			&& (getBackpack(entity) == null)                              // Doesn't currently have backpack equipped.
			&& !(equipAsChestArmor && (entity instanceof EntityPlayer)    // Isn't wearing a chestplate while equipAsChestArmor is on.
				&& !entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty());
		// FIXME: How does this work with non-player entities? Do / should they always wear backpacks as armor or what?
	}
	
	/** Sets the entity's equipped backpack and data. */
	public static void setEquippedBackpack(EntityLivingBase entity, ItemStack stack,
	                                       IBackpackData backpackData) {
		IBackpackType backpackType = getBackpackType(stack);
		if (!stack.isEmpty() && (backpackType == null))
			throw new IllegalArgumentException("Backpack item isn't an IBackpackType.");
		
		IBackpack backpack = entity.getCapability(IBackpack.CAPABILITY, null);
		backpack.setStack(stack);
		backpack.setData(backpackData);
	}
	
	/** Checks if a player can open an entity's equipped backpack.
	 *  Returns if the player stands close enough to and behind the carrier.
	 *  Always returns true if player and carrier are the same entity. */
	public static boolean canInteractWithEquippedBackpack(EntityPlayer player, EntityLivingBase carrier) {
		IBackpack backpack = getBackpack(carrier);
		if ((backpack == null) || !player.isEntityAlive() || !carrier.isEntityAlive()) return false;
		if (player == carrier) return true;
		
		double distance = player.getDistanceToEntity(carrier);
		// Calculate angle between player and carrier.
		double angle = Math.toDegrees(Math.atan2(carrier.posZ - player.posZ, carrier.posX - player.posX));
		// Calculate difference between angle and the direction the carrier entity is looking.
		angle = ((angle - carrier.renderYawOffset - 90) % 360 + 540) % 360 - 180;
		return ((distance <= INTERACT_MAX_DISTANCE) && (Math.abs(angle) < INTERACT_MAX_ANGLE / 2));
	}
	
	/** Equips a backpack from a tile entity, returns if successful. */
	public static boolean equipBackpack(EntityLivingBase entity, TileEntity tileEntity) {
		if ((tileEntity == null) || !canEquipBackpack(entity)) return false;
		IBackpack backpack = tileEntity.getCapability(IBackpack.CAPABILITY, null);
		if (backpack == null) return false;
		ItemStack stack = backpack.getStack();
		IBackpackType type = getBackpackType(stack);
		if (type == null) return false;
		type.onEquip(entity, tileEntity, backpack);
		if (!entity.world.isRemote) {
			BackpackHelper.setEquippedBackpack(entity, stack, backpack.getData());
			backpack.setStack(ItemStack.EMPTY);
			backpack.setData(null);
		}
		return true;
	}
	
	/** Attempts to place down a backpack, unequipping it
	 *  if the specified entity is currently wearing it. */
	public static boolean placeBackpack(World world, BlockPos pos, ItemStack stack,
	                                    EntityLivingBase entity, boolean ignoreEntities) {
		
		EntityPlayer player = ((entity instanceof EntityPlayer) ? (EntityPlayer)entity : null);
		if ((player != null) && !player.canPlayerEdit(pos, EnumFacing.UP, stack))
			return false;
		// TODO: Should permission things be handled outside the API method?
		// TODO: For mobs, use mob griefing gamerule?
		
		Item item = stack.getItem();
		// Would use this instead, but gotta avoid depending on the rest of WearableBackpacks.
		//Block block = MiscUtils.getBlockFromItem(item);
		Block block = Block.REGISTRY.getObject(item.getRegistryName());
		if (ignoreEntities ? !block.canPlaceBlockAt(world, pos)
		                   : !world.mayPlace(block, pos, false, EnumFacing.UP, null))
			return false;
		
		// Actually go ahead and try to set the block in the world.
		IBlockState state = block.getStateForPlacement(
			world, pos, EnumFacing.UP, 0.5F, 0.5F, 0.5F,
			item.getMetadata(stack.getMetadata()), player, EnumHand.MAIN_HAND);
		if (!world.setBlockState(pos, state, 3) ||
		    (world.getBlockState(pos).getBlock() != block)) return false;
		block.onBlockPlacedBy(world, pos, state, entity, stack);
		
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.5;
		double z = pos.getZ() + 0.5;
		SoundType sound = block.getSoundType(state, world, pos, entity);
		world.playSound(x, y, z, sound.getPlaceSound(), SoundCategory.BLOCKS,
		                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F, false);
		
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity == null) return true;
		IBackpack placedBackpack = BackpackHelper.getBackpack(tileEntity);
		if (placedBackpack == null) return true;
		
		IBackpack carrierBackpack = BackpackHelper.getBackpack(entity);
		boolean isEquipped = ((carrierBackpack != null) && (carrierBackpack.getStack() == stack));
		
		ItemStack stackOrig = stack;
		// Create a copy of the stack with stackSize set to 1 and transfer it.
		stack = stack.copy();
		stack.setCount(1);
		placedBackpack.setStack(stack);
		
		// If the carrier had the backpack equipped, transfer data and unequip.
		if (isEquipped) {
			
			IBackpackType type = carrierBackpack.getType();
			IBackpackData data = carrierBackpack.getData();
			if ((data == null) && !world.isRemote) {
				LOG.error("Backpack data was null when placing down equipped backpack");
				data = type.createBackpackData(stack);
			}
			
			placedBackpack.setData(data);
			
			if (!world.isRemote)
				BackpackHelper.setEquippedBackpack(entity, ItemStack.EMPTY, null);
			
			type.onUnequip(entity, tileEntity, placedBackpack);
		
		// Otherwise create a fresh backpack data on the server.
		} else if (!world.isRemote) placedBackpack.setData(
			placedBackpack.getType().createBackpackData(stack));
		
		// We only shrink the original stack here instead of earlier
		// as its information is still needed for other checks, and
		// shrinking it from 1 to 0 would effectively empty the stack.
		stackOrig.shrink(1);
		return true;
		
	}
	
	/** Updates the lid ticks for some backpack properties.
	 *  Plays a sound when the backpack is being opened or closed. */
	@SideOnly(Side.CLIENT)
	public static void updateLidTicks(IBackpack properties, double x, double y, double z) {
		World world = Minecraft.getMinecraft().world;
		boolean usedByPlayer = (properties.getPlayersUsing() > 0);
		int prevLidTicks = properties.getLidTicks();
		IBackpackType backpackType = getBackpackType(properties.getStack());
		int maxLidTicks = ((backpackType != null) ? backpackType.getLidMaxTicks() : 0);
		
		int lidTicks = Math.max(-1, Math.min(maxLidTicks, prevLidTicks + (usedByPlayer ? +1 : -1)));
		properties.setLidTicks(lidTicks);
		
		// Play sound when backpack is being opened.
		if ((lidTicks > 0) && (prevLidTicks <= 0))
			world.playSound(x, y, z, SoundEvents.BLOCK_SNOW_BREAK, SoundCategory.PLAYERS, 1.0F, 0.6F, false);
		// Play sound when backpack is being closed.
		if ((lidTicks < (maxLidTicks / 5)) && (prevLidTicks >= (maxLidTicks / 5)))
			world.playSound(x, y, z, SoundEvents.BLOCK_SNOW_BREAK, SoundCategory.PLAYERS, 0.8F, 0.4F, false);
	}
	
}
