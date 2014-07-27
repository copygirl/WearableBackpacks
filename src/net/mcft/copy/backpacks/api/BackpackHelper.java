package net.mcft.copy.backpacks.api;

import net.mcft.copy.core.util.ClientUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Support is almost non-existent when WearableBackpacks isn't installed.
    It's recommended to either have it be required, or only add backpacks
    when the mod is present (check with Loader.isModLoaded). */ 
public final class BackpackHelper {
	
	/** The maximum distance from which an equipped backpack can be opened. */
	public static double INTERACT_MAX_DISTANCE = 2;
	/** The maximum angle from which an equipped backpack can be opened. */
	public static double INTERACT_MAX_ANGLE = 90;
	
	/** Controlled by a WearableBackpacks config setting. Don't change this. */
	public static boolean equipAsChestArmor = true;
	
	private BackpackHelper() {  }
	
	/** Returns the entity's backpack property. <br>
	 *  Note that this will return null on entities which do not support backpacks
	 *  (see {@link BackpackRegistry}) and when WearableBackpacks is not installed. */
	public static IBackpackProperties getBackpackProperties(EntityLivingBase entity) {
		return (IBackpackProperties)entity.getExtendedProperties(IBackpackProperties.IDENTIFIER);
	}
	
	/** Returns an entity's equipped backpack, null if none. */
	public static ItemStack getEquippedBackpack(EntityLivingBase entity) {
		// Check for backpack in chest armor slot.
		ItemStack chestSlotBackpack = getBackpackInChestSlot(entity);
		if (chestSlotBackpack != null)
			return chestSlotBackpack;
		// Check for backpack in backpack properties.
		IBackpackProperties properties = getBackpackProperties(entity);
		return ((properties != null) ? properties.getBackpackStack() : null);
	}
	
	/** Returns if the entity can equip a backpack. <br>
	 *  Returns false if the chest armor slot is taken up or a backpack is already equipped. <br>
	 *  Also see {@link BackpackRegistry#canEntityWearBackpacks}. */
	public static boolean canEquipBackpack(EntityLivingBase entity) {
		if ((getBackpackProperties(entity) == null) ||
		    (getEquippedBackpack(entity) != null)) return false;
		else if (equipAsChestArmor && (entity instanceof EntityPlayer))
			return (getEquipmentInChestSlot(entity) == null);
		else return (getBackpackProperties(entity) != null);
	}
	
	/** Sets the entity's equipped backpack and data. */
	public static void setEquippedBackpack(EntityLivingBase entity, ItemStack backpack,
	                                       IBackpackData backpackData) {
		IBackpack backpackType = getBackpackType(backpack);
		if ((backpack != null) && (backpackType == null))
			throw new IllegalArgumentException("Backpack item isn't an IBackpack.");
		IBackpackProperties properties = getBackpackProperties(entity);
		// Set backpack
		if (equipAsChestArmor && (entity instanceof EntityPlayer)) {
			setEquipmentInChestSlot(entity, backpack);
			// Remove any backpack in the properties
			// in case the setting was changed.
			if (properties != null)
				properties.setBackpackStack(null);
		} else if (properties != null) {
			properties.setBackpackStack(backpack);
			// Remove any backpack in the chest slot
			// in case the setting was changed.
			if (getBackpackInChestSlot(entity) != null)
				setEquipmentInChestSlot(entity, null);
		}
		// Set backpack data and last backpack type.
		if (properties != null) {
			properties.setBackpackData(backpackData);
			properties.setLastBackpackType(backpackType);
		}
	}
	
	/** Returns the backpack data of an equipped backpack, null if none. */
	public static IBackpackData getEquippedBackpackData(EntityLivingBase entity) {
		IBackpackProperties properties = getBackpackProperties(entity);
		return ((properties != null) ? properties.getBackpackData() : null);
	}
	
	/** Returns the backpack type of an item stack, or null if it isn't a backpack. */
	public static IBackpack getBackpackType(ItemStack stack) {
		return (((stack != null) && (stack.getItem() instanceof IBackpack))
				? (IBackpack)stack.getItem() : null);
	}
	
	/** Checks if a player can open an entity's equipped backpack. <br>
	 *  Returns if the player stands close enough to and behind the carrier. <br>
	 *  Always returns true if player and carrier are the same entity. */
	public static boolean canInteractWithEquippedBackpack(EntityPlayer player, EntityLivingBase carrier) {
		double distance = player.getDistanceToEntity(carrier);
		// Calculate angle between player and carrier.
		double angle = Math.toDegrees(Math.atan2(carrier.posY - player.posY, carrier.posX - player.posX));
		// Calculate difference between angle and the
		// direction the carrier entity is looking.
		angle = ((angle - carrier.renderYawOffset + 90.0F) % 360 + 540) % 360;
		return (carrier.isEntityAlive() && (player == carrier) ||
		        ((distance <= INTERACT_MAX_DISTANCE) &&
		         (angle > (180 - INTERACT_MAX_ANGLE / 2))));
	}
	
	/** Equips a backpack from a tile entity, returns if successful. */
	public static <T extends TileEntity & IBackpackProperties> boolean equipBackpack(EntityLivingBase entity, T tileEntity) {
		if (!canEquipBackpack(entity)) return false;
		ItemStack stack = tileEntity.getBackpackStack();
		getBackpackType(stack).onEquip(entity, tileEntity);
		if (!entity.worldObj.isRemote) {
			BackpackHelper.setEquippedBackpack(entity, stack, tileEntity.getBackpackData());
			tileEntity.setBackpackStack(null);
			tileEntity.setBackpackData(null);
		}
		return true;
	}
	
	/** Updates the lid ticks for some backpack properties.
	 *  Plays a sound when the backpack is being opened or closed. */
	@SideOnly(Side.CLIENT)
	public static void updateLidTicks(IBackpackProperties properties, double x, double y, double z) {
		World world = ClientUtils.getLocalWorld();
		boolean usedByPlayer = (properties.getPlayersUsing() > 0);
		int prevLidTicks = properties.getLidTicks();
		IBackpack backpackType = getBackpackType(properties.getBackpackStack());
		int maxLidTicks = ((backpackType != null) ? backpackType.getLidMaxTicks() : 0);
		
		int lidTicks = Math.max(-1, Math.min(maxLidTicks, prevLidTicks + (usedByPlayer ? +1 : -1)));
		properties.setLidTicks(lidTicks);
		
		String sound = Block.soundTypeSnow.getStepResourcePath();
		// Play sound when backpack is being opened.
		if ((lidTicks > 0) && (prevLidTicks <= 0))
			world.playSound(x, y, z, sound, 1.0F, 0.6F, false);
		// Play sound when backpack is being closed.
		if ((lidTicks < (maxLidTicks / 5)) && (prevLidTicks >= (maxLidTicks / 5)))
			world.playSound(x, y, z, sound, 0.8F, 0.4F, false);
	}
	
	// Helper functions
	
	private static ItemStack getEquipmentInChestSlot(EntityLivingBase entity) {
		return entity.getEquipmentInSlot(3); // EquipmentSlot.CHEST
	}
	private static void setEquipmentInChestSlot(EntityLivingBase entity, ItemStack stack) {
		entity.setCurrentItemOrArmor(3, stack); // EquipmentSlot.CHEST
	}
	
	private static ItemStack getBackpackInChestSlot(EntityLivingBase entity) {
		ItemStack equipment = getEquipmentInChestSlot(entity);
		return ((getBackpackType(equipment) != null) ? equipment : null);
	}
	
}
