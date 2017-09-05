package net.mcft.copy.backpacks.config.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import net.mcft.copy.backpacks.api.BackpackRegistry;
import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.item.ItemBackpack;
import net.mcft.copy.backpacks.misc.util.NbtUtils;
import net.mcft.copy.backpacks.misc.util.NbtUtils.NbtType;

public class SettingListSpawn extends Setting<List<SettingListSpawn.BackpackEntityEntry>> {
	
	public static class BackpackEntityEntry implements INBTSerializable<NBTTagCompound> {
		
		public static final String TAG_ID      = "id";
		public static final String TAG_ENTRIES = "entries";
		
		public String entityID = "";
		public List<BackpackEntry> entries = Collections.emptyList();
		
		public NBTTagCompound serializeNBT() {
			return NbtUtils.createCompound(
				TAG_ID, entityID,
				TAG_ENTRIES, entries);
		}
		public void deserializeNBT(NBTTagCompound nbt) {
			entityID = nbt.getString(TAG_ID);
			entries = NbtUtils.getTagList(nbt.getTagList(TAG_ENTRIES, NbtType.COMPOUND), BackpackEntry::new);
		}
		
		public BackpackEntityEntry clone() {
			BackpackEntityEntry entry = new BackpackEntityEntry();
			entry.entityID = entityID;
			entry.entries  = entries.stream().map(BackpackEntry::clone)
				.collect(Collectors.toCollection(ArrayList::new));
			return entry;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof BackpackEntityEntry)) return false;
			BackpackEntityEntry entry = (BackpackEntityEntry)obj;
			return entry.entityID.equals(entityID)
				&& entry.entries.equals(entries);
		}
		
	}
	
	public static class BackpackEntry implements INBTSerializable<NBTTagCompound> {
		
		public static final BackpackEntry DEFAULT = new BackpackEntry(
			"wearablebackpacks:backpack", 1000, "wearablebackpacks:backpack/default");
		
		public static final String TAG_ID         = "id";
		public static final String TAG_BACKPACK   = "backpack";
		public static final String TAG_CHANCE     = "chance";
		public static final String TAG_LOOT_TABLE = "lootTable";
		
		public String id;
		public String backpack;
		public int chance;
		public String lootTable;
		
		public BackpackEntry() {  }
		public BackpackEntry(NBTBase tag) { deserializeNBT((NBTTagCompound)tag); }
		public BackpackEntry(String backpack, int chance, String lootTable)
			{ this(null, backpack, chance, lootTable); }
		public BackpackEntry(String id, String backpack, int chance, String lootTable)
			{ this.id = id; this.backpack = backpack; this.chance = chance; this.lootTable = lootTable; }
		
		public static BackpackEntry parse(String str) {
			String id = null;
			
			if (str.indexOf('=') >= 0) {
				String[] values = str.split("=", 2);
				id  = values[0];
				str = values[1];
			}
			
			String[] values = str.split(";", 4);
			if (values.length != 3) throw new IllegalArgumentException("Expected 3 parts, got " + values.length);
			int chance       = Integer.parseInt(values[0]);
			String backpack  = values[1];
			String lootTable = values[2];
			if (chance < 0) throw new IllegalArgumentException("Chance is negative");
			return new BackpackEntry(id, backpack, chance, lootTable);
		}
		
		public BackpackEntry clone()
			{ return new BackpackEntry(id, backpack, chance, lootTable); }
		
		@Override
		public String toString() {
			String str = (chance + ";" + backpack + ";" + lootTable);
			return (id != null) ? (id + "=" + str) : str;
		}
		
		public NBTTagCompound serializeNBT() {
			return NbtUtils.createCompound(
				TAG_ID,         id,
				TAG_CHANCE,     chance,
				TAG_BACKPACK,   backpack,
				TAG_LOOT_TABLE, lootTable);
		}
		public void deserializeNBT(NBTTagCompound nbt) {
			id        = nbt.hasKey(TAG_ID) ? nbt.getString(TAG_ID) : null;
			chance    = nbt.getInteger(TAG_CHANCE);
			backpack  = nbt.hasKey(TAG_BACKPACK) ? nbt.getString(TAG_BACKPACK) : null;
			lootTable = nbt.hasKey(TAG_LOOT_TABLE) ? nbt.getString(TAG_LOOT_TABLE) : null;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof BackpackEntry)) return false;
			BackpackEntry entry = (BackpackEntry)obj;
			return (entry.chance == chance)
				&& ((entry.id != null) || (id != null)) // If either entry has an
					? Objects.equals(entry.id, id)      // id set, just compare that.
					: Objects.equals(entry.backpack, backpack)         // Otherwise compare backpack
						&& Objects.equals(entry.lootTable, lootTable); // and loot table fields.
		}
		
	}
	
	
	public SettingListSpawn() {
		super(Collections.emptyList());
		setConfigEntryClass("net.mcft.copy.backpacks.client.gui.config.custom.EntryListSpawn");
	}
	
	
	@Override
	protected void loadFromConfiguration(Configuration config) {
		set(config.getCategoryNames().stream()
			.filter(name -> name.startsWith(getCategory() + Configuration.CATEGORY_SPLITTER))
			.sorted(Comparator.comparingInt(category -> config.get(category, "index", Integer.MAX_VALUE).getInt()))
			.map(category -> {
				BackpackEntityEntry entry = new BackpackEntityEntry();
				entry.entityID = category.split("\\" + Configuration.CATEGORY_SPLITTER, 2)[1];
				// TODO: Read other per-entity properties.
				entry.entries = Arrays.stream(config.get(category, "entries", new String[0]).getStringList())
					.map(BackpackEntry::parse).collect(Collectors.toList());
				return entry;
			}).collect(Collectors.toList()));
	}
	
	@Override
	protected void saveToConfiguration(Configuration config) {
		// Remove all existing categories that belong to this setting.
		// Just doing this to make sure that old ones are properly removed.
		config.getCategoryNames().stream()
			.filter(name -> name.startsWith(getCategory() + Configuration.CATEGORY_SPLITTER))
			.forEach(category -> config.removeCategory(config.getCategory(category)));
		
		int index = 0;
		for (BackpackEntityEntry entity : get()) {
			String category = getCategory() + Configuration.CATEGORY_SPLITTER + entity.entityID;
			config.setCategoryPropertyOrder(category, Arrays.asList(
				"index", "entries"));
			
			config.get(category, "index", 0).set(index++);
			
			// TODO: Add other per-entity properties.
			config.get(category, "entries", new String[0]).set(entity.entries.stream()
				.map(BackpackEntry::toString).toArray(length -> new String[length]));
		}
	}
	
	
	@Override
	public List<BackpackEntityEntry> read(NBTBase tag)
		{ return NbtUtils.getTagList((NBTTagList)tag, BackpackEntityEntry::new); }
	@Override
	public NBTBase write(List<BackpackEntityEntry> value)
		{ return NbtUtils.createTag(value); }
	
	
	private static List<BackpackEntityEntry> _default;
	private static Set<String> _entityIDs = new HashSet<>();
	private static Set<String> _entryIDs  = new HashSet<>();
	
	public static List<BackpackEntityEntry> getDefaultValue() {
		if (_default == null) {
			_default = new ArrayList<>();
			for (Map.Entry<Class<? extends EntityLivingBase>, List<BackpackRegistry.Entry>> entityEntry
				: BackpackRegistry.entities.entrySet()) {
				BackpackEntityEntry backpackEntry = new BackpackEntityEntry();
				_default.add(backpackEntry);
				
				backpackEntry.entityID = ForgeRegistries.ENTITIES.getEntries().stream()
					.filter(e -> e.getValue().getEntityClass() == entityEntry.getKey())
					.findAny().map(e -> e.getKey().toString()).orElse(null);
				if (backpackEntry.entityID == null) continue;
				_entityIDs.add(backpackEntry.entityID);
				
				backpackEntry.entries = new ArrayList<>();
				for (BackpackRegistry.Entry e : entityEntry.getValue()) {
					if (e.id != null) _entryIDs.add(e.id);
					String backpack = Item.REGISTRY.getNameForObject(e.backpack).toString();
					backpackEntry.entries.add(new BackpackEntry(e.id, backpack, e.chance, e.lootTable));
				}
			}
			_default.sort(Comparator.comparing(e -> e.entityID));
		}
		return _default;
	}
	public static Set<String> getDefaultEntityIDs()
		{ getDefaultValue(); return _entityIDs; }
	public static Set<String> getDefaultEntryIDs()
		{ getDefaultValue(); return _entryIDs; }
	
	@Override
	public void update() {
		if (!isEnabled()) return;
		getDefaultValue(); // Call to make sure default is initialized.
		BackpackRegistry.entities.clear();
		for (SettingListSpawn.BackpackEntityEntry entityEntry : get()) {
			EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityEntry.entityID));
			// Ignore entry if entity is missing or not a subclass of EntityLivingBase.
			if ((entry == null) || !EntityLivingBase.class.isAssignableFrom(entry.getEntityClass())) continue;
			Class<? extends EntityLivingBase> entityClass = entry.getEntityClass().asSubclass(EntityLivingBase.class);
			
			List<BackpackRegistry.Entry> entries = new ArrayList<>();
			BackpackRegistry.entities.put(entityClass, entries);
			
			for (BackpackEntry backpackEntry : entityEntry.entries) {
				ItemBackpack backpack = (ItemBackpack)Item.getByNameOrId(backpackEntry.backpack);
				if (backpack == null) continue; // Ignore entry if backpack item is missing.
				entries.add(new BackpackRegistry.Entry(
					backpackEntry.id, backpack, backpackEntry.chance, backpackEntry.lootTable));
			}
		}
	}
	
}
