package net.mcft.copy.backpacks.api;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

public final class BackpackRegistry {
	
	private BackpackRegistry() {  }
	
	public static final Map<Class<? extends EntityLivingBase>, Map<Item, Double>> entities =
			new HashMap<Class<? extends EntityLivingBase>, Map<Item, Double>>();
	
	/** Registers an entity as possible backpack carrier, meaning they'll get
	 *  constructed with backpack properties. Additionally controls how backpacks
	 *  are spawned on entities. Only registers the exact class, subclasses have
	 *  to be registered separately. */
	public static void registerBackpackEntity(Class<? extends EntityLivingBase> entityClass,
	                                          Object... backpackChancePairs) {
		Map<Item, Double> entry = entities.get(entityClass);
		if (entry == null)
			entities.put(entityClass, (entry = new HashMap<Item, Double>()));
		if ((backpackChancePairs.length % 2) > 0)
			throw new IllegalArgumentException("Number of backpack chance pairs is not even");
		for (int i = 0; i < backpackChancePairs.length; i += 2) {
			if (!(backpackChancePairs[i] instanceof Item))
				throw new IllegalArgumentException("First argument in backpack chance pair is not an Item");
			if (!(backpackChancePairs[i] instanceof IBackpack))
				throw new IllegalArgumentException("First argument in backpack chance pair is not an IBackpack");
			if (!(backpackChancePairs[i + 1] instanceof Double))
				throw new IllegalArgumentException("Second argument in backpack chance pair is not a Double");
			Item item = (Item)backpackChancePairs[i];
			double chance = (Double)backpackChancePairs[i + 1];
			if (chance > 0) entry.put(item, chance);
			else entry.remove(item);
		}
	}
	
	/** Returns if the entity can wear backpacks. */
	public static boolean canEntityWearBackpacks(Entity entity) {
		return ((entity instanceof EntityPlayer) ? true : entities.containsKey(entity.getClass()));
	}
	
}
