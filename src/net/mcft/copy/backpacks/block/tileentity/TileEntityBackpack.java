package net.mcft.copy.backpacks.block.tileentity;

import java.util.List;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.api.IBackpackProperties;
import net.mcft.copy.backpacks.client.BackpackResources;
import net.mcft.copy.backpacks.client.model.ModelBackpack;
import net.mcft.copy.core.base.TileEntityBase;
import net.mcft.copy.core.client.Color;
import net.mcft.copy.core.client.model.CoreModelBase;
import net.mcft.copy.core.client.renderer.IModelProvider;
import net.mcft.copy.core.client.renderer.ITextureProvider;
import net.mcft.copy.core.util.DirectionUtils;
import net.mcft.copy.core.util.NbtUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityBackpack extends TileEntityBase
                                implements IBackpackProperties,
                                           IModelProvider, ITextureProvider {
	
	public static final String TAG_STACK         = "stack";
	public static final String TAG_BACKPACK_DATA = "data";
	public static final String TAG_ORIENTATION   = "orientation";
	public static final String TAG_USING         = "using";
	
	private ItemStack backpackStack = null;
	private IBackpackData backpackData = null;
	
	public ForgeDirection orientation = ForgeDirection.UNKNOWN;
	
	public int playersUsing = 0;
	public int prevLidTicks = 0;
	public int lidTicks = 0;
	
	// Loading, saving and syncing
	
	@Override
	public boolean hasDescriptionPacket() { return true; }
	
	@Override
	public void write(NBTTagCompound compound) {
		if (getBackpackStack() != null)
			compound.setTag(TAG_STACK, NbtUtils.writeItem(getBackpackStack()));
		compound.setByte(TAG_ORIENTATION, (byte)orientation.ordinal());
	}
	@Override
	public void read(NBTTagCompound compound) {
		if (compound.hasKey(TAG_STACK))
			setBackpackStack(NbtUtils.readItem(compound.getCompoundTag(TAG_STACK)));
		orientation = ForgeDirection.getOrientation(compound.getByte(TAG_ORIENTATION));
	}
	
	@Override
	public void writeToSave(NBTTagCompound compound) {
		if (getBackpackData() != null) {
			NBTTagCompound dataCompound = new NBTTagCompound();
			getBackpackData().writeToNBT(dataCompound);
			compound.setTag(TAG_BACKPACK_DATA, dataCompound);
		}
	}
	@Override
	public void readFromSave(NBTTagCompound compound) {
		if (compound.hasKey(TAG_BACKPACK_DATA) && (getBackpackStack() != null)) {
			IBackpack backpackType = BackpackHelper.getBackpackType(getBackpackStack());
			IBackpackData data = backpackType.createBackpackData();
			data.readFromNBT(compound.getCompoundTag(TAG_BACKPACK_DATA));
			setBackpackData(data);
		}
	}
	
	@Override
	public void writeToDescriptionPacket(NBTTagCompound compound) {
		compound.setBoolean(TAG_USING, (playersUsing > 0));
	}
	@Override
	public void readFromDescriptionPacket(NBTTagCompound compound) {
		playersUsing = (compound.getBoolean(TAG_USING) ? 1 : 0);
	}
	
	// TileEntityBase methods
	
	@Override
	public void onBlockPlaced(EntityLivingBase entity, ItemStack stack,
	                          ForgeDirection side, float hitX, float hitY, float hitZ) {
		orientation = DirectionUtils.getOrientation(entity).getOpposite();
		setBackpackStack(stack.copy());
		setBackpackData(getBackpackType().createBackpackData());
	}
	
	@Override
	public boolean onBlockBreak(EntityPlayer player, boolean brokenInCreative) {
		if (!player.isSneaking()) return true;
		return BackpackHelper.equipBackpack(player, this);
	}
	
	@Override
	public void onBlockDestroyed(boolean brokenInCreative) {
		if (getBackpackStack() != null)
			getBackpackType().onBlockBreak(this);
	}
	
	@Override
	public void getBlockDrops(List<ItemStack> drops, int fortune) {
		drops.clear();
		ItemStack stack = getBackpackStack();
		if (stack != null) drops.add(stack);
	}
	
	@Override
	public boolean onBlockActivated(EntityPlayer player, ForgeDirection side,
	                                float hitX, float hitY, float hitZ) {
		if (getBackpackStack() != null)
			getBackpackType().onPlacedInteract(player, this);
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void onRenderAsItem(ItemStack stack) {
		setBackpackStack(stack);
	}
	
	// TileEntity methods
	
	@Override
	public void updateEntity() {
		if (worldObj.isRemote)
			BackpackHelper.updateLidTicks(this, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5);
	}
	
	@Override
	public boolean receiveClientEvent(int event, int value) {
		if (event == 0) {
			playersUsing = value;
			return true;
		} else return false;
	}
	
	// IBackpackProperties implementation
	
	@Override
	public ItemStack getBackpackStack() { return backpackStack; }
	@Override
	public void setBackpackStack(ItemStack stack) { backpackStack = stack; }
	
	@Override
	public IBackpackData getBackpackData() { return backpackData; }
	@Override
	public void setBackpackData(IBackpackData data) { backpackData = data; }
	
	@Override
	public IBackpack getLastBackpackType() { return null; }
	@Override
	public void setLastBackpackType(IBackpack type) {  }
	
	@Override
	public int getPlayersUsing() { return playersUsing; }
	@Override
	public void setPlayersUsing(int players) {
		if ((players > 0) != (playersUsing > 0))
			sendEvent(0, (players > 0) ? 1 : 0);
		playersUsing = players;
	}
	
	@Override
	public int getLidTicks() { return lidTicks; }
	@Override
	public void setLidTicks(int ticks) {
		prevLidTicks = lidTicks;
		lidTicks = ticks;
	}
	
	// IModelProvider implementation
	
	@Override
	@SideOnly(Side.CLIENT)
	public CoreModelBase getModel() {
		IBackpack backpack = BackpackHelper.getBackpackType(getBackpackStack());
		return ((backpack != null) ? ModelBackpack.getModel(backpack.getModel(getBackpackStack())) : null);
	}
	
	// ITextureProvider implementation
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderPasses() {
		ItemStack stack = getBackpackStack();
		return ((stack != null) ? stack.getItem().getRenderPasses(stack.getItemDamage()) : 1);
	}
	@Override
	@SideOnly(Side.CLIENT)
	public Color getColor(int pass) {
		ItemStack stack = getBackpackStack();
		return ((stack != null) ? Color.fromRGB(stack.getItem().getColorFromItemStack(stack, pass)) : Color.WHITE);
	}
	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getTexture(int pass) {
		if (pass != 1) return null;
		return BackpackResources.textureBackpackOverlay;
	}
	
	// Helper methods
	
	private IBackpack getBackpackType() {
		return BackpackHelper.getBackpackType(getBackpackStack());
	}
	
}
