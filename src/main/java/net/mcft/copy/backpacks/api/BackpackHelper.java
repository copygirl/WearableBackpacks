package net.mcft.copy.backpacks.api;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Support is almost non-existent when WearableBackpacks isn't installed.
    It's recommended to either have it be required, or only add backpacks
    when the mod is present (check with Loader.isModLoaded). */ 
public final class BackpackHelper {
	
	private BackpackHelper() {  }
	
	
	/** The maximum distance from which an equipped backpack can be opened. */
	public static double INTERACT_MAX_DISTANCE = 2;
	/** The maximum angle from which an equipped backpack can be opened. */
	public static double INTERACT_MAX_ANGLE = 90;
	
	/** Controlled by a WearableBackpacks config setting. Don't change this. */
	// TODO: Actually implement said config option!
	public static boolean equipAsChestArmor = true;
	
	
	/** Returns the entity's backpack capability, or null if the
	 *  entity either can't or currently doesn't have one equipped. */
	public static IBackpack getBackpack(Entity entity) {
		if (entity == null) return null;
		IBackpack backpack = entity.getCapability(IBackpack.CAPABILITY, null);
		return (((backpack != null) && (backpack.getStack() != null)) ? backpack : null);
	}
	
	/** Returns the tile entity's backpack capability. */
	public static IBackpack getBackpack(TileEntity entity) {
		return ((entity != null) ? entity.getCapability(IBackpack.CAPABILITY, null) : null);
	}
	
	/** Returns the backpack type of an item stack, or null if it isn't a backpack. */
	public static IBackpackType getBackpackType(ItemStack stack) {
		return ((stack != null) ? getBackpackType(stack.getItem()) : null);
	}
	/** Returns the backpack type of an item, or null if it doesn't implement IBackpackType. */
	public static IBackpackType getBackpackType(Item item) {
		return ((item instanceof IBackpackType) ? (IBackpackType)item : null);
	}
	
	/** Returns if the entity can equip a backpack right now.
	 *  Requires the entity to be able to wear backpacks, not currently have a backpack equipped, and
	 *  if {@link equipAsChestArmor} is true and the entity is a player, an empty chest armor slot. */
	public static boolean canEquipBackpack(EntityLivingBase entity) {
		return (BackpackRegistry.canEntityWearBackpacks(entity) && (getBackpack(entity) == null) &&
		        !(equipAsChestArmor && (entity instanceof EntityPlayer) &&
		          (entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST) != null)));
	}
	
	/** Sets the entity's equipped backpack and data. */
	public static void setEquippedBackpack(EntityLivingBase entity, ItemStack stack,
	                                       IBackpackData backpackData) {
		IBackpackType backpackType = getBackpackType(stack);
		if ((stack != null) && (backpackType == null))
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
		double angle = Math.toDegrees(Math.atan2(carrier.posY - player.posY, carrier.posX - player.posX));
		// Calculate difference between angle and the direction the carrier entity is looking.
		angle = ((angle - carrier.renderYawOffset + 90.0F) % 360 + 540) % 360;
		return ((distance <= INTERACT_MAX_DISTANCE) && (angle > (180 - INTERACT_MAX_ANGLE / 2)));
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
		if (!entity.worldObj.isRemote) {
			BackpackHelper.setEquippedBackpack(entity, stack, backpack.getData());
			backpack.setStack(null);
			backpack.setData(null);
		}
		return true;
	}
	
	/** Updates the lid ticks for some backpack properties.
	 *  Plays a sound when the backpack is being opened or closed. */
	@SideOnly(Side.CLIENT)
	public static void updateLidTicks(IBackpack properties, double x, double y, double z) {
		World world = Minecraft.getMinecraft().theWorld;
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
