package net.mcft.copy.backpacks.item;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.misc.BackpackDataItems;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;

public class ItemBackpack extends ItemBlock implements IBackpack, ISpecialArmor {
	
	public ItemBackpack(Block block) {
		super(block);
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player,
	                         World world, int x, int y, int z, int side,
	                         float hitX, float hitY, float hitZ) {
		
		return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
		
	}
	
	// IBackpack implementation
	
	@Override
	public void onSpawnedWith(EntityLivingBase entity) {
		// TODO: Fill backpack with random items.
	}
	
	@Override
	public void onEquip(EntityPlayer player, TileEntity tileEntity) {  }
	
	@Override
	public void onUnequip(EntityPlayer player, TileEntity tileEntity) {  }
	
	@Override
	public void onPlacedInteract(EntityPlayer player, TileEntity target) {
		
	}
	
	@Override
	public void onEquippedInteract(EntityPlayer player, EntityLivingBase target) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onEquippedTick(EntityLivingBase entity) {  }
	
	@Override
	public void onDeath(EntityLivingBase entity) {
		IBackpackData data = BackpackHelper.getEquippedBackpackData(entity);
		if ((data == null) || !(data instanceof BackpackDataItems)) return;
		BackpackDataItems dataItems = (BackpackDataItems)data;
		// Drop items
	}

	@Override
	public void onFaultyRemoval(EntityLivingBase entity) {
		onDeath(entity);
	}
	
	@Override
	public IBackpackData createBackpackData() {
		return new BackpackDataItems(36); // TODO: Get backpack size.
	}
	
	// ISpecialArmor implementation
	
	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) { return 2; }
	
	@Override
	public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor,
	                                     DamageSource source, double damage, int slot) {
		return new ArmorProperties(0, 12.5, armor.getMaxDamage() + 1 - armor.getItemDamage());
	}
	
	@Override
	public void damageArmor(EntityLivingBase entity, ItemStack armor,
	                        DamageSource source, int damage, int slot) {
		armor.damageItem(damage, entity);
	}
	
}
