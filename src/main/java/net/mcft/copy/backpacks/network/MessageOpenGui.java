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

/** Causes the receiving player to open a GUI.
 *  (Currently hardcoded to GuiBackpack, though.) */
public class MessageOpenGui implements IMessage {
	
	private int _windowId;
	private NBTTagCompound _data;
	
	public MessageOpenGui() {  }
	
	public static MessageOpenGui create(ContainerBackpack container) {
		MessageOpenGui message = new MessageOpenGui();
		message._windowId = container.windowId;
		message._data = new NBTTagCompound();
		container.writeToNBT(message._data);
		return message;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		try {
			_windowId = buffer.readInt();
			_data = buffer.readCompoundTag();
		} catch (Exception ex) {
			_windowId = -1;
			_data = null;
		}
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		buffer.writeInt(_windowId);
		buffer.writeCompoundTag(_data);
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
