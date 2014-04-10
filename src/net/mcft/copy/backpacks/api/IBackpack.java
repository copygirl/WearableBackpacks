package net.mcft.copy.backpacks.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

/** Signalizes that an {@link net.minecraft.item.Item Item} can be equipped as a backpack. */
public interface IBackpack {
	
	/** Called when an entity spawns with this backpack naturally. */
	public void onSpawnedWith(EntityLivingBase entity);
	
	/** Called before the backpack is equipped by a player. */
	public void onEquip(EntityPlayer player, TileEntity tileEntity);
	
	/** Called after the backpack is unequipped by a player. */
	public void onUnequip(EntityPlayer player, TileEntity tileEntity);
	
	/** Called when a player interacts with a placed-down backpack. <br>
	 *  The tile entity will be an instance of IBackpackProperties. */
	public void onPlacedInteract(EntityPlayer player, TileEntity target);
	
	/** Called when a player interacts with an equipped backpack. <br>
	 *  Also called when a player opens eir own backpack while it's
	 *  equipped, which is possible when a certain setting is enabled. <br>
	 *  Note: The player might be a "fake" player. */
	public void onEquippedInteract(EntityPlayer player, EntityLivingBase target);
	
	/** Called every game tick when the backpack is equipped,
	 *  regardless of where it's equipped (properties or armor). */
	public void onEquippedTick(EntityLivingBase entity);
	
	/** Called when the entity wearing this backpack dies. */
	public void onDeath(EntityLivingBase entity);
	
	/** Called when the backpack is removed by means it's not supposed to. <br>
	 *  The backpack is gone, but the backpack data is still there. */
	public void onFaultyRemoval(EntityLivingBase entity);
	
	/** Creates and returns a new backpack data object for this backpack. */
	public IBackpackData createBackpackData();
	
}
