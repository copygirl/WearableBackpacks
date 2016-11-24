package net.mcft.copy.backpacks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.BackpackRegistry;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.api.IBackpackType;
import net.mcft.copy.backpacks.container.SlotArmorBackpack;
import net.mcft.copy.backpacks.misc.BackpackCapability;
import net.mcft.copy.backpacks.network.MessageUpdateStack;

public class ProxyCommon {
	
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
		
		CapabilityManager.INSTANCE.register(IBackpack.class,
			new BackpackCapability.Storage(), BackpackCapability.class);
	}
	
	public void init() {  }
	
	// Attaching / sending capability
	
	@SubscribeEvent
	public void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event) {
		// Give entities that can wear backpacks the backpack capability.
		if (BackpackRegistry.canEntityWearBackpacks(event.getObject()))
			event.addCapability(BackpackCapability.IDENTIFIER,
				new BackpackCapability.Provider((EntityLivingBase)event.getObject()));
	}
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event) {
		sendBackpackStack(event.player, event.player);
	}
	@SubscribeEvent
	public void onPlayerChangedDimensionEvent(PlayerChangedDimensionEvent event) {
		sendBackpackStack(event.player, event.player);
	}
	@SubscribeEvent
	public void onPlayerStartTracking(PlayerEvent.StartTracking event) {
		sendBackpackStack(event.getTarget(), event.getEntityPlayer());
	}
	
	private void sendBackpackStack(Entity carrier, EntityPlayer player) {
		BackpackCapability backpack = (BackpackCapability)BackpackHelper.getBackpack(carrier);
		if (backpack != null) WearableBackpacks.CHANNEL.sendTo(
			new MessageUpdateStack(carrier, backpack.stack), player);
	}
	
	// Backpack interactions / events
	
	// TODO: Implement backpack dropping as block on death.
	// TODO: Implement "drop as block on death" option.
	//       (Unequip backpack automatically and don't call IBackpackType.onDeath?)
	
	@SubscribeEvent
	public void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event) {
		
		// When players sneak-right-click the ground with an
		// empty hand, place down their equipped backpack.
		
		EntityPlayer player = event.getEntityPlayer();
		World world = event.getWorld();
		if (!player.isSneaking() || (player.getHeldItemMainhand() != null)) return;
		
		IBackpack backpack = BackpackHelper.getBackpack(player);
		if (backpack == null) return;
		
		if (event.getHand() == EnumHand.MAIN_HAND) {
			// Since cancelling the event will not prevent the RightClickBlock
			// event for the other hand to be fired, we need to cancel this one
			// first and wait for the OFF_HAND one to actually do the unequipping,
			// so we can also cancel that. Otherwise, the OFF_HAND interaction
			// would cause items to be used or blocks being activated.
			event.setCanceled(true);
			return;
		}
		
		// Try place the equipped backpack on the ground by using it. Also takes
		// care of setting the tile entity stack and data as well as unequipping.
		// See ItemBackpack.onItemUse.
		if (backpack.getStack().onItemUse(
				player, world, event.getPos(), null,
				event.getFace(), 0.5F, 0.5F, 0.5F) == EnumActionResult.SUCCESS) {
			player.swingArm(EnumHand.MAIN_HAND);
			event.setCanceled(true);
		}
		
	}
	
	@SubscribeEvent
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		
		// When players right-click equipped backpacks, interact with them.
		
		if (!WearableBackpacks.CONFIG.enableEquippedInteraction.getValue() ||
		    !(event.getTarget() instanceof EntityLivingBase)) return;
		EntityPlayer player = event.getEntityPlayer();
		EntityLivingBase target = (EntityLivingBase)event.getTarget();
		
		BackpackCapability backpack = (BackpackCapability)target.getCapability(IBackpack.CAPABILITY, null);
		if ((backpack == null) || !BackpackHelper.canInteractWithEquippedBackpack(player, target)) return;
		
		IBackpackType type = backpack.getType();
		if (type == null) {
			WearableBackpacks.LOG.error("Backpack type was null when accessing equipped backpack");
			return;
		}
		if (!player.worldObj.isRemote && (backpack.getData() == null)) {
			IBackpackData data = type.createBackpackData();
			if (data != null) {
				// Only show this error message if the backpack type is supposed to have backpack data.
				// Some backpacks might not need any to function, for example an ender backpack.
				WearableBackpacks.LOG.error("Backpack data was null when accessing equipped backpack");
				backpack.setData(data);
			}
		}
		type.onEquippedInteract(player, target, backpack);
		
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		
		// Update equipped backpacks and check
		// if they've been removed somehow.
		
		EntityLivingBase entity = event.getEntityLiving();
		if (!BackpackRegistry.canEntityWearBackpacks(entity)) return;
		
		BackpackCapability backpack = (BackpackCapability)entity
			.getCapability(IBackpack.CAPABILITY, null);
		if (backpack == null) return;
		
		if (backpack.isChestArmor()) {
			if (entity instanceof EntityPlayer)
				SlotArmorBackpack.replace((EntityPlayer)entity);
			
			if (backpack.getStack() == null) {
				// Backpack has been removed somehow.
				backpack.getType().onFaultyRemoval(entity, backpack);
				backpack.setStack(null);
			}
		}
		
		if (backpack.getStack() != null) {
			backpack.getType().onEquippedTick(entity, backpack);
			
			if (entity.worldObj.isRemote)
				BackpackHelper.updateLidTicks(backpack, entity.posX, entity.posY + 1.0, entity.posZ);
		}
		
	}
	
}
