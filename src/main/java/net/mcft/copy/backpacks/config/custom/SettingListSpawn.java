package net.mcft.copy.backpacks.config.custom;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.config.Configuration;

import net.mcft.copy.backpacks.api.BackpackRegistry;
import net.mcft.copy.backpacks.api.BackpackRegistry.BackpackEntityEntry;
import net.mcft.copy.backpacks.api.BackpackRegistry.BackpackEntry;
import net.mcft.copy.backpacks.api.BackpackRegistry.ColorRange;
import net.mcft.copy.backpacks.api.BackpackRegistry.RenderOptions;
import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.misc.util.NbtUtils;
import net.mcft.copy.backpacks.misc.util.NbtUtils.NbtType;

public class SettingListSpawn extends Setting<List<BackpackEntityEntry>> {
	
	public SettingListSpawn() {
		super(Collections.emptyList());
		setConfigEntryClass("net.mcft.copy.backpacks.client.gui.config.custom.EntryListSpawn");
	}
	
	
	@Override
	protected void loadFromConfiguration(Configuration config) {
		set(config.getCategoryNames().stream()
			.filter(name -> name.startsWith(getCategory() + Configuration.CATEGORY_SPLITTER))
			.sorted(Comparator.comparingInt(category -> config.get(category, "index", Integer.MAX_VALUE).getInt()))
			.map(category -> loadEntity(config, category))
			.collect(Collectors.toList()));
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
				"index", "translate", "rotate", "scale", "entries"));
			config.get(category, "index", 0).set(index++);
			saveEntity(config, category, entity);
		}
	}
	
	
	@Override
	public List<BackpackEntityEntry> read(NBTBase tag) {
		return NbtUtils.stream((NBTTagList)tag)
			.map(NBTTagCompound.class::cast)
			.map(SettingListSpawn::deserializeEntity)
			.collect(Collectors.toList());
	}
	@Override
	public NBTBase write(List<BackpackEntityEntry> value) {
		return value.stream()
			.map(SettingListSpawn::serializeEntity)
			.collect(NbtUtils.toList());
	}
	
	
	@Override
	public void update() {
		if (!isEnabled()) return;
		BackpackRegistry.updateEntityEntries(get());
	}
	
	
	// Utility methods for cloning / equals / parsing / NBT.
	// Don't want these clogging up the API, I guess.
	
	// BackpackEntityEntry
	
	public static final String TAG_ENTITY_ID      = "id";
	public static final String TAG_RENDER_OPTIONS = "render";
	public static final String TAG_ENTRIES        = "entries";
	
	private static void saveEntity(Configuration config, String category, BackpackEntityEntry value) {
		saveRenderOptions(config, category, value.renderOptions);
		config.get(category, "entries", new String[0]).set(value.getEntries().stream()
			.map(SettingListSpawn::toString).toArray(length -> new String[length]));
	}
	private static BackpackEntityEntry loadEntity(Configuration config, String category) {
		return new BackpackEntityEntry(
			category.split("\\" + Configuration.CATEGORY_SPLITTER, 2)[1],
			loadRenderOptions(config, category),
			Arrays.stream(config.get(category, "entries", new String[0]).getStringList())
				.map(SettingListSpawn::parseBackpack).collect(Collectors.toList()), false);
	}
	
	private static NBTTagCompound serializeEntity(BackpackEntityEntry value) {
		return NbtUtils.createCompound(
			TAG_ENTITY_ID,      value.entityID,
			TAG_RENDER_OPTIONS, serializeRenderOptions(value.renderOptions));
		// Entries don't need to be synchronized to clients.
		// 	TAG_ENTRIES,        value.getEntries().stream()
		// 		.map(SettingListSpawn::serializeBackpack)
		// 		.collect(NbtUtils.toList()));
	}
	private static BackpackEntityEntry deserializeEntity(NBTTagCompound nbt) {
		return new BackpackEntityEntry(
			nbt.getString(TAG_ENTITY_ID),
			deserializeRenderOptions(nbt.getCompoundTag(TAG_RENDER_OPTIONS)),
			Collections.emptyList(), false);
		// Entries don't need to be synchronized to clients.
		// 	NbtUtils.stream(nbt.getTagList(TAG_ENTRIES, NbtType.COMPOUND))
		// 		.map(NBTTagCompound.class::cast)
		// 		.map(SettingListSpawn::deserializeBackpack)
		// 		.collect(Collectors.toList()));
	}
	
	// RenderOptions
	
	public static final String TAG_TRANSLATE = "translate";
	public static final String TAG_ROTATE    = "rotate";
	public static final String TAG_SCALE     = "scale";
	
	private static void saveRenderOptions(Configuration config, String category, RenderOptions value) {
		config.get(category, "translate", new double[3]).set(new double[]{ value.x, value.y, value.z });
		config.get(category, "rotate", 0.0).set(value.rotate);
		config.get(category, "scale", 0.0).set(value.scale);
	}
	private static RenderOptions loadRenderOptions(Configuration config, String category) {
		double[] translate = config.get(category, "translate", new double[3]).getDoubleList();
		return new RenderOptions(
			translate[0], translate[1], translate[2],
			config.get(category, "rotate", 0.0).getDouble(),
			config.get(category, "scale", 0.0).getDouble());
	}
	
	private static NBTTagCompound serializeRenderOptions(RenderOptions value) {
		return NbtUtils.createCompound(
			TAG_TRANSLATE, NbtUtils.createList(value.x, value.y, value.z),
			TAG_ROTATE, value.rotate,
			TAG_SCALE, value.scale);
	}
	private static RenderOptions deserializeRenderOptions(NBTTagCompound nbt) {
		NBTTagList translate = nbt.getTagList(TAG_TRANSLATE, NbtType.DOUBLE);
		return new RenderOptions(
			translate.getDoubleAt(0),
			translate.getDoubleAt(1),
			translate.getDoubleAt(2),
			nbt.getDouble(TAG_ROTATE),
			nbt.getDouble(TAG_SCALE));
	}
	
	// BackpackEntry
	
	/* Entries don't need to be synchronized to clients.
	 * Leaving this in for the sake of completion - or if needed in the future.
	public static final String TAG_BACKPACK_ID = "id";
	public static final String TAG_BACKPACK    = "backpack";
	public static final String TAG_CHANCE      = "chance";
	public static final String TAG_LOOT_TABLE  = "lootTable";
	public static final String TAG_COLOR_MIN   = "colorMin";
	public static final String TAG_COLOR_MAX   = "colorMax";
	
	private static NBTTagCompound serializeBackpack(BackpackEntry value) {
		return NbtUtils.createCompound(
			TAG_BACKPACK_ID, value.id,
			TAG_CHANCE,      value.chance,
			TAG_BACKPACK,    value.backpack,
			TAG_LOOT_TABLE,  value.lootTable,
			TAG_COLOR_MIN,   (value.colorRange != null) ? value.colorRange.min : null,
			TAG_COLOR_MAX,   (value.colorRange != null) ? value.colorRange.max : null);
	}
	private static BackpackEntry deserializeBackpack(NBTTagCompound nbt) {
		return new BackpackEntry(
			nbt.getString(TAG_BACKPACK_ID),
			nbt.getString(TAG_BACKPACK),
			nbt.getInteger(TAG_CHANCE),
			nbt.getString(TAG_LOOT_TABLE),
			nbt.hasKey(TAG_COLOR_MIN)
				? new ColorRange(nbt.getInteger(TAG_COLOR_MIN),
				                 nbt.getInteger(TAG_COLOR_MAX))
				: null);
	}
	*/
	
	private static final String SEPERATOR       = ",";
	private static final String ID_SEPERATOR    = "=";
	private static final String COLOR_SEPERATOR = "~";
	private static final String COLOR_NULL      = "DEFAULT";
	
	private static BackpackEntry parseBackpack(String str) {
		String id = null;
		if (str.indexOf('=') >= 0) {
			String[] values = str.split("\\" + ID_SEPERATOR, 2);
			id  = values[0].trim();
			str = values[1];
		}
		String[] values = str.split("\\" + SEPERATOR);
		if (values.length != 4) throw new IllegalArgumentException(
			"Expected 4 parts for backpack entry, got " + values.length);
		int chance       = Integer.parseInt(values[0].trim());
		String backpack  = values[1].trim();
		String lootTable = values[2].trim();
		ColorRange color = parseColorRange(values[3].trim());
		if (chance < 0) throw new IllegalArgumentException("Chance is negative");
		return new BackpackEntry(id, backpack, chance, lootTable, color, false);
	}
	private static String toString(BackpackEntry value) {
		String str = value.chance + SEPERATOR + " " +
		             value.backpack + SEPERATOR + " " +
		             value.lootTable + SEPERATOR + " " +
		             toString(value.colorRange);
		return (value.id != null) ? (value.id + " " + ID_SEPERATOR + " " + str) : str;
	}
	
	private static ColorRange parseColorRange(String str) {
		if (COLOR_NULL.equalsIgnoreCase(str)) return null;
		String[] minMax = str.split("\\" + COLOR_SEPERATOR, 2);
		if (minMax.length != 2) throw new IllegalArgumentException(
			"Expected 2 parts for color range, got " + minMax.length);
		String min = minMax[0].trim();
		String max = minMax[1].trim();
		if ((min.length() != 7) || (min.charAt(0) != '#') ||
		    (max.length() != 7) || (max.charAt(0) != '#')) throw new IllegalArgumentException(
				"Colors in the color range are not in the format #RRGGBB");
		return new ColorRange(Integer.parseInt(min.substring(1), 16),
		                      Integer.parseInt(max.substring(1), 16));
	}
	private static String toString(ColorRange colorRange) {
		return (colorRange != null)
			? String.format("#%06X %s #%06X", colorRange.min, COLOR_SEPERATOR, colorRange.max)
			: COLOR_NULL;
	}
	
}
