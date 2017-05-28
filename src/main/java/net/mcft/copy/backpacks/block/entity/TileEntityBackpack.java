package net.mcft.copy.backpacks.block.entity;

import net.minecraft.entity.player.EntityPlayer;
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
import net.mcft.copy.backpacks.block.BlockBackpack;
import net.mcft.copy.backpacks.misc.util.NbtUtils;

// TODO: Implement ItemStackHandler (only for bottom side)?
public class TileEntityBackpack extends TileEntity implements ITickable, IBackpack {
	
	/** If any player is within this distance of the backpack, _despawnTimer is paused.' */
	private static final int DESPAWN_DISTANCE = 20;
	private static final int DESPAWN_TIME = 5 * 60 * 20; // 5 minutes
	
	public static final String TAG_AGE    = "age";
	public static final String TAG_STACK  = "stack";
	public static final String TAG_DATA   = "data";
	public static final String TAG_FACING = "facing";
	public static final String TAG_DESPAWN_TIMER = "despawnTimer";
	
	private int _age = 0;
	private ItemStack _stack = null;
	private IBackpackData _data = null;
	private int _playersUsing = 0;
	private int _lidTicks = 0;
	private int _prevLidTicks = 0;
	private int _despawnTimer = -1;
	
	public EnumFacing facing = EnumFacing.NORTH;
	
	/** Returns the age of this backpack tile entity in ticks. */
	public int getAge() { return _age; }
	
	@Override
	public void update() {
		_age++;
		if (world.isRemote)
			BackpackHelper.updateLidTicks(this,
				pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		else if (_despawnTimer >= 0) {
			// TODO: Slower than it needs to be; just want to check if there's ANY player nearby.
			EntityPlayer player = world.getClosestPlayer(
				pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, DESPAWN_DISTANCE, false);
			// If no player is within DESPAWN_DISTANCE blocks, increase the despawn timer
			// and if this timer is above the despawn time, destroy the tile entity.
			if ((player == null) && (++_despawnTimer > DESPAWN_TIME)) {
				_stack = null; _data = null;
				world.setBlockToAir(pos);
			}
		}
	}
	
	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 0) { _playersUsing = type; return true; }
		return false;
	}
	
	/** Called when the backpack is placed upon death. */
	public void setPlacedOnDeath(boolean mayDespawn) {
		// Set the age to a negative value - this prevents
		// backpacks from exploding right after being dropped.
		_age = -BlockBackpack.EXPLOSION_RESIST_TICKS;
		// If the backpack may despawn, set the despawn
		// timer to 0 so it'll be active, -1 otherwise.
		_despawnTimer = (mayDespawn ? 0 : -1);
	}
	
	// Reading/writing, loading/saving, update packets
	
	public void readNBT(NBTTagCompound compound, boolean isClient) {
		_age = (!isClient ? compound.getInteger(TAG_AGE) : 0);
		facing = EnumFacing.getFront(NbtUtils.get(compound, (byte)0, TAG_FACING) + 2);
		
		_stack = NbtUtils.readItem(compound.getCompoundTag(TAG_STACK));
		if ((_stack == null) || isClient) { _data = null; return; }
		
		_data = BackpackHelper.getBackpackType(_stack).createBackpackData(_stack);
		NBTBase dataTag = compound.getTag(TAG_DATA);
		if (dataTag != null) _data.deserializeNBT(dataTag);
		
		_despawnTimer = (compound.hasKey(TAG_DESPAWN_TIMER)
			? compound.getInteger(TAG_DESPAWN_TIMER) : -1);
	}
	
	public NBTTagCompound writeNBT(NBTTagCompound compound, boolean isClient) {
		NbtUtils.addToCompound(compound,
			TAG_AGE, (!isClient ? _age : null),
			TAG_FACING, (byte)(facing.ordinal() - 2),
			TAG_STACK, ((_stack != null) ? _stack.serializeNBT() : null),
			TAG_DATA, (((_data != null) && !isClient) ? _data.serializeNBT() : null),
			TAG_DESPAWN_TIMER, (((_despawnTimer >= 0) && !isClient) ? _despawnTimer : null));
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
		_despawnTimer = -1;
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
