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

// TODO: Implement ItemStackHandler (only for bottom side)?
public class TileEntityBackpack extends TileEntity implements ITickable, IBackpack {
	
	public static final String TAG_STACK  = "stack";
	public static final String TAG_DATA   = "data";
	public static final String TAG_FACING = "facing";
	
	private ItemStack _stack = ItemStack.EMPTY;
	private IBackpackData _data = null;
	private int _playersUsing = 0;
	private int _lidTicks = 0;
	private int _prevLidTicks = 0;
	
	public EnumFacing facing = EnumFacing.NORTH;
	
	@Override
	public void update() {
		if (world.isRemote)
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
		facing = EnumFacing.getFront(NbtUtils.get(compound, (byte)0, TAG_FACING) + 2);
		
		_stack = NbtUtils.readItem(compound.getCompoundTag(TAG_STACK));
		if (_stack.isEmpty() || isClient) { _data = null; return; }
		
		_data = BackpackHelper.getBackpackType(_stack).createBackpackData(_stack);
		NBTBase dataTag = compound.getTag(TAG_DATA);
		if (dataTag != null) _data.deserializeNBT(dataTag);
	}
	
	public NBTTagCompound writeNBT(NBTTagCompound compound, boolean isClient) {
		NbtUtils.addToCompound(compound,
			TAG_FACING, (byte)(facing.ordinal() - 2),
			TAG_STACK, (!_stack.isEmpty() ? _stack.serializeNBT() : null),
			TAG_DATA, (((_data != null) && !isClient) ? _data.serializeNBT() : null));
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
		if ((value > 0) != (_playersUsing > 0))
			world.addBlockEvent(pos, getBlockType(), 0, (value > 0) ? 1 : 0);
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
