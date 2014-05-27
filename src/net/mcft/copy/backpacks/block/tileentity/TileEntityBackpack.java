package net.mcft.copy.backpacks.block.tileentity;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.api.IBackpackTileEntity;
import net.mcft.copy.core.base.TileEntityBase;
import net.mcft.copy.core.util.DirectionUtils;
import net.mcft.copy.core.util.NbtUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityBackpack extends TileEntityBase
                                implements IBackpackTileEntity {
	
	public static final String TAG_STACK         = "stack";
	public static final String TAG_BACKPACK_DATA = "data";
	public static final String TAG_ORIENTATION   = "orientation";
	
	private ItemStack backpackStack = null;
	private IBackpackData backpackData = null;
	
	public int playersUsing = 0;
	public ForgeDirection orientation = ForgeDirection.UNKNOWN;
	
	// Loading, saving and syncing
	
	@Override
	public boolean hasDescriptionPacket() { return true; }
	
	@Override
	public void write(NBTTagCompound compound) {
		if (getBackpackStack() != null)
			compound.setTag(TAG_STACK, NbtUtils.writeItem(getBackpackStack()));
		compound.setByte(TAG_ORIENTATION, (byte)orientation.ordinal());
	}
	
	@Override
	public void read(NBTTagCompound compound) {
		if (compound.hasKey(TAG_STACK))
			setBackpackStack(NbtUtils.readItem(compound.getCompoundTag(TAG_STACK)));
		orientation = ForgeDirection.getOrientation(compound.getByte(TAG_ORIENTATION));
	}
	
	@Override
	public void writeToSave(NBTTagCompound compound) {
		if (getBackpackData() != null) {
			NBTTagCompound dataCompound = new NBTTagCompound();
			getBackpackData().writeToNBT(dataCompound);
			compound.setTag(TAG_BACKPACK_DATA, dataCompound);
		}
	}
	
	@Override
	public void readFromSave(NBTTagCompound compound) {
		if (compound.hasKey(TAG_BACKPACK_DATA) && (getBackpackStack() != null)) {
			IBackpack backpackType = BackpackHelper.getBackpackType(getBackpackStack());
			IBackpackData data = backpackType.createBackpackData();
			data.readFromNBT(compound.getCompoundTag(TAG_BACKPACK_DATA));
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
	
	// TileEntityBase methods
	
	@Override
	public void onBlockPlaced(EntityLivingBase entity, ItemStack stack,
	                          ForgeDirection side, float hitX, float hitY, float hitZ) {
		orientation = DirectionUtils.getOrientation(entity).getOpposite();
		setBackpackStack(stack.copy());
		setBackpackData(getBackpackType().createBackpackData());
	}
	
	@Override
	public boolean onBlockBreak(EntityPlayer player, boolean brokenInCreative) {
		if (player.isSneaking()) {
			// Don't break block if it can't be equipped.
			if (!BackpackHelper.canEquipBackpack(player))
				return false;
			
			BackpackHelper.setEquippedBackpack(
					player, getBackpackStack(), getBackpackData());
			getBackpackType().onEquip(player, this);
			
			setBackpackStack(null);
			setBackpackData(null);
			
		}
		return true;
	}
	
	@Override
	public void onBlockDestroyed(boolean brokenInCreative) {
		if (getBackpackStack() != null)
			getBackpackType().onBlockBreak(this);
	}
	
	@Override
	public boolean onBlockActivated(EntityPlayer player, ForgeDirection side,
	                                float hitX, float hitY, float hitZ) {
		if (getBackpackStack() != null)
			getBackpackType().onPlacedInteract(player, this);
		return true;
	}
	
	// Helper methods
	
	private IBackpack getBackpackType() {
		return BackpackHelper.getBackpackType(getBackpackStack());
	}
	
}
