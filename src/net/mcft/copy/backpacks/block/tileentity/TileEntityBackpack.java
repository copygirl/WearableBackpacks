package net.mcft.copy.backpacks.block.tileentity;

import java.util.List;

import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.api.IBackpack;
import net.mcft.copy.backpacks.api.IBackpackData;
import net.mcft.copy.backpacks.api.IBackpackTileEntity;
import net.mcft.copy.backpacks.client.BackpackResources;
import net.mcft.copy.core.base.TileEntityBase;
import net.mcft.copy.core.client.Color;
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
                                implements IBackpackTileEntity, ITextureProvider {
	
	public static final String TAG_STACK         = "stack";
	public static final String TAG_BACKPACK_DATA = "data";
	public static final String TAG_ORIENTATION   = "orientation";
	
	private ItemStack backpackStack = null;
	private IBackpackData backpackData = null;
	
	public int playersUsing = 0;
	public ForgeDirection orientation = ForgeDirection.UNKNOWN;
	
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
		if (!player.isSneaking()) return false;
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
	
	// IBackpackTileEntity implementation
	
	@Override
	public ItemStack getBackpackStack() { return backpackStack; }
	@Override
	public void setBackpackStack(ItemStack stack) { backpackStack = stack; }
	@Override
	public IBackpackData getBackpackData() { return backpackData; }
	@Override
	public void setBackpackData(IBackpackData data) { backpackData = data; }
	@Override
	public boolean isUsedByPlayer() { return (playersUsing > 0); }
	
	// ITextureProvider implementation
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderPasses() {
		ItemStack stack = getBackpackStack();
		return ((stack != null) ? stack.getItem().getRenderPasses(stack.getItemDamage()) : 1);
	}
	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getTexture(int pass) {
		if (pass != 1) return null;
		return BackpackResources.textureBackpackOverlay;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public Color getColor(int pass) {
		ItemStack stack = getBackpackStack();
		return ((stack != null) ? Color.fromRGB(stack.getItem().getColorFromItemStack(stack, pass)) : Color.WHITE);
	}
	
	// Helper methods
	
	private IBackpack getBackpackType() {
		return BackpackHelper.getBackpackType(getBackpackStack());
	}
	
}
