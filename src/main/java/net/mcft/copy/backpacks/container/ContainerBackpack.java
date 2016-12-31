package net.mcft.copy.backpacks.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.misc.BackpackDataItems;
import net.mcft.copy.backpacks.network.MessageOpenGui;

public abstract class ContainerBackpack extends Container {
	
	public static final String TAG_TITLE     = "title";
	public static final String TAG_LOCALIZED = "localized";
	public static final String TAG_SLOTS     = "slots";
	
	
	public final EntityPlayer player;
	public final IBackpack backpack;
	public final BackpackDataItems data;
	public final ItemStackHandler items;
	
	public final String title;
	public final boolean titleLocalized;
	
	public ContainerBackpack(EntityPlayer player, IBackpack backpack) {
		this.player = player;
		this.backpack = backpack;
		this.data = ((BackpackDataItems)backpack.getData());
		this.items = this.data.items;
		
		ItemStack stack = backpack.getStack();
		this.title = (stack.hasDisplayName() ? stack.getDisplayName()
			: "container.wearablebackpacks.backpack");
		this.titleLocalized = stack.hasDisplayName();
		
		setupSlots();
	}
	
	@SideOnly(Side.CLIENT)
	public ContainerBackpack(EntityPlayer player, NBTTagCompound data) {
		this.player = player;
		this.backpack = null;
		this.data = null;
		this.items = new ItemStackHandler(data.getInteger(TAG_SLOTS));
		
		this.title = data.getString(TAG_TITLE);
		this.titleLocalized = data.getBoolean(TAG_LOCALIZED);
		
		setupSlots();
	}
	
	/** Opens the container, sending a MessageOpenGui to the client. */
	public void open() {
		EntityPlayerMP player = (EntityPlayerMP)this.player;
		player.getNextWindowId();
		player.closeContainer();
		player.openContainer = this;
		windowId = player.currentWindowId;
		WearableBackpacks.CHANNEL.sendTo(new MessageOpenGui(this), player);
		addListener(player);
		backpack.setPlayersUsing(backpack.getPlayersUsing() + 1);
	}
	
	
	protected void setupSlots() {
		setupBackpackSlots();
		setupPlayerSlots();
	}
	
	protected void setupBackpackSlots() {
		int rows = (items.getSlots() / 9);
		for (int y = 0; y < rows; y++)
			for (int x = 0; x < 9; x++)
				addSlotToContainer(new SlotItemHandler(items, x + y * 9,
				                                       8 + x * 18, 18 + y * 18));
	}
	
	protected void setupPlayerSlots() {
		int yOffset = ((items.getSlots() / 9) - 4) * 18;
		// Inventory
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 9; x++)
				addSlotToContainer(new Slot(
					player.inventory, x + y * 9 + 9,
					8 + x * 18, 103 + y * 18 + yOffset));
		// Hotbar
		for (int x = 0; x < 9; x++)
			addSlotToContainer(new Slot(
				player.inventory, x,
				8 + x * 18, 161 + yOffset));
	}
	
	
	public void writeToNBT(NBTTagCompound compound) {
		compound.setString(TAG_TITLE, title);
		compound.setBoolean(TAG_LOCALIZED, titleLocalized);
		compound.setInteger(TAG_SLOTS, items.getSlots());
	}
	
	// Container overrides
	
	@Override
	public abstract boolean canInteractWith(EntityPlayer playerIn);
	
	@Override
	public void onContainerClosed(EntityPlayer player) {
		if (backpack != null) backpack.setPlayersUsing(backpack.getPlayersUsing() - 1);
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		Slot slot = inventorySlots.get(index);
		if (slot == null) return null;
		ItemStack stack = slot.getStack();
		if (stack.isEmpty()) return ItemStack.EMPTY;
		ItemStack result = stack.copy();
		
		if (index < items.getSlots()) {
			if (!mergeItemStack(stack, items.getSlots(), inventorySlots.size(), true))
				return ItemStack.EMPTY;
		} else if (!mergeItemStack(stack, 0, items.getSlots(), false))
			return ItemStack.EMPTY;
		
		if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
		else slot.onSlotChanged();
		
		return result;
	}
	
}