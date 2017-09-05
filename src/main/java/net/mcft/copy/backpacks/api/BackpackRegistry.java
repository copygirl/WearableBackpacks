package net.mcft.copy.backpacks.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

import net.mcft.copy.backpacks.item.ItemBackpack;

public final class BackpackRegistry {
	
	private BackpackRegistry() {  }
	
	public static final Map<Class<? extends EntityLivingBase>, List<Entry>> entities = new HashMap<>();
	
	/**
	 * Registers an entity as possible backpack carrier, meaning they'll get
	 * constructed with backpack properties, along with backpack spawn entry.
	 * <p>
	 * Must be called in pre-initialization phase (or before).
	 * 
	 * @param id          String uniquely identifying this entry.
	 * @param entityClass The entity to register to spawn with this backpack.
	 * @param backpack    Backpack item to spawn on the entity (must implement IBackpackType).
	 * @param chance      Chance in 1 out of X, so for example 100 = 1% and 1000 = 0.1%.
	 * @param lootTable   Loot table for the backpack when spawned on this mob (if any).
	 **/
	public static void registerBackpackEntity(Class<? extends EntityLivingBase> entityClass,
	                                          String id, ItemBackpack backpack, int chance, String lootTable) {
		
		if (entityClass == null) throw new NullPointerException("entityClass must not be null");
		if (id == null) throw new NullPointerException("id must not be null");
		if (backpack == null) throw new NullPointerException("backpack must not be null");
		if (!(backpack instanceof IBackpackType)) throw new IllegalArgumentException("backpack must be an IBackpackType");
		if (chance <= 0) throw new IllegalArgumentException("chance must be positive");
		if (Loader.instance().getLoaderState().compareTo(LoaderState.PREINITIALIZATION) > 0)
			throw new IllegalStateException("Must be called during (or before) pre-initialization phase.");
		
		List<Entry> list = entities.get(entityClass);
		if (list == null) entities.put(entityClass, list = new ArrayList<>());
		list.add(new Entry(id, backpack, chance, lootTable));
		
	}
	
	/** Returns if the specified entity should be able to wear backpacks.
	 *  This affects whether the entity will be constructed with an IBackpack capability. */
	public static boolean canEntityWearBackpacks(Entity entity) {
		return (entity instanceof EntityPlayer) ? true
			: entities.containsKey(entity.getClass());
	}
	
	
	public static final class Entry {
		
		public final String id;
		public final ItemBackpack backpack;
		public final int chance;
		public final String lootTable;
		// TODO: This also needs positional data and color.
		
		public Entry(String id, ItemBackpack backpack, int chance, String lootTable) {
			this.id        = id;
			this.backpack  = backpack;
			this.chance    = chance;
			this.lootTable = lootTable;
		}
		
	}
	
}
