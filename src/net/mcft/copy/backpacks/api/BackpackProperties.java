package net.mcft.copy.backpacks.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class BackpackProperties implements IExtendedEntityProperties {
	
	// Used by EntityUtils.getIdentifier().
	public static final String IDENTIFIER = "WearableBackpack";
	
	public static final String TAG_STACK = "stack";
	public static final String TAG_DATA = "data";
	
	public ItemStack backpackStack = null;
	public IBackpackData backpackData = null;
	
	public IBackpack lastBackpackType = null;
	public int playersUsing = 0;
	
	private EntityLivingBase entity;
	
	@Override
	public void init(Entity entity, World world) {
		this.entity = (EntityLivingBase)entity;
	}
	
	@Override
	public void saveNBTData(NBTTagCompound compound) {
		NBTTagCompound properties = new NBTTagCompound();
		if (backpackStack != null)
			properties.setTag(TAG_STACK, backpackStack.writeToNBT(new NBTTagCompound()));
		if (backpackData != null) {
			NBTTagCompound dataCompound = new NBTTagCompound();
			backpackData.writeToNBT(dataCompound);
			properties.setTag(TAG_DATA, dataCompound);
		}
		compound.setTag(IDENTIFIER, properties);
	}
	
	@Override
	public void loadNBTData(NBTTagCompound compound) {
		
		NBTTagCompound properties = compound.getCompoundTag(IDENTIFIER);
		if (properties == null) return;
		
		if (properties.hasKey(TAG_STACK))
			backpackStack = ItemStack.loadItemStackFromNBT(properties.getCompoundTag(TAG_STACK));
		
		ItemStack backpack = BackpackHelper.getEquippedBackpack(entity);
		lastBackpackType = BackpackHelper.getBackpackType(backpack);
		
		if (properties.hasKey(TAG_DATA) && (lastBackpackType != null))
			(backpackData = lastBackpackType.createBackpackData())
					.readFromNBT(properties.getCompoundTag(TAG_DATA));
	}
	
}
