package net.mcft.copy.backpacks.entity;

import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.api.IBackpackProperties;
import net.mcft.copy.core.entity.EntityPropertiesBase;
import net.mcft.copy.core.entity.EntityProperty;
import net.mcft.copy.core.entity.EntityPropertyPrimitive;
import net.mcft.copy.core.entity.EntityPropertyStack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class BackpackProperties extends EntityPropertiesBase implements IBackpackProperties {
	
	// Used by EntityUtils.getIdentifier().
	public static final String IDENTIFIER = "WearableBackpack";
	
	public final EntityProperty<Integer>       playersUsing;
	public final EntityProperty<ItemStack>     backpackStack;
	public final EntityProperty<IBackpackData> backpackData;
	public final EntityProperty<IBackpack>     backpackType;
	
	public int prevLidTicks = 0;
	public int lidTicks = 0;
	
	public BackpackProperties() {
		add(playersUsing = new EntityPropertyPrimitive<Integer>("using", 0));
		add(backpackStack = new EntityPropertyStack("stack").setSaved().setSynced(true));
		add(backpackType = new EntityPropertyBackpackType("type", this).setSaved());
		add(backpackData = new EntityPropertyBackpackData("data", this).setSaved());
	}
	
	// EntityPropertiesBase methods
	
	@Override
	public EntityLivingBase getEntity() { return (EntityLivingBase)super.getEntity(); }
	
	@Override
	public boolean isSynced() { return true; }
	
	// IBackpackProperties implementation
	
	@Override
	public ItemStack getBackpackStack() { return backpackStack.get(); }
	@Override
	public void setBackpackStack(ItemStack stack) { backpackStack.set(stack); }
	
	@Override
	public IBackpackData getBackpackData() { return backpackData.get(); }
	@Override
	public void setBackpackData(IBackpackData data) { backpackData.set(data); }
	
	@Override
	public IBackpack getLastBackpackType() { return backpackType.get(); }
	@Override
	public void setLastBackpackType(IBackpack type) { backpackType.set(type); }
	
	@Override
	public int getPlayersUsing() { return playersUsing.get(); }
	@Override
	public void setPlayersUsing(int players) { playersUsing.set(players); }
	
	@Override
	public int getLidTicks() { return lidTicks; }
	@Override
	public void setLidTicks(int ticks) {
		prevLidTicks = lidTicks;
		lidTicks = ticks;
	}
	
}
