package net.mcft.copy.backpacks.container;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.api.IBackpackType;

/** Wraps around the regular player armor slot to
 *  prevent equipped backpacks from being taken out. */
public class SlotBackpackWrapper extends Slot {
	
	public final Slot base;
	
	public SlotBackpackWrapper(Slot base) {
		super(base.inventory, base.getSlotIndex(), base.xPos, base.yPos);
		this.base = base;
	}
	
	/** When a backpack is equipped, go through the currently open container, see if
	 *  any slot contains the player's equipped backpack. If so, replace that slot with
	 *  a wrapper that prevents the backpack from being unequipped though normal means. */
	public static void replace(EntityPlayer player, ItemStack backpack) {
		Container container = player.openContainer;
		if (container == null) return;
		for (Slot slot : container.inventorySlots) {
			if (slot.getStack() != backpack) continue;
			if (slot instanceof SlotBackpackWrapper) continue;
			
			Slot newSlot = new SlotBackpackWrapper(slot);
			newSlot.slotNumber = slot.slotNumber;
			container.inventorySlots.set(slot.slotNumber, newSlot);
			// Keep going, there may be more slots to fix!
		}
	}
	
	@Override
	public boolean canTakeStack(EntityPlayer player) {
		return base.canTakeStack(player)
			&& !(getStack().getItem() instanceof IBackpackType);
		// Why, Mojang, whhhyyyyy??! Still no Item.canEquip / canUnequip!?
	}
	
	
	@Override
	public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack)
		{ return base.onTake(thePlayer, stack); }
	@Override
	public boolean isItemValid(ItemStack stack)
		{ return base.isItemValid(stack); }
	@Override
	public void putStack(ItemStack stack)
		{ base.putStack(stack); }
	@Override
	public void onSlotChanged()
		{ base.onSlotChanged(); }
	@Override
	public int getSlotStackLimit()
		{ return base.getSlotStackLimit(); }
	@Override
	public int getItemStackLimit(ItemStack stack)
		{ return base.getItemStackLimit(stack); }
	@Override
	public ItemStack decrStackSize(int amount)
		{ return base.decrStackSize(amount); }
	
	@Override
	@Nullable
	@SideOnly(Side.CLIENT)
	public String getSlotTexture()
		{ return base.getSlotTexture(); }
	
	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getBackgroundLocation()
		{ return base.getBackgroundLocation(); }
	
}