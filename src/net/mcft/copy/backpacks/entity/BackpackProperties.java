package net.mcft.copy.backpacks.entity;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.api.IBackpackProperties;
import net.mcft.copy.core.misc.SyncedEntityProperties;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BackpackProperties extends SyncedEntityProperties implements IBackpackProperties {
	
	// Used by EntityUtils.getIdentifier().
	public static final String IDENTIFIER = "WearableBackpack";
	
	public static final String TAG_STACK = "stack";
	public static final String TAG_DATA = "data";
	
	public ItemStack backpackStack = null;
	public IBackpackData backpackData = null;
	
	public IBackpack lastBackpackType = null;
	public int playersUsing = 0;
	
	// SyncedEntityProperties methods
	
	@Override
	public EntityLivingBase getEntity() { return (EntityLivingBase)super.getEntity(); }
	
	@Override
	public boolean isWrittenToEntity() { return ((backpackStack != null) || (backpackData != null)); }
	@Override
	public boolean requiresSyncing() { return (backpackStack != null); }
	
	@Override
	public void write(NBTTagCompound compound) {
		if (backpackStack != null)
			compound.setTag(TAG_STACK, backpackStack.writeToNBT(new NBTTagCompound()));
	}
	@Override
	public void read(NBTTagCompound compound) {
		if (compound.hasKey(TAG_STACK))
			backpackStack = ItemStack.loadItemStackFromNBT(compound.getCompoundTag(TAG_STACK));
	}
	
	@Override
	public void writeToEntity(NBTTagCompound compound) {
		if (backpackData != null) {
			NBTTagCompound dataCompound = new NBTTagCompound();
			backpackData.writeToNBT(dataCompound);
			compound.setTag(TAG_DATA, dataCompound);
		}
	}
	@Override
	public void readFromEntity(NBTTagCompound compound) {
		ItemStack backpack = BackpackHelper.getEquippedBackpack(getEntity());
		lastBackpackType = BackpackHelper.getBackpackType(backpack);
		
		if (compound.hasKey(TAG_DATA) && (lastBackpackType != null))
			(backpackData = lastBackpackType.createBackpackData())
					.readFromNBT(compound.getCompoundTag(TAG_DATA));
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
