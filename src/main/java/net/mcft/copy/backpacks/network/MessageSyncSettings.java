package net.mcft.copy.backpacks.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.config.Setting;

/** Synchronizes config settings to players joining a world. */
public class MessageSyncSettings implements IMessage {
	
	private NBTTagCompound _data;
	
	public MessageSyncSettings() {  }
	
	public static MessageSyncSettings create() {
		MessageSyncSettings message = new MessageSyncSettings();
		message._data = new NBTTagCompound();
		for (Setting<?> setting : WearableBackpacks.CONFIG.getSettings())
			if (setting.doesSync() && setting.isRequiredEnabled())
				message._data.setTag(setting.getFullName(), setting.writeSynced());
		return message;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		try { _data = buffer.readCompoundTag(); }
		catch (Exception ex) { _data = null; }
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		buffer.writeCompoundTag(_data);
	}
	
	public static class Handler extends BackpacksMessageHandler<MessageSyncSettings> {
		@Override
		public boolean isScheduled() { return false; }
		@Override
		@SideOnly(Side.CLIENT)
		public void handle(MessageSyncSettings message, MessageContext ctx) {
			for (String key : message._data.getKeySet()) {
				NBTBase tag = message._data.getTag(key);
				Setting<?> setting = WearableBackpacks.CONFIG.getSetting(key);
				if ((setting != null) && setting.doesSync())
					setting.readSynced(tag);
			}
		}
	}
	
}
