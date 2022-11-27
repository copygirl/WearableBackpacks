package net.mcft.copy.backpacks.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

/** Signalizes that an {@link net.minecraft.item.Item Item} can be equipped as a backpack. */
public interface IBackpackType {
	
	/** Called when an entity spawns with this backpack naturally. */
	void onSpawnedWith(EntityLivingBase entity, IBackpack capability, String lootTable);
	
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
	
	/** Called before backpack is broken and should drop as the backpack item.
	 * <br>
	 *  Ideally you would make all the items in the backpack drop on the ground, or add them to a container of some kind
	 *  that drops on the ground.
	 *  <br>
	 *  (<b>TheUnderTaker11 note</b>) As far as I can tell, it should be assumed the backpack item itself will be dropped elsewhere.
	 *  The contents of the bag, not the bag itself, are the only things dropped from this in current implementation.
	 */
	void onBackpackDeath(EntityLivingBase entity, IBackpack backpack);
	
	/** Called before the Entity dies, ideally you should make 1 of 2 things happen. <br>
	 * 1. All backpack contents are added to the {@link LivingDropsEvent#getDrops()} collection.
	 * <br>
	 * or 2. All backpack contents should be added to an item container, add that container to {@link LivingDropsEvent#getDrops()} collection.
	 * <p><b>If you do not add the backpack stack itself to the {@link LivingDropsEvent#getDrops()}, it will simply not be dropped! </b>
	 *  <p>
	 *  If either the "general.dropAsBlockOnDeath" config setting or "keepInventory"
	 *  gamerule are enabled, this method won't be called at all. 
	 */
	void onEntityWearerDeath(LivingDropsEvent playerDropsEvent, IBackpack backpack);
	
	/** Called when the backpack breaks while being equipped. */
	void onEquippedBroken(EntityLivingBase entity, IBackpack backpack);
	
	/** Called when the backpack is removed from the
	 *  chestplate slot by means it's not supposed to. */
	void onFaultyRemoval(EntityLivingBase entity, IBackpack backpack);
	
	/** Called when this backpack is broken when placed down. */
	void onBlockBreak(TileEntity tileEntity, IBackpack backpack);
	
	/** Creates and returns a new backpack data object for this backpack. */
	IBackpackData createBackpackData(ItemStack stack);
	
	
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
