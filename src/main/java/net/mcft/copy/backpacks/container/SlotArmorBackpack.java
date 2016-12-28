package net.mcft.copy.backpacks.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.api.IBackpackType;

// TODO: Open backpack when interacting with slot?
// FIXME: In 1.11, is it possible to do without this hack?
/** Replaces the player's regular chest armor slot to
 *  prevent them from taking out equipped backpacks. */
public class SlotArmorBackpack extends Slot {
	
	public SlotArmorBackpack(IInventory inventory, int slot, int x, int y) {
		super(inventory, slot, x, y);
	}
	
	/** Replaces the chest armor slot of the player with one that
	 *  prevents backpacks from being taken out, if necessary. */
	public static void replace(EntityPlayer player) {
		Slot slot = player.inventoryContainer.getSlot(6);
		if (slot instanceof SlotArmorBackpack) return;
		Slot newSlot = new SlotArmorBackpack(slot.inventory, slot.getSlotIndex(),
		                                     slot.xDisplayPosition, slot.yDisplayPosition);
		newSlot.slotNumber = slot.slotNumber;
		player.inventoryContainer.inventorySlots.set(slot.slotNumber, newSlot);
	}
	
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getSlotTexture() {
		return ItemArmor.EMPTY_SLOT_NAMES[EntityEquipmentSlot.CHEST.getIndex()];
	}
	
	@Override
	public int getSlotStackLimit() { return 1; }
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack.getItem().isValidArmor(stack, EntityEquipmentSlot.CHEST, ((InventoryPlayer)inventory).player);
	}
	
	@Override
	public boolean canTakeStack(EntityPlayer player) { 
		ItemStack backpack = getStack();
		return ((backpack == null) || !(backpack.getItem() instanceof IBackpackType));
	}
	
}