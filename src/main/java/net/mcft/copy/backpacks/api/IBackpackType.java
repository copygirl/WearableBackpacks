package net.mcft.copy.backpacks.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

/** Signalizes that an {@link net.minecraft.item.Item Item} can be equipped as a backpack. */
public interface IBackpackType {
	
	/** Called when an entity spawns with this backpack naturally. */
	void onSpawnedWith(EntityLivingBase entity, IBackpack capability);
	
	/** Called before the backpack is equipped by an entity.
	 *  <p>
	 *  Note that on the client side, there's no certainty whether
	 *  the backpack is actually going to be equipped successfully. */
	void onEquip(EntityLivingBase entity, TileEntity tileEntity, IBackpack backpack);
	
	/** Called after the backpack is unequipped by an entity.
	 *  <p>
	 *  Also called when the entity dies and the backpack is placed down as a block automatically,
	 *  which can be tested using {@link EntityLivingBase#isEntityAlive() entity.isEntityAlive()}.
	 *  This behavior is controlled by the "general.dropAsBlockOnDeath" config setting.
	 *  <p>
	 *  Note that on the client side, there's no certainty whether
	 *  the backpack is actually going to be unequipped successfully. */
	void onUnequip(EntityLivingBase entity, TileEntity tileEntity, IBackpack backpack);
	
	/** Called when a player interacts with a placed-down backpack. */
	void onPlacedInteract(EntityPlayer player, TileEntity tileEntity, IBackpack backpack);
	
	/** Called when a player interacts with an equipped backpack.
	 *  <p>
	 *  Also called when a player opens their own backpack while it's equipped,
	 *  which is enabled with the "general.enableSelfInteraction" config setting.
	 *  <p>
	 *  Note that the player might be a "fake" player
	 *  and this is also called on the client side. */
	void onEquippedInteract(EntityPlayer player, EntityLivingBase target, IBackpack backpack);
	
	/** Called every game tick when the backpack is equipped, regardless of where. */
	void onEquippedTick(EntityLivingBase entity, IBackpack backpack);
	
	/** Called before the entity wearing this backpack dies and drops the backpack item.
	 *  <p>
	 *  If either the "general.dropAsBlockOnDeath" config setting or "keepInventory"
	 *  gamerule are enabled, this method won't be called. */
	void onDeath(EntityLivingBase entity, IBackpack backpack);
	
	/** Called when the backpack is removed from the
	 *  chestplate slot by means it's not supposed to. */
	void onFaultyRemoval(EntityLivingBase entity, IBackpack backpack);
	
	/** Called when this backpack is broken when placed down. */
	void onBlockBreak(TileEntity tileEntity, IBackpack backpack);
	
	/** Creates and returns a new backpack data object for this backpack. */
	IBackpackData createBackpackData();
	
	
	/** Returns the number of ticks the backpack's lid takes to fully open. */
	default int getLidMaxTicks() { return 5; }
	
	/** Returns the lid angle, from current ticks the backpack is open.
	 *  Used for rendering the backpack model with an animated lid. */
	default float getLidAngle(int lidTicks, int prevLidTicks, float partialTicks) {
		float progress = lidTicks + (lidTicks - prevLidTicks) * partialTicks;
		progress = Math.max(0, Math.min(getLidMaxTicks(), progress)) / getLidMaxTicks();
		return (1.0F - (float)Math.pow(1.0F - progress, 2)) * 45;
	}
	
}
