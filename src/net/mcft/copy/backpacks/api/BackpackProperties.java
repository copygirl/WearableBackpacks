package net.mcft.copy.backpacks.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class BackpackProperties implements IExtendedEntityProperties {
	
	// Used by EntityUtils.getIdentifier().
	public static final String identifier = "WearableBackpack";
	
	public ItemStack backpackStack = null;
	public IBackpackData backpackData = null;
	
	public IBackpack lastBackpackType = null;
	public int playersUsing = 0;
	
	// IPropertiesBackpack implementation
	
	private EntityLivingBase entity;
	
	@Override
	public void init(Entity entity, World world) {
		this.entity = (EntityLivingBase)entity;
	}
	
	@Override
	public void saveNBTData(NBTTagCompound compound) {
		if (backpackStack != null)
			compound.setTag("stack", backpackStack.writeToNBT(new NBTTagCompound()));
		if (backpackData != null)
			compound.setTag("data", backpackData.writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public void loadNBTData(NBTTagCompound compound) {
		if (compound.hasKey("stack"))
			backpackStack = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("stack"));
		
		ItemStack backpack = BackpackHelper.getEquippedBackpack(entity);
		lastBackpackType = BackpackHelper.getBackpackType(backpack);
		
		if (compound.hasKey("data") && (lastBackpackType != null))
			(backpackData = lastBackpackType.createBackpackData())
					.readFromNBT(compound.getCompoundTag("data"));
	}
	
}
