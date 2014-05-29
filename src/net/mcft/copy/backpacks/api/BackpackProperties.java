package net.mcft.copy.backpacks.api;

import net.mcft.copy.core.misc.SyncedEntityProperties;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BackpackProperties extends SyncedEntityProperties {
	
	// Used by EntityUtils.getIdentifier().
	public static final String IDENTIFIER = "WearableBackpack";
	
	public static final String TAG_STACK = "stack";
	public static final String TAG_DATA = "data";
	
	public ItemStack backpackStack = null;
	public IBackpackData backpackData = null;
	
	public IBackpack lastBackpackType = null;
	public int playersUsing = 0;
	
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
	
}
