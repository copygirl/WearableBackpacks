package net.mcft.copy.backpacks.misc;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.api.IBackpackType;
import net.mcft.copy.backpacks.misc.util.MiscUtils;
import net.mcft.copy.backpacks.misc.util.NbtUtils;
import net.mcft.copy.backpacks.misc.util.NbtUtils.NbtType;
import net.mcft.copy.backpacks.network.MessageBackpackUpdate;

/** Concrete implementation of the IBackpack interface and capability, used for entities. */
public class BackpackCapability implements IBackpack {
	
	public static final String TAG_STACK = "stack";
	public static final String TAG_TYPE  = "type";
	public static final String TAG_DATA  = "data";
	
	public static final ResourceLocation IDENTIFIER =
		new ResourceLocation("wearablebackpacks:backpack");
	
	
	public final EntityLivingBase entity;
	
	public ItemStack stack = ItemStack.EMPTY;
	public IBackpackData data = null;
	public int playersUsing = 0;
	public int lidTicks = 0;
	public int prevLidTicks = 0;
	
	// This is also null if the backpack is not equipped to the chestplate slot.
	public IBackpackType lastType = null;
	
	public BackpackCapability(EntityLivingBase entity) { this.entity = entity; }
	
	/** Returns if the entity is wearing the backpack in the chest armor slot. */
	public boolean isChestArmor() {
		return ((lastType != null) || (BackpackHelper.getBackpackType(
			entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST)) != null));
	}
	
	// IBackpack implementation
	
	@Override
	public ItemStack getStack() {
		if (!stack.isEmpty()) return stack;
		ItemStack chestArmor = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		return ((BackpackHelper.getBackpackType(chestArmor) != null) ? chestArmor : ItemStack.EMPTY);
	}
	
	@Override
	public void setStack(ItemStack value) {
		ItemStack lastStack = stack;
		boolean chestArmorChanged = false;
		
		// Remove previous backpack from chest armor slot, if any.
		if (isChestArmor()) {
			entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemStack.EMPTY);
			chestArmorChanged = true;
		}
		
		// Equip backpack to chest armor slot if config option is set to enabled.
		if (BackpackHelper.equipAsChestArmor) {
			stack = ItemStack.EMPTY;
			lastType = ((!value.isEmpty()) ? BackpackHelper.getBackpackType(value) : null);
			entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, value);
			chestArmorChanged = true;
		// Otherwise store it inside the capability.
		} else {
			stack = value;
			lastType = null;
		}
		
		if (!entity.world.isRemote) {
			// If chest armor was changed and this is a player, send the updated stack.
			if (chestArmorChanged && (entity instanceof EntityPlayer))
				((EntityPlayer)entity).inventoryContainer.detectAndSendChanges();
			// If backpack capability stack was changed, send it to everyone who can see the entity.
			if (stack != lastStack)
				WearableBackpacks.CHANNEL.sendToAllTracking(
					MessageBackpackUpdate.stack(entity, stack), entity, true);
		}
	}
	
	@Override
	public IBackpackData getData() { return data; }
	@Override
	public void setData(IBackpackData value) { data = value; }
	
	@Override
	public int getPlayersUsing() { return playersUsing; }
	@Override
	public void setPlayersUsing(int value) {
		// If the backpack is being opened or closed (# of players
		// using changed from 0 to non-zero or the other way around),
		// send an update to anyone who can see this entity.
		if ((value > 0) != (playersUsing > 0))
			WearableBackpacks.CHANNEL.sendToAllTracking(
				MessageBackpackUpdate.open(entity, (value > 0)), entity, true);
		playersUsing = value;
	}
	
	@Override
	public int getLidTicks() { return lidTicks; }
	@Override
	public int getPrevLidTicks() { return prevLidTicks; }
	@Override
	public void setLidTicks(int value) { prevLidTicks = lidTicks; lidTicks = value; }
	
	@Override
	public IBackpackType getType() { return (lastType != null)
		? lastType : BackpackHelper.getBackpackType(getStack()); }
	
	// Capabilities related implementations
	
	public static class Provider implements ICapabilitySerializable<NBTTagCompound> {
		
		final BackpackCapability backpack;
		
		public Provider(EntityLivingBase entity) { backpack = new BackpackCapability(entity); }
		
		// ICapabilityProvider implementation
		
		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return (capability == IBackpack.CAPABILITY);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return ((capability == IBackpack.CAPABILITY) ? (T)backpack : null);
		}
		
		// INBTSerializable implementation
		
		@Override
		public void deserializeNBT(NBTTagCompound compound) {
			backpack.stack = NbtUtils.readItem(compound.getCompoundTag(TAG_STACK));
			
			IBackpackType type;
			if (backpack.stack.isEmpty()) {
				if (!compound.hasKey(TAG_TYPE, NbtType.STRING)) return;
				// If the backpack has its type saved, restore it.
				// This is the case when the backpack stack is equipped in
				// the chest armor slot, which has not yet been loaded. :'(
				String id = compound.getString(TAG_TYPE);
				backpack.lastType = type = BackpackHelper.getBackpackType(MiscUtils.getItemFromName(id));
				if (type == null) return;
			} else type = BackpackHelper.getBackpackType(backpack.stack);
			
			if (type == null) {
				WearableBackpacks.LOG.error("Backpack type was null when deserializing backpack capability");
				return;
			}
			
			backpack.data = type.createBackpackData(backpack.getStack());
			NBTBase dataTag = compound.getTag(TAG_DATA);
			if ((backpack.data != null) && (dataTag != null))
				backpack.data.deserializeNBT(dataTag);
		}
		
		@Override
		public NBTTagCompound serializeNBT() {
			return NbtUtils.createCompound(
				TAG_STACK, ((!backpack.stack.isEmpty()) ? backpack.stack.serializeNBT() : null),
				// If the backpack is stored in the chest armor slot, we need to save the item. See deserializeNBT.
				TAG_TYPE, (backpack.isChestArmor() ? backpack.getStack().getItem().getRegistryName().toString() : null),
				TAG_DATA, ((backpack.data != null) ? backpack.data.serializeNBT() : null));
		}
		
	}
	
	public static class Storage implements Capability.IStorage<IBackpack> {
		
		@Override
		public NBTBase writeNBT(Capability<IBackpack> capability, IBackpack instance, EnumFacing side) {
			BackpackCapability backpack = (BackpackCapability)instance;
			return ((backpack.stack.isEmpty()) && (backpack.data == null)) ? null
				: NbtUtils.createCompound(
					TAG_STACK, ((!backpack.stack.isEmpty()) ? backpack.stack.serializeNBT() : null),
					TAG_DATA, ((backpack.data != null) ? backpack.data.serializeNBT() : null));
		}
		
		@Override
		public void readNBT(Capability<IBackpack> capability, IBackpack instance, EnumFacing side, NBTBase nbt) {
			BackpackCapability backpack = (BackpackCapability)instance;
			if (!(nbt instanceof NBTTagCompound)) return;
			NBTTagCompound compound = (NBTTagCompound)nbt;
			
			ItemStack stack = NbtUtils.readItem(compound.getCompoundTag(TAG_STACK));
			backpack.setStack(stack);
			
			IBackpackType type;
			if (stack.isEmpty()) {
				// Try to get the backpack type from the chestplate slot.
				stack = backpack.entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
				backpack.lastType = type = BackpackHelper.getBackpackType(stack);
				if (type == null) return; // No backpack equipped.
			} else type = BackpackHelper.getBackpackType(stack);
			
			IBackpackData data = type.createBackpackData(stack);
			NBTBase dataTag = compound.getTag(TAG_DATA);
			if (dataTag != null) data.deserializeNBT(dataTag);
			backpack.setData(data);
		}
		
	}
	
}
