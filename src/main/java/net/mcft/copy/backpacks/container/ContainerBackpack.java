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
import net.mcft.copy.backpacks.misc.BackpackSize;
import net.mcft.copy.backpacks.network.MessageOpenGui;

public abstract class ContainerBackpack extends Container {
	
	public static final String TAG_SIZE      = "size";
	public static final String TAG_TITLE     = "title";
	public static final String TAG_LOCALIZED = "localized";
	
	
	public final EntityPlayer player;
	public final IBackpack backpack;
	public final BackpackDataItems data;
	
	public final BackpackSize size;
	public final ItemStackHandler items;
	
	public final String title;
	public final boolean titleLocalized;
	
	
	public ContainerBackpack(EntityPlayer player, IBackpack backpack) {
		this.player   = player;
		this.backpack = backpack;
		this.data     = ((BackpackDataItems)backpack.getData());
		
		size  = data.size;
		items = data.items;
		
		ItemStack stack = backpack.getStack();
		title = (stack.hasDisplayName() ? stack.getDisplayName()
			: "container.wearablebackpacks.backpack");
		titleLocalized = stack.hasDisplayName();
		
		setupSlots();
	}
	
	@SideOnly(Side.CLIENT)
	public ContainerBackpack(EntityPlayer player, NBTTagCompound data) {
		this.player   = player;
		this.backpack = null;
		this.data     = null;
		
		size  = BackpackSize.parse(data.getTag(TAG_SIZE));
		items = new ItemStackHandler(size.getColumns() * size.getRows());
		
		title = data.getString(TAG_TITLE);
		titleLocalized = data.getBoolean(TAG_LOCALIZED);
		
		setupSlots();
	}
	
	/** Opens the container, sending a MessageOpenGui to the client. */
	public void open() {
		EntityPlayerMP player = (EntityPlayerMP)this.player;
		player.getNextWindowId();
		player.closeContainer();
		player.openContainer = this;
		windowId = player.currentWindowId;
		WearableBackpacks.CHANNEL.sendTo(MessageOpenGui.create(this), player);
		addListener(player);
		backpack.setPlayersUsing(backpack.getPlayersUsing() + 1);
	}
	
	
	public int getBorderTop() { return 17; }
	public int getBorderSide() { return 7; }
	public int getBorderBottom() { return 7; }
	
	/** Returns the space between container and player inventory in pixels. */
	public int getBufferInventory() { return 13; }
	/** Returns the space between player inventory and hotbar in pixels. */
	public int getBufferHotbar() { return 4; }
	
	public int getMaxColumns() { return BackpackSize.MAX.getColumns(); }
	public int getMaxRows() { return BackpackSize.MAX.getRows(); }
	
	
	public int getWidth() { return Math.max(size.getColumns(), 9) * 18 + getBorderSide() * 2; }
	public int getHeight() { return getBorderTop() + (size.getRows() * 18) +
	                                getBufferInventory() + (4 * 18) +
	                                getBufferHotbar() + getBorderBottom(); }
	
	public int getContainerInvWidth() { return size.getColumns() * 18; }
	public int getContainerInvHeight() { return size.getRows() * 18; }
	public int getContainerInvXOffset() { return getBorderSide() +
		Math.max(0, (getPlayerInvWidth() - getContainerInvWidth()) / 2); }
	
	public int getPlayerInvWidth() { return 9 * 18; }
	public int getPlayerInvHeight() { return 4 * 18 + getBufferHotbar(); }
	public int getPlayerInvXOffset() { return getBorderSide() +
		Math.max(0, (getContainerInvWidth() - getPlayerInvWidth()) / 2); }
	
	
	protected void setupSlots() {
		setupBackpackSlots();
		setupPlayerSlots();
	}
	
	protected void setupBackpackSlots() {
		int xOffset = 1 + getContainerInvXOffset();
		int yOffset = 1 + getBorderTop();
		for (int y = 0; y < size.getRows(); y++, yOffset += 18)
			for (int x = 0; x < size.getColumns(); x++)
				addSlotToContainer(new SlotItemHandler(items, x + y * size.getColumns(),
					xOffset + x * 18, yOffset));
	}
	
	protected void setupPlayerSlots() {
		int xOffset = 1 + getPlayerInvXOffset();
		int yOffset = 1 + getBorderTop() + getContainerInvHeight() + getBufferInventory();
		
		// Inventory
		for (int y = 0; y < 3; y++, yOffset += 18)
			for (int x = 0; x < 9; x++)
				addSlotToContainer(new Slot(
					player.inventory, x + y * 9 + 9,
					xOffset + x * 18, yOffset));
		
		// Hotbar
		yOffset += getBufferHotbar();
		for (int x = 0; x < 9; x++)
			addSlotToContainer(new Slot(
				player.inventory, x,
				xOffset + x * 18, yOffset));
	}
	
	
	public void writeToNBT(NBTTagCompound compound) {
		compound.setTag(TAG_SIZE, size.serializeNBT());
		compound.setString(TAG_TITLE, title);
		compound.setBoolean(TAG_LOCALIZED, titleLocalized);
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