package net.mcft.copy.backpacks.entity;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.core.entity.EntityProperty;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;

public class EntityPropertyBackpackType extends EntityProperty<IBackpack> {
	
	private final BackpackProperties properties;
	
	public EntityPropertyBackpackType(String name, BackpackProperties properties) {
		super(name, null);
		this.properties = properties;
	}
	
	@Override
	public IBackpack get() {
		// Automatically grab the most recent backpack type, if available.
		ItemStack backpack = getBackpack();
		if (backpack != null) set((IBackpack)backpack.getItem());
		return super.get();
	}
	
	@Override
	public NBTBase write() {
		return new NBTTagString(Item.itemRegistry.getNameForObject(get()));
	}
	@Override
	public void write(PacketBuffer buffer) {
		throw new UnsupportedOperationException("Last backpack type can't be synced.");
	}
	
	@Override
	public void read(NBTBase tag) {
		set((IBackpack)Item.itemRegistry.getObject(((NBTTagString)tag).func_150285_a_()));
	}
	@Override
	public void read(PacketBuffer buffer) {
		throw new UnsupportedOperationException("Last backpack type can't be synced.");
	}
	
	private ItemStack getBackpack() { return BackpackHelper.getEquippedBackpack(properties.getEntity()); }
	
}
