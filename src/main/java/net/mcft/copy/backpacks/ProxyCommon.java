package net.mcft.copy.backpacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.BackpackRegistry;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.api.IBackpackType;
import net.mcft.copy.backpacks.api.BackpackRegistry.BackpackEntry;
import net.mcft.copy.backpacks.block.entity.TileEntityBackpack;
import net.mcft.copy.backpacks.container.SlotArmorBackpack;
import net.mcft.copy.backpacks.item.DyeWashingHandler;
import net.mcft.copy.backpacks.misc.BackpackCapability;
import net.mcft.copy.backpacks.misc.util.NbtUtils;
import net.mcft.copy.backpacks.misc.util.WorldUtils;
import net.mcft.copy.backpacks.network.MessageBackpackUpdate;

public class ProxyCommon {
	
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(WearableBackpacks.CONFIG);
		MinecraftForge.EVENT_BUS.register(WearableBackpacks.CONTENT);
		MinecraftForge.EVENT_BUS.register(new DyeWashingHandler());
		
		CapabilityManager.INSTANCE.register(IBackpack.class,
			new BackpackCapability.Storage(), BackpackCapability.class);
	}
	
	public void init() {  }
	
	/** Intializes the backpack layers on the client-side.
	 *  Called when spawn setting is loaded / changed. */
	public void initBackpackLayers() {  }
	
	// Attaching / sending capability
	
	@SubscribeEvent
	public void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event) {
		if (!BackpackRegistry.canEntityWearBackpacks(event.getObject())) return;
		// Give entities that can wear backpacks the backpack capability.
		event.addCapability(BackpackCapability.IDENTIFIER,
			new BackpackCapability.Provider((EntityLivingBase)event.getObject()));
	}
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event)
		{ sendBackpackStack(event.player, event.player); }
	@SubscribeEvent
	public void onPlayerChangedDimensionEvent(PlayerChangedDimensionEvent event)
		{ sendBackpackStack(event.player, event.player); }
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
		{ sendBackpackStack(event.player, event.player); }
	@SubscribeEvent
	public void onPlayerStartTracking(PlayerEvent.StartTracking event)
		{ sendBackpackStack(event.getTarget(), event.getEntityPlayer()); }
	
	private void sendBackpackStack(Entity carrier, EntityPlayer player) {
		BackpackCapability backpack = (BackpackCapability)BackpackHelper.getBackpack(carrier);
		if (backpack != null) WearableBackpacks.CHANNEL.sendTo(
			MessageBackpackUpdate.stack(carrier, backpack.stack), player);
	}
	
	// Backpack interactions / events
	
	@SubscribeEvent
	public void onCheckSpawn(CheckSpawn event) {
		// When a mob is about to spawn, see if it has a chance to wear a backpack.
		if (!(event.isSpawner() ? WearableBackpacks.CONFIG.entity.spawnFromSpawners
		                        : WearableBackpacks.CONFIG.entity.spawnNaturally).get()) return;
		EntityLivingBase entity = event.getEntityLiving();
		
		for (BackpackEntry entry : BackpackRegistry.getBackpackEntries(entity.getClass())) {
			if ((entry.chance == 0) || (entity.world.rand.nextDouble() > (1.0 / entry.chance))) continue;
			BackpackCapability backpack = (BackpackCapability)entity.getCapability(IBackpack.CAPABILITY, null);
			// Set the backpack capability of the entity to spawn with the specified backpack.
			// This will be delayed until the first update tick to fire after armor has been generated.
			backpack.spawnWith = entry;
		}
	}
	/** Called when a mob spawns with a backpack with a 1 tick delay. */
	private void onSpawnedWith(EntityLivingBase entity, BackpackCapability backpack, BackpackEntry entry) {
		ItemStack stack = new ItemStack(entry.getBackpackItem());
		
		// Set damage to a random amount (25% - 75%).
		int maxDamage = stack.getMaxDamage();
		int damage = maxDamage / 4 + ((maxDamage / 2 > 0)
			? entity.world.rand.nextInt(maxDamage / 2) : 0);
		stack.setItemDamage(damage);
		
		if (BackpackHelper.equipAsChestArmor) {
			// If the entity spawned with enchanted armor,
			// then move over all compatible enchantments.
			ItemStack armor = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
			if ((armor != null) && armor.isItemEnchanted()) {
				NBTTagList enchList = armor.getEnchantmentTagList();
				for (int i = 0; i < enchList.tagCount(); ++i) {
					NBTTagCompound enchTag = enchList.getCompoundTagAt(i);
					Enchantment enchantment = Enchantment.getEnchantmentByID(enchTag.getShort("id"));
					// If the enchantment doesn't work with the backpack, remove it.
					if (!enchantment.canApply(stack)) enchList.removeTag(i--);
				}
				if (enchList.tagCount() > 0)
					NbtUtils.set(stack, enchList, "ench");
			}
		}
		
		IBackpackType type = entry.getBackpackItem();
		IBackpackData data = type.createBackpackData(stack);
		BackpackHelper.setEquippedBackpack(entity, stack, data);
		type.onSpawnedWith(entity, backpack, entry.lootTable);
		backpack.spawnWith  = null;
		backpack.mayDespawn = true;
	}
	
	private boolean cancelOffHand = false;
	@SubscribeEvent
	public void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event) {
		
		// This event is fired twice, once for each hand. Unfortunately there
		// is no way to set the result of the main hand interaction to SUCCESS
		// so the off hand one will be skipped. So: Hacky code!
		
		if (cancelOffHand) {
			cancelOffHand = false;
			if (event.getHand() == EnumHand.OFF_HAND)
				{ event.setCanceled(true); return; }
		}
		
		// When players sneak-right-click the ground with an
		// empty hand, place down their equipped backpack.
		
		EntityPlayer player = event.getEntityPlayer();
		World world = event.getWorld();
		if (!player.isSneaking() || (event.getHand() != EnumHand.MAIN_HAND) ||
		    !player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()) return;
		
		IBackpack backpack = BackpackHelper.getBackpack(player);
		if (backpack == null) return;
		
		// Try place the equipped backpack on the ground by using it. Also takes
		// care of setting the tile entity stack and data as well as unequipping.
		// See ItemBackpack.onItemUse.
		player.inventory.mainInventory.set(player.inventory.currentItem, backpack.getStack());
		if (backpack.getStack().onItemUse(
				player, world, event.getPos(), EnumHand.MAIN_HAND,
				event.getFace(), 0.5F, 0.5F, 0.5F) == EnumActionResult.SUCCESS) {
			
			player.swingArm(EnumHand.MAIN_HAND);
			event.setCanceled(true);
			cancelOffHand = true;
			
		} else player.inventory.mainInventory.set(player.inventory.currentItem, ItemStack.EMPTY);
		
	}
	
	@SubscribeEvent
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		
		// When players right-click equipped backpacks, interact with them.
		
		if (!WearableBackpacks.CONFIG.enableEquippedInteraction.get() ||
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
		if (!player.world.isRemote && (backpack.getData() == null)) {
			IBackpackData data = type.createBackpackData(backpack.getStack());
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
		BackpackCapability backpack = (BackpackCapability)entity
			.getCapability(IBackpack.CAPABILITY, null);
		if (backpack == null) return;
		
		if (backpack.spawnWith != null)
			onSpawnedWith(entity, backpack, backpack.spawnWith);
		boolean hasBackpack = !backpack.getStack().isEmpty();
			
		if (backpack.isChestArmor()) {
			if (entity instanceof EntityPlayer)
				SlotArmorBackpack.replace((EntityPlayer)entity);
			
			if (!hasBackpack) {
				// Backpack has been removed somehow.
				backpack.getType().onFaultyRemoval(entity, backpack);
				backpack.setStack(ItemStack.EMPTY);
				return;
			}
		} else if (!hasBackpack) return;
		
		backpack.getType().onEquippedTick(entity, backpack);
		if (entity.world.isRemote)
			BackpackHelper.updateLidTicks(backpack, entity.posX, entity.posY + 1.0, entity.posZ);
		
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		
		// If an entity wearing a backpack dies, try
		// to place it as a block, or drop the items.
		
		EntityLivingBase entity = event.getEntityLiving();
		World world = entity.world;
		if (world.isRemote) return;
		
		BackpackCapability backpack = (BackpackCapability)entity
			.getCapability(IBackpack.CAPABILITY, null);
		if ((backpack == null) || backpack.getStack().isEmpty()) return;
		
		// If keep inventory is on, keep the backpack capability so we
		// can copy it over to the new player entity in onPlayerClone.
		EntityPlayer player = ((entity instanceof EntityPlayer) ? (EntityPlayer)entity : null);
		boolean keepInventory = world.getGameRules().getBoolean("keepInventory");
		if ((player != null) && keepInventory) return;
		
		// Attempt to place the backpack as a block instead of dropping the items.
		if (WearableBackpacks.CONFIG.dropAsBlockOnDeath.get()) {
			
			List<BlockCoord> coords = new ArrayList<BlockCoord>();
			for (int x = -2; x <= 2; x++)
				for (int z = -2; z <= 2; z++)
					coords.add(new BlockCoord(entity, x, z));
			
			// Try to place the backpack on the ground nearby,
			// or look for a ground above or below to place it.
			
			Collections.sort(coords, new Comparator<BlockCoord>() {
				@Override public int compare(BlockCoord o1, BlockCoord o2) {
					if (o1.distance < o2.distance) return -1;
					else if (o1.distance > o2.distance) return 1;
					else return 0;
				}
			});
			while (!coords.isEmpty()) {
				Iterator<BlockCoord> iter = coords.iterator();
				while (iter.hasNext()) {
					BlockCoord coord = iter.next();
					// Attempt to place and unequip the backpack at
					// this coordinate. If successful, we're done here.
					if (BackpackHelper.placeBackpack(world, coord, backpack.getStack(), entity, true)) {
						// TODO: I'm aware that this is not the cleanest solution.
						((TileEntityBackpack)world.getTileEntity(coord)).setPlacedOnDeath(backpack.mayDespawn);
						return;
					}
					boolean replacable = world.getBlockState(coord).getBlock().isReplaceable(world, coord);
					coord.add(0, (replacable ? -1 : 1), 0);
					coord.moved += (replacable ? 1 : 5);
					if ((coord.getY() <= 0) || (coord.getY() > world.getHeight()) ||
						(coord.moved > 24 - coord.distance * 4)) iter.remove();
				}
			}
			
		}
		
		// In the case of regular backpacks, this causes their contents to be dropped.
		backpack.getType().onDeath(entity, backpack);
		
		// Drop the backpack as an item and remove it from the entity.
		if (!backpack.getStack().isEmpty())
			WorldUtils.dropStackFromEntity(entity, backpack.getStack(), 4.0F);
		BackpackHelper.setEquippedBackpack(entity, ItemStack.EMPTY, null);
		
	}
	// Would use a method local class but "extractRangemapReplacedMain" gradle task doesn't like that.
	private static class BlockCoord extends MutableBlockPos {
		public double distance;
		public int moved = 0;
		public BlockCoord(Entity entity, int x, int z) {
			super((int)entity.posX + x, (int)entity.posY, (int)entity.posZ + z);
			distance = Math.sqrt(Math.pow(getX() + 0.5 - entity.posX, 2) +
			                     Math.pow(getY() + 0.5 - entity.posY, 2) +
			                     Math.pow(getZ() + 0.5 - entity.posZ, 2));
		}
	}
	
	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		// This comes into play when the "keepInventory" gamerule is on.
		// In that case, onLivingDeath will keep the backpack information,
		// so we can transfer it to the new player entity.
		IBackpack originalBackpack = BackpackHelper.getBackpack(event.getOriginal());
		if (originalBackpack == null) return;
		
		EntityPlayer player = event.getEntityPlayer();
		IBackpack clonedBackpack = player.getCapability(IBackpack.CAPABILITY, null);
		clonedBackpack.setStack(originalBackpack.getStack());
		clonedBackpack.setData(originalBackpack.getData());
	}
	
}
