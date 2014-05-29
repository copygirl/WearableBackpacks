package net.mcft.copy.backpacks.entity;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.api.IBackpackProperties;
import net.mcft.copy.core.misc.SyncedEntityProperties;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BackpackProperties extends SyncedEntityProperties implements IBackpackProperties {
	
	// Used by EntityUtils.getIdentifier().
	public static final String IDENTIFIER = "WearableBackpack";
	
	public static final String TAG_STACK = "stack";
	public static final String TAG_DATA = "data";
	public static final String TAG_TYPE = "type";
	
	public int playersUsing = 0;
	
	private ItemStack backpackStack = null;
	private IBackpackData backpackData = null;
	private IBackpack lastBackpackType = null;
	
	// SyncedEntityProperties methods
	
	@Override
	public EntityLivingBase getEntity() { return (EntityLivingBase)super.getEntity(); }
	
	@Override
	public boolean isWrittenToEntity() { return ((getBackpackStack() != null) || (backpackData != null)); }
	@Override
	public boolean requiresSyncing() { return (getBackpackStack() != null); }
	
	@Override
	public void write(NBTTagCompound compound) {
		if (getBackpackStack() != null)
			compound.setTag(TAG_STACK, getBackpackStack().writeToNBT(new NBTTagCompound()));
	}
	@Override
	public void read(NBTTagCompound compound) {
		if (compound.hasKey(TAG_STACK))
			setBackpackStack(ItemStack.loadItemStackFromNBT(compound.getCompoundTag(TAG_STACK)));
	}
	
	@Override
	public void writeToEntity(NBTTagCompound compound) {
		if (getBackpackData() != null) {
			ItemStack backpack = BackpackHelper.getEquippedBackpack(getEntity());
			if ((getBackpackStack() == null) && (backpack != null))
				compound.setString(TAG_TYPE, Item.itemRegistry.getNameForObject(backpack.getItem()));
			NBTTagCompound dataCompound = new NBTTagCompound();
			getBackpackData().writeToNBT(dataCompound);
			compound.setTag(TAG_DATA, dataCompound);
		}
	}
	@Override
	public void readFromEntity(NBTTagCompound compound) {
		ItemStack backpack = BackpackHelper.getEquippedBackpack(getEntity());
		IBackpack backpackType = ((backpack != null)
				? BackpackHelper.getBackpackType(backpack)
				: (IBackpack)Item.itemRegistry.getObject(compound.getString(TAG_TYPE)));
		setLastBackpackType(backpackType);
		
		if (compound.hasKey(TAG_DATA) && (getLastBackpackType() != null)) {
			IBackpackData data = getLastBackpackType().createBackpackData();
			data.readFromNBT(compound.getCompoundTag(TAG_DATA));
			setBackpackData(data);
		}
	}
	
	// IBackpackProperties implementation
	
	@Override
	public ItemStack getBackpackStack() { return backpackStack; }
	@Override
	public void setBackpackStack(ItemStack stack) { backpackStack = stack; }
	@Override
	public IBackpackData getBackpackData() { return backpackData; }
	@Override
	public void setBackpackData(IBackpackData data) { backpackData = data; }
	@Override
	public IBackpack getLastBackpackType() { return lastBackpackType; }
	@Override
	public void setLastBackpackType(IBackpack type) { lastBackpackType = type; }
	
}
