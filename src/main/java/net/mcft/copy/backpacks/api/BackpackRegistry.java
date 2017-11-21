package net.mcft.copy.backpacks.api;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class BackpackRegistry {
	
	private BackpackRegistry() {  }
	
	/** Registers an entity as possible backpack carrier, meaning
	 *  they'll get constructed with an IBackpack capability.
	 *  Must be called in pre-initialization phase (or before). */
	public static void registerEntity(String entityID, RenderOptions renderOptions) {
		if (entityID == null) throw new NullPointerException("entityID must not be null");
		if (getDefaultEntityEntry(entityID) != null)
			throw new IllegalArgumentException("entityID '" + entityID + "' has already been registered");
		if (Loader.instance().getLoaderState().compareTo(LoaderState.PREINITIALIZATION) > 0)
			throw new IllegalStateException("Must be called during (or before) pre-initialization phase.");
		_defaultEntities.add(new BackpackEntityEntry(entityID, renderOptions, new ArrayList<>(), true));
	}
	
	/** Registers a backpack to randomly spawn on the specified entity.
	 *  Must be called after registerEntity, in pre-initialization phase (or before).
	 * 
	 * @param entityID   The entity to register to spawn with this backpack.
	 * 
	 * @param entryID    String uniquely identifying this entry for this entity.
	 *                   For example "wearblebackpacks:default".
	 * @param backpack   Backpack item ID to spawn on the entity.
	 *                   For example "wearablebackpacks:backpack".
	 * @param chance     Chance in 1 out of X. For example 100 = 1% and 1000 = 0.1%.
	 * @param lootTable  Loot table for the backpack when spawned on this mob (if any).
	 * @param colorRange A range of colors to spawn the backpack with, or null if default. 
	 **/
	public static void registerBackpack(String entityID,
	                                    String entryID, String backpack, int chance,
	                                    String lootTable, ColorRange colorRange) {
		if (entityID == null) throw new NullPointerException("entityID must not be null");
		if (entryID == null) throw new NullPointerException("entryID must not be null");
		BackpackEntityEntry entityEntry = getDefaultEntityEntry(entityID);
		if (entityEntry == null) new IllegalStateException("entityID '" + entityID + "' has not been registered yet");
		if (entityEntry._backpackEntries.stream().anyMatch(e -> e.id.equals(entryID)))
			throw new IllegalArgumentException("entryID '" + entryID + "' has already been used for entityID '" + entityID + "'");
		if (Loader.instance().getLoaderState().compareTo(LoaderState.PREINITIALIZATION) > 0)
			throw new IllegalStateException("Must be called during (or before) pre-initialization phase.");
		entityEntry._backpackEntries.add(new BackpackEntry(entryID, backpack, chance, lootTable, colorRange, true));
	}
	
	/** Returns if the specified entity should be able to wear backpacks.
	 *  This affects whether the entity will be constructed with an IBackpack capability. */
	public static boolean canEntityWearBackpacks(Entity entity) {
		return (entity != null) && EntityLivingBase.class.isAssignableFrom(entity.getClass())
			&& (getEntityEntry(entity.getClass().asSubclass(EntityLivingBase.class)) != null);
	}
	
	
	// Internal / semi-internal stuff
	
	private static final List<BackpackEntityEntry> _defaultEntities = new ArrayList<>();
	private static final List<BackpackEntityEntry> _entities = new ArrayList<>();
	private static final Map<String, Optional<Class<? extends EntityLivingBase>>> _entityClassLookupCache = new HashMap<>();
	private static Map<Class<? extends EntityLivingBase>, BackpackEntityEntry> _entityEntryLookupCache = null;
	
	public static List<BackpackEntityEntry> getEntityEntries()
		{ return Collections.unmodifiableList(_entities); }
	public static List<BackpackEntityEntry> getDefaultEntityEntries()
		{ return Collections.unmodifiableList(_defaultEntities); }
	
	public static BackpackEntityEntry getEntityEntry(String entityID)
		{ return _entities.stream().filter(e -> e.entityID.equals(entityID)).findAny().orElse(null); }
	public static BackpackEntityEntry getDefaultEntityEntry(String entityID)
		{ return _defaultEntities.stream().filter(e -> e.entityID.equals(entityID)).findAny().orElse(null); }
	
	public static BackpackEntityEntry getEntityEntry(Class<? extends EntityLivingBase> entityClass) {
		if (EntityPlayer.class.isAssignableFrom(entityClass))
			return BackpackEntityEntry.PLAYER;
		if (_entityEntryLookupCache == null)
			_entityEntryLookupCache = ForgeRegistries.ENTITIES.getEntries().stream()
				.map(e -> new AbstractMap.SimpleEntry<>(e.getValue().getEntityClass(), getEntityEntry(e.getKey().toString())))
				.filter(e -> EntityLivingBase.class.isAssignableFrom(e.getKey()) && (e.getValue() != null))
				.collect(Collectors.toMap(e -> e.getKey().asSubclass(EntityLivingBase.class), Map.Entry::getValue));
		return _entityEntryLookupCache.get(entityClass);
	}
	
	public static void updateEntityEntries(List<BackpackEntityEntry> value) {
		mergeEntityEntriesWithDefault(_entities, value);
		_entityEntryLookupCache = null;
	}
	public static List<BackpackEntityEntry> mergeEntityEntriesWithDefault(List<BackpackEntityEntry> value)
		{ return mergeEntityEntriesWithDefault(new ArrayList<>(), value); }
	public static List<BackpackEntityEntry> mergeEntityEntriesWithDefault(
			List<BackpackEntityEntry> dest, List<BackpackEntityEntry> value) {
		dest.clear();
		_defaultEntities.stream().map(BackpackEntityEntry::new).forEach(dest::add);
		
		for (BackpackEntityEntry entityEntry : value) {
			BackpackEntityEntry defaultEntityEntry = dest.stream()
				.filter(e -> e.entityID.equals(entityEntry.entityID))
				.findAny().orElse(null);
			if (defaultEntityEntry != null) {
				for (BackpackEntry backpackEntry : entityEntry._backpackEntries) {
					int index = IntStream.range(0, defaultEntityEntry._backpackEntries.size())
						.filter(i -> defaultEntityEntry._backpackEntries.get(i).id.equals(backpackEntry.id))
						.findFirst().orElse(-1);
					if (index >= 0) {
						BackpackEntry e = defaultEntityEntry._backpackEntries.get(index);
						defaultEntityEntry._backpackEntries.set(index, new BackpackEntry(
							e.id, e.backpack, backpackEntry.chance, e.lootTable, e.colorRange, e.isDefault));
					} else defaultEntityEntry._backpackEntries.add(backpackEntry);
				}
			} else dest.add(entityEntry);
		}
		
		return dest;
	}
	
	public static List<BackpackEntry> getBackpackEntries(Class<? extends EntityLivingBase> entityClass) {
		if (entityClass == null) throw new NullPointerException("entityClass must not be null");
		BackpackEntityEntry entityEntry = getEntityEntry(entityClass);
		return (entityEntry != null) ? entityEntry.getEntries() : Collections.emptyList();
	}
	
	
	public static final class BackpackEntityEntry {
		
		public static final BackpackEntityEntry PLAYER = new BackpackEntityEntry(
			"<player>", RenderOptions.DEFAULT, Collections.emptyList(), true);
		
		public final String entityID;
		public final RenderOptions renderOptions;
		public final boolean isDefault;
		
		private final List<BackpackEntry> _backpackEntries;
		
		public BackpackEntityEntry(String entityID, RenderOptions renderOptions,
		                           List<BackpackEntry> entries, boolean isDefault) {
			if (entityID == null) throw new NullPointerException("entityID must not be null");
			if (renderOptions == null) throw new NullPointerException("renderOptions must not be null");
			if (entries == null) throw new NullPointerException("entries must not be null");
			this.entityID      = entityID;
			this.renderOptions = renderOptions;
			this.isDefault     = isDefault;
			_backpackEntries   = entries;
		}
		public BackpackEntityEntry(BackpackEntityEntry value) {
			this(value.entityID, value.renderOptions,
			     new ArrayList<>(value._backpackEntries), value.isDefault);
		}
		
		public Class<? extends EntityLivingBase> getEntityClass() {
			return _entityClassLookupCache.computeIfAbsent(entityID, id ->
				Optional.ofNullable(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id)))
					.map(EntityEntry::getEntityClass)
					.filter(EntityLivingBase.class::isAssignableFrom)
					.map(c -> c.asSubclass(EntityLivingBase.class))
				).orElse(null);
		}
		
		public List<BackpackEntry> getEntries()
			{ return Collections.unmodifiableList(_backpackEntries); }
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof BackpackEntityEntry)) return false;
			BackpackEntityEntry other = (BackpackEntityEntry)obj;
			return Objects.equals(other.entityID, entityID)
				&& other.renderOptions.equals(renderOptions)
				&& other._backpackEntries.equals(_backpackEntries);
		}
		
	}
	
	public static final class RenderOptions {
		
		public static final RenderOptions DEFAULT = new RenderOptions(0, 2.5, 0.0, 0.8);
		
		public final double y, z;
		public final double rotate, scale;
		
		public RenderOptions(double y, double z, double rotate, double scale)
			{ this.y = y; this.z = z; this.rotate = rotate; this.scale = scale; }
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof RenderOptions)) return false;
			RenderOptions other = (RenderOptions)obj;
			return (other.y == y) && (other.z == z) &&
			       (other.rotate == rotate) && (other.scale == scale);
		}
		
	}
	
	public static final class BackpackEntry {
		
		public static final BackpackEntry DEFAULT = new BackpackEntry(
			null, "wearablebackpacks:backpack", 1000, "wearablebackpacks:backpack/default", null, false);
		
		public final String id;
		public final String backpack;
		public final int chance;
		public final String lootTable;
		public final ColorRange colorRange; // TODO: Check if color is supported?
		public final boolean isDefault;
		
		public BackpackEntry(String id, String backpack, int chance,
		                     String lootTable, ColorRange colorRange, boolean isDefault) {
			if (backpack == null) throw new NullPointerException("backpack must not be null");
			if (chance < 0) throw new NullPointerException("chance must not be negative");
			
			this.id         = id;
			this.backpack   = backpack;
			this.chance     = chance;
			this.lootTable  = lootTable;
			this.colorRange = colorRange;
			this.isDefault  = isDefault;
		}
		
		@SuppressWarnings("unchecked")
		public <T extends Item & IBackpackType> T getBackpackItem() {
			Item item = Item.getByNameOrId(backpack);
			return (item instanceof IBackpackType) ? (T)item : null;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof BackpackEntry)) return false;
			BackpackEntry other = (BackpackEntry)obj;
			return Objects.equals(other.id, id)
				&& Objects.equals(other.backpack, backpack)
				&& (other.chance == chance)
				&& Objects.equals(other.lootTable, lootTable)
				&& Objects.equals(other.colorRange, colorRange);
		}
		
	}
	
	public static final class ColorRange {
		
		public static final ColorRange DEFAULT = new ColorRange(0x202020, 0xD0D0D0);
		private static final Random rnd = new Random();
		
		public final int min, max;
		
		public ColorRange(int min, int max)
			{ this.min = min; this.max = max; }
		
		public boolean isValid() {
			int minR = min >> 16 & 0xFF;
			int minG = min >> 8 & 0xFF;
			int minB = min & 0xFF;
			int maxR = max >> 16 & 0xFF;
			int maxG = max >> 8 & 0xFF;
			int maxB = max & 0xFF;
			return (minR <= maxR) && (minG <= maxG) && (minB <= maxB);
		}
		
		public int getRandom() {
			int minR = min >> 16 & 0xFF;
			int minG = min >> 8 & 0xFF;
			int minB = min & 0xFF;
			int maxR = max >> 16 & 0xFF;
			int maxG = max >> 8 & 0xFF;
			int maxB = max & 0xFF;
			int r = (maxR - minR + 1 >= 0) ? minR + rnd.nextInt(maxR - minR + 1) : minR;
			int g = (maxG - minG + 1 >= 0) ? minG + rnd.nextInt(maxG - minG + 1) : minG;
			int b = (maxB - minB + 1 >= 0) ? minB + rnd.nextInt(maxB - minB + 1) : minB;
			return r << 16 | g << 8 | b;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ColorRange)) return false;
			ColorRange other = (ColorRange)obj;
			return (other.min == min) && (other.max == max);
		}
		
	}
	
}
