package net.mcft.copy.backpacks.entity;

import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.core.entity.EntityProperty;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class EntityPropertyBackpackData extends EntityProperty<IBackpackData> {
	
	private final BackpackProperties properties;
	
	public EntityPropertyBackpackData(String name, BackpackProperties properties) {
		super(name, null);
		this.properties = properties;
	}
	
	@Override
	public NBTBase write() {
		NBTTagCompound compound = new NBTTagCompound();
		if (get() != null)
			get().writeToNBT(compound);
		return compound;
	}
	@Override
	public void write(PacketBuffer buffer) {
		throw new UnsupportedOperationException("Backpack data can't be synced.");
	}
	
	@Override
	public void read(NBTBase tag) {
		IBackpack type = properties.backpackType.get();
		if (type == null) return;
		IBackpackData data = type.createBackpackData();
		data.readFromNBT((NBTTagCompound)tag);
		set(data);
	}
	@Override
	public void read(PacketBuffer buffer) {
		throw new UnsupportedOperationException("Backpack data can't be synced.");
	}
	
}
