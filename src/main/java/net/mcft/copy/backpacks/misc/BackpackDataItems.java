package net.mcft.copy.backpacks.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;

import net.minecraftforge.items.ItemStackHandler;

import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.misc.util.NbtUtils;

public class BackpackDataItems implements IBackpackData {
	
	public static final String TAG_SIZE  = "size";
	public static final String TAG_ITEMS = "items";
	public static final String TAG_LOOT_TABLE      = "LootTable";
	public static final String TAG_LOOT_TABLE_SEED = "LootTableSeed";
	
	private BackpackSize _size;
	private ItemStackHandler _items = null;
	private String _lootTable = null;
	private long _lootTableSeed = 0;
	
	public BackpackDataItems() {  }
	public BackpackDataItems(int columns, int rows) { this(new BackpackSize(columns, rows)); }
	public BackpackDataItems(BackpackSize size) { _size = size; }
	
	/** Returns the size (in columns and rows) of this backpack. */
	public BackpackSize getSize() { return _size; }
	
	/** Returns the contained items of this backpack, generating it if necessary. */
	public ItemStackHandler getItems() { return getItems(null, null); }
	/** Returns the contained items of this backpack as accessed by
	 *  the specified world and player, generating it if necessary. */
	public ItemStackHandler getItems(World world, EntityPlayer player) {
		if (_items == null) {
			_items = new ItemStackHandler(_size.getColumns() * _size.getRows());
			if (_lootTable != null) {
				generateLoot(_items, _lootTable, _lootTableSeed, world, player);
				_lootTable = null;
			}
		}
		return _items;
	}
	
	/** Sets the backpack's loot table and seed. */
	public void setLootTable(String lootTable, long lootTableSeed) {
		if (_items != null) throw new UnsupportedOperationException();
		_lootTable = lootTable;
		_lootTableSeed = lootTableSeed;
	}
	
	
	public static void generateLoot(ItemStackHandler items, String tableStr, long seed,
	                                World world, EntityPlayer player) {
		Random rnd = new Random(seed);
		double maxFullness = (0.6 + rnd.nextDouble() * 0.2);
		int maxOccupiedSlots = (int)Math.ceil(items.getSlots() * maxFullness);
		
		LootTableManager manager = world.getLootTableManager();
		LootTable table = manager.getLootTableFromLocation(new ResourceLocation(tableStr));
		LootContext context = new LootContext(((player != null) ? player.getLuck() : 0),
		                                      (WorldServer)world, manager, player, null, null);
		List<ItemStack> loot = table.generateLootForPools(rnd, context);
		Collections.shuffle(loot);
		
		List<Integer> randomizedSlots = new ArrayList<Integer>(items.getSlots());
		for (int i = 0; i < items.getSlots(); i++) randomizedSlots.add(i);
		Collections.shuffle(randomizedSlots);
		for (int i = 0; (i < maxOccupiedSlots) && (i < loot.size()); i++) {
			ItemStack stack = loot.get(i);
			int slot = randomizedSlots.get(i);
			items.setStackInSlot(slot, stack);
		}
	}
	
	
	@Override
	public NBTBase serializeNBT() {
		return NbtUtils.createCompound(
			TAG_SIZE, _size.serializeNBT(),
			TAG_ITEMS, ((_items != null) ? _items.serializeNBT() : null),
			TAG_LOOT_TABLE, ((_lootTable != null) ? _lootTable.toString() : null),
			TAG_LOOT_TABLE_SEED, ((_lootTable != null) ? _lootTableSeed : null));
	}
	
	@Override
	public void deserializeNBT(NBTBase nbt) {
		NBTTagCompound compound = (NBTTagCompound)nbt;
		if (compound.hasKey(TAG_SIZE)) {
			_size = BackpackSize.parse(compound.getTag(TAG_SIZE));
			if (compound.hasKey(TAG_ITEMS)) {
				getItems().deserializeNBT(compound.getCompoundTag(TAG_ITEMS));
			} else if (compound.hasKey(TAG_LOOT_TABLE)) {
				_lootTable     = compound.getString(TAG_LOOT_TABLE);
				_lootTableSeed = compound.getLong(TAG_LOOT_TABLE_SEED);
			}
		} else {
			// Backwards compatibility for 1.5.0 / 2.2.0 and before.
			getItems().deserializeNBT(compound);
			_size = new BackpackSize(9, _items.getSlots() / 9);
		}
	}
	
}
