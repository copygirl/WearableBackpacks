package net.mcft.copy.backpacks.proxy;

import java.util.Map;
import java.util.Map.Entry;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.BackpackProperties;
import net.mcft.copy.backpacks.api.BackpackRegistry;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.core.misc.EquipmentSlot;
import net.mcft.copy.core.util.EntityUtils;
import net.mcft.copy.core.util.RandomUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.SpecialSpawn;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CommonProxy {
	
	public void init() {
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
		
		// TODO: Register entities to spawn with backpacks.
		// BackpackRegistry.registerBackpackEntity(EntityZombie.class, backpack, 1.0 / 800);
		// BackpackRegistry.registerBackpackEntity(EntitySkeleton.class, backpack, 1.0 / 1200);
		// BackpackRegistry.registerBackpackEntity(EntityPigZombie.class, backpack, 1.0 / 1000);
		// BackpackRegistry.registerBackpackEntity(EntityEnderman.class, backpack, 1.0 / 80);
	}
	
	@SubscribeEvent
	public void onEntityContructing(EntityConstructing event) {
		
		if (!(event.entity instanceof EntityLivingBase)) return;
		EntityLivingBase entity = (EntityLivingBase)event.entity;
		
		// Give entities that can support backpacks the backpack properties.
		if (BackpackRegistry.canEntityWearBackpacks(entity)) return;
			EntityUtils.createProperties(entity, BackpackProperties.class);
			
	}
	
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		EntityPlayer player = event.entityPlayer;
		if ((event.action != Action.RIGHT_CLICK_BLOCK) || !player.isSneaking() ||
		    (player.getEquipmentInSlot(EquipmentSlot.HELD) != null)) return;
		
		ItemStack backpack = BackpackHelper.getEquippedBackpack(player);
		if (backpack == null) return;
		
		backpack.tryPlaceItemIntoWorld(player, player.worldObj,
		                               event.x, event.y, event.z, event.face,
		                               0.5F, 0.5F, 0.5F);
		
		if (backpack.stackSize <= 0)
			BackpackHelper.setEquippedBackpack(player, null, null);
		
	}
	
	@SubscribeEvent
	public void onEntityInteract(EntityInteractEvent event) {
		
		if (!(event.target instanceof EntityLivingBase)) return;
		EntityPlayer player = event.entityPlayer;
		EntityLivingBase target = (EntityLivingBase)event.target;
		
		ItemStack backpack = BackpackHelper.getEquippedBackpack(target);
		if ((backpack == null) || !BackpackHelper.canInteractWithEquippedBackpack(player, target)) return;
		
		// When players right click equipped backpacks, interact with them.
		IBackpack backpackType = BackpackHelper.getBackpackType(backpack);
		backpackType.onEquippedInteract(player, target);
		
	}
	
	@SubscribeEvent
	public void onSpecialSpawn(SpecialSpawn event) {
		
		EntityLivingBase entity = event.entityLiving;
		Map<Item, Double> chances = BackpackRegistry.entities.get(entity.getClass());
		if ((chances == null) || chances.isEmpty()) return;
		for (Entry<Item, Double> entry : chances.entrySet())
			if (RandomUtils.getBoolean(entry.getValue())) {
				// Spawn entity with backpack.
				ItemStack stack = new ItemStack(entry.getKey());
				IBackpack backpackType = BackpackHelper.getBackpackType(stack);
				IBackpackData data = backpackType.createBackpackData();
				BackpackHelper.setEquippedBackpack(entity, stack, data);
				backpackType.onSpawnedWith(entity);
			}
		
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		
		EntityLivingBase entity = event.entityLiving;
		EntityPlayer player = ((entity instanceof EntityPlayer)
				? (EntityPlayer)entity : null);
		
		ItemStack backpack = BackpackHelper.getEquippedBackpack(entity);
		BackpackProperties properties =
				BackpackHelper.getBackpackProperties(entity);
		
		if (backpack != null) {
			IBackpack backpackItem = BackpackHelper.getBackpackType(backpack);
			backpackItem.onEquippedTick(entity);
		} else if ((BackpackHelper.getEquippedBackpackData(entity) != null) &&
		           (properties.lastBackpackType != null)) {
			// Backpack has been removed somehow.
			properties.lastBackpackType.onFaultyRemoval(entity);
			properties.lastBackpackType = null;
		}
		
	}
	
}
