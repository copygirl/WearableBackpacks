package net.mcft.copy.backpacks.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.GuiBackpack;
import net.mcft.copy.backpacks.container.ContainerBackpack;

public class MessageOpenGui implements IMessage {
	
	private int _windowId;
	private NBTTagCompound _data;
	
	public MessageOpenGui() {  }
	
	public MessageOpenGui(ContainerBackpack container) {
		_windowId = container.windowId;
		_data = new NBTTagCompound();
		container.writeToNBT(_data);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		try {
			_windowId = buffer.readInt();
			_data = buffer.readNBTTagCompoundFromBuffer();
		} catch (Exception ex) {
			_windowId = -1;
			_data = null;
		}
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		buffer.writeInt(_windowId);
		buffer.writeNBTTagCompoundToBuffer(_data);
	}
	
	public static class Handler extends BackpacksMessageHandler<MessageOpenGui> {
		@Override
		@SideOnly(Side.CLIENT)
		public void handle(MessageOpenGui message, MessageContext ctx) {
			ContainerBackpack container = new ContainerBackpack(getPlayer(ctx), message._data) {
				@Override public boolean canInteractWith(EntityPlayer player) { return true; } };
			container.windowId = message._windowId;
			Minecraft.getMinecraft().displayGuiScreen(new GuiBackpack(container));
		}
	}
	
}
