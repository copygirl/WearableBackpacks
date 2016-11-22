package net.mcft.copy.backpacks.block.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.common.capabilities.Capability;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.misc.util.NbtUtils;

// TODO: Implement ItemStackHandler (only for bottom side?)
public class TileEntityBackpack extends TileEntity implements ITickable, IBackpack {
	
	private ItemStack _stack = null;
	private IBackpackData _data = null;
	private int _playersUsing = 0;
	private int _lidTicks = 0;
	private int _prevLidTicks = 0;
	
	@Override
	public void update() {
		if (worldObj.isRemote)
			BackpackHelper.updateLidTicks(this,
				pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
	}
	
	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 0) { _playersUsing = type; return true; }
		return false;
	}
	
	// Reading/writing, loading/saving, update packets
	
	public void readNBT(NBTTagCompound compound, boolean isClient) {
		_stack = NbtUtils.readItem(compound.getCompoundTag("stack"));
		if ((_stack == null) || isClient) { _data = null; return; }
		
		_data = BackpackHelper.getBackpackType(_stack).createBackpackData();
		NBTBase dataTag = compound.getTag("data");
		if (dataTag != null) _data.deserializeNBT(dataTag);
	}
	
	public NBTTagCompound writeNBT(NBTTagCompound compound, boolean isClient) {
		NbtUtils.addToCompound(compound,
			"stack", ((_stack != null) ? _stack.serializeNBT() : null),
			"data", (((_data != null) && !isClient) ? _data.serializeNBT() : null));
		return compound;
	}
	
	
	@Override
	public final SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(),
			writeNBT(new NBTTagCompound(), true));
	}
	
	@Override
	public final NBTTagCompound getUpdateTag() {
		return writeNBT(super.writeToNBT(new NBTTagCompound()), true);
	}
	
	@Override
	public final void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readNBT(pkt.getNbtCompound(), true);
	}
	
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		readNBT(compound, false);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		return writeNBT(super.writeToNBT(compound), false);
	}
	
	// IBackpack implementation
	
	@Override
	public ItemStack getStack() { return _stack; }
	@Override
	public void setStack(ItemStack value) { _stack = value; }
	
	@Override
	public IBackpackData getData() { return _data; }
	@Override
	public void setData(IBackpackData value) { _data = value; }
	
	@Override
	public int getPlayersUsing() { return _playersUsing; }
	@Override
	public void setPlayersUsing(int value) {
		if ((value > 0) != (_playersUsing > 0)) {
			worldObj.addBlockEvent(pos, getBlockType(), 0, (value > 0) ? 1 : 0);
		}
		_playersUsing = value;
	}
	
	@Override
	public int getLidTicks() { return _lidTicks; }
	@Override
	public int getPrevLidTicks() { return _prevLidTicks; }
	@Override
	public void setLidTicks(int value) { _prevLidTicks = _lidTicks; _lidTicks = value; }
	
	// Capability overrides
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return (capability == IBackpack.CAPABILITY);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return ((capability == IBackpack.CAPABILITY) ? (T)this : null);
	}
	
}
