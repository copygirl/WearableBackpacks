package net.mcft.copy.backpacks.item;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.mcft.copy.backpacks.item.IDyeableItem;
import net.mcft.copy.backpacks.misc.util.NbtUtils;

public class DyeWashingHandler {
	
	@SubscribeEvent
	public void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event) {
		
		if (event.getWorld().isRemote ||
		    event.getEntityPlayer().isSneaking()) return;
		
		// Check if item is washable and currently dyed.
		ItemStack stack = event.getItemStack();
		if ((stack == null) || !(stack.getItem() instanceof IDyeableItem) ||
		    !((IDyeableItem)stack.getItem()).canWash(stack) ||
		    !NbtUtils.has(stack, "display", "color")) return;
		
		// Check if block is a cauldron.
		IBlockState state = event.getWorld().getBlockState(event.getPos());
		if (!(state.getBlock() instanceof BlockCauldron)) return;
		BlockCauldron block = (BlockCauldron)state.getBlock();
		
		// Check if water is in the cauldron.
		int level = state.getValue(BlockCauldron.LEVEL);
		if (level <= 0) return;
		
		// Remove the color from the item!
		NbtUtils.remove(stack, "display", "color");
		// Use up some water from the cauldron!
		block.setWaterLevel(event.getWorld(), event.getPos(), state, level - 1);
		// Increase "armor cleaned" statistic! Wheee!
		event.getEntityPlayer().addStat(StatList.ARMOR_CLEANED);
		
		// Cancel the event, as the item / cauldron was used.
		event.setCanceled(true);
		
	}
	
}
