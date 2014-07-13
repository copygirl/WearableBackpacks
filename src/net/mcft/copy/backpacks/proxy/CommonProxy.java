package net.mcft.copy.backpacks.proxy;

import java.util.Map;
import java.util.Map.Entry;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.BackpackRegistry;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.api.IBackpackProperties;
import net.mcft.copy.backpacks.container.SlotArmorBackpack;
import net.mcft.copy.backpacks.entity.BackpackProperties;
import net.mcft.copy.core.container.ContainerBase;
import net.mcft.copy.core.container.ContainerRegistry;
import net.mcft.copy.core.misc.BlockLocation;
import net.mcft.copy.core.misc.EquipmentSlot;
import net.mcft.copy.core.util.EntityUtils;
import net.mcft.copy.core.util.RandomUtils;
import net.mcft.copy.core.util.StackUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
		
		ContainerRegistry.register(ContainerBase.class);
		
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
		if (BackpackRegistry.canEntityWearBackpacks(entity))
			EntityUtils.createProperties(entity, BackpackProperties.class);
		
	}
	
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		// When players sneak-right-click the ground with an
		// empty hand, place down their equipped backpack.
		
		EntityPlayer player = event.entityPlayer;
		if ((event.action != Action.RIGHT_CLICK_BLOCK) || !player.isSneaking() ||
		    (player.getEquipmentInSlot(EquipmentSlot.HELD) != null)) return;
		
		ItemStack backpack = BackpackHelper.getEquippedBackpack(player);
		if (backpack == null) return;
		
		backpack.tryPlaceItemIntoWorld(player, player.worldObj,
		                               event.x, event.y, event.z, event.face,
		                               0.5F, 0.5F, 0.5F);
		
		if (backpack.stackSize <= 0) {
			BlockLocation block = BlockLocation.get(player.worldObj, event.x, event.y + 1, event.z);
			TileEntity tileEntity = block.getTileEntity();
			if (tileEntity instanceof IBackpackProperties) {
				
				IBackpack backpackType = BackpackHelper.getBackpackType(backpack);
				IBackpackData backpackData = BackpackHelper.getEquippedBackpackData(player);
				if ((backpackData == null) && !player.worldObj.isRemote) {
					WearableBackpacks.log.error("Backpack data was null when placing down backpack");
					backpackData = backpackType.createBackpackData();
				}
				
				IBackpackProperties backpackTileEntity = (IBackpackProperties)tileEntity;
				backpackTileEntity.setBackpackStack(StackUtils.copy(backpack, 1));
				backpackTileEntity.setBackpackData(backpackData);
				
				backpackType.onUnequip(player, cast(tileEntity));
				
				if (!player.worldObj.isRemote) {
					BackpackHelper.setEquippedBackpack(player, null, null);
					player.inventoryContainer.detectAndSendChanges();
					// TODO: Sync backpack across players.
				}
				
			} else WearableBackpacks.log.error("TileEntity at {} is not an IBackpackTileEntity", block);
		} else {
			// TODO: Sync backpack to player, make sure e
			//       thinks e still has a backpack equipped.
		}
		
	}
	/** Just a helper method to get the TileEntity to also be an IBackpackProperties. */
	private static <T extends TileEntity & IBackpackProperties> T cast(TileEntity tileEntity) { return (T)tileEntity; }
	
	@SubscribeEvent
	public void onEntityInteract(EntityInteractEvent event) {
		
		// When players right click equipped backpacks, interact with them.
		
		if (!(event.target instanceof EntityLivingBase)) return;
		EntityPlayer player = event.entityPlayer;
		EntityLivingBase target = (EntityLivingBase)event.target;
		
		ItemStack backpack = BackpackHelper.getEquippedBackpack(target);
		if ((backpack == null) || !BackpackHelper.canInteractWithEquippedBackpack(player, target)) return;
		
		IBackpack backpackType = BackpackHelper.getBackpackType(backpack);
		IBackpackProperties properties = BackpackHelper.getBackpackProperties(target);
		if ((properties.getBackpackData() == null) && !player.worldObj.isRemote) {
			WearableBackpacks.log.error("Backpack data was null when placing accessing equipped backpack");
			properties.setBackpackData(backpackType.createBackpackData());
		}
		backpackType.onEquippedInteract(player, target);
		
	}
	
	@SubscribeEvent
	public void onSpecialSpawn(SpecialSpawn event) {
		
		// When an entity spawns naturally, check to see
		// if it should spawn with a backpack.
		
		EntityLivingBase entity = event.entityLiving;
		Map<Item, Double> chances = BackpackRegistry.entities.get(entity.getClass());
		if ((chances == null) || chances.isEmpty()) return;
		for (Entry<Item, Double> entry : chances.entrySet())
			if (RandomUtils.getBoolean(entry.getValue())) {
				ItemStack stack = new ItemStack(entry.getKey());
				IBackpack backpackType = BackpackHelper.getBackpackType(stack);
				IBackpackData data = backpackType.createBackpackData();
				BackpackHelper.setEquippedBackpack(entity, stack, data);
				backpackType.onSpawnedWith(entity);
			}
		
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		
		// Update equipped backpacks and check
		// if they've been removed somehow.
		
		EntityLivingBase entity = event.entityLiving;
		if (!BackpackRegistry.canEntityWearBackpacks(entity)) return;
		EntityPlayer player = ((entity instanceof EntityPlayer) ? (EntityPlayer)entity : null);
		
		ItemStack backpack = BackpackHelper.getEquippedBackpack(entity);
		BackpackProperties properties =
				(BackpackProperties)BackpackHelper.getBackpackProperties(entity);
		properties.update();
		
		if (backpack != null) {
			IBackpack backpackItem = BackpackHelper.getBackpackType(backpack);
			backpackItem.onEquippedTick(entity);
			replaceChestArmorSlot(player, backpack);
			if (entity.worldObj.isRemote)
				BackpackHelper.updateLidTicks(properties, entity.posX, entity.posY + 1.0, entity.posZ);
		} else if ((BackpackHelper.getEquippedBackpackData(entity) != null) &&
		           (properties.getLastBackpackType() != null)) {
			// Backpack has been removed somehow.
			properties.getLastBackpackType().onFaultyRemoval(entity);
			properties.setLastBackpackType(null);
		}
		
	}
	
	/** Replaces the chest armor slot with one that prevents
	 *  backpacks from being taken out, if necessary. */
	private static void replaceChestArmorSlot(EntityPlayer player, ItemStack backpack) {
		if ((player == null) || (player.getEquipmentInSlot(EquipmentSlot.CHEST) != backpack)) return;
		Slot slot = player.inventoryContainer.getSlot(6);
		if (slot instanceof SlotArmorBackpack) return;
		Slot newSlot = new SlotArmorBackpack(slot.inventory, slot.getSlotIndex(),
		                                     slot.xDisplayPosition, slot.yDisplayPosition);
		newSlot.slotNumber = slot.slotNumber;
		player.inventoryContainer.inventorySlots.set(slot.slotNumber, newSlot);
	}
	
}
