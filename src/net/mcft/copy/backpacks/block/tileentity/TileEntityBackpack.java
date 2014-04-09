package net.mcft.copy.backpacks.block.tileentity;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.api.IBackpackTileEntity;
import net.mcft.copy.core.util.NbtUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityBackpack extends TileEntity
                                implements IBackpackTileEntity {
	
	private ItemStack backpackStack = null;
	private IBackpackData backpackData = null;
	
	public int playersUsing = 0;
	
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (getBackpackStack() != null)
			compound.setTag("stack", NbtUtils.writeItem(getBackpackStack()));
		if (getBackpackData() != null)
			compound.setTag("data", getBackpackData().writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("stack"))
			setBackpackStack(NbtUtils.readItem(compound.getCompoundTag("stack")));
		if (compound.hasKey("data") && (getBackpackStack() != null)) {
			IBackpack backpackType = BackpackHelper.getBackpackType(getBackpackStack());
			IBackpackData data = backpackType.createBackpackData();
			data.readFromNBT(compound.getCompoundTag("data"));
			setBackpackData(data);
		}
	}
	
	// IBackpackTileEntity implementation
	
	@Override
	public ItemStack getBackpackStack() { return backpackStack; }
	
	@Override
	public void setBackpackStack(ItemStack stack) { backpackStack = stack; }
	
	@Override
	public IBackpackData getBackpackData() { return backpackData; }
	
	@Override
	public void setBackpackData(IBackpackData data) { backpackData = data; }
	
	@Override
	public boolean isUsedByPlayer() { return (playersUsing > 0); }
	
}
