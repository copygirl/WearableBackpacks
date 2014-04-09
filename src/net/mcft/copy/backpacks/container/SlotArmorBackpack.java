package net.mcft.copy.backpacks.container;

import net.mcft.copy.backpacks.api.IBackpack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Replaces the player's regular chest armor slot to
 *  prevent them from taking out equipped backpacks. */
public class SlotArmorBackpack extends Slot {
	
	private static final int armorType = 1;
	
	public SlotArmorBackpack(IInventory inventory, int slot, int x, int y) {
		super(inventory, slot, x, y);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getBackgroundIconIndex() {
		return ItemArmor.func_94602_b(armorType);
	}
	
	@Override
	public int getSlotStackLimit() { return 1; }
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack.getItem().isValidArmor(stack, armorType, ((InventoryPlayer)inventory).player);
	}
	
	@Override
	public boolean canTakeStack(EntityPlayer player) { 
		ItemStack backpack = getStack();
		return ((backpack == null) || !(backpack.getItem() instanceof IBackpack));
	}
	
}
