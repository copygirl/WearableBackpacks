package net.mcft.copy.backpacks.client;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;

import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.api.BackpackHelper;
import net.mcft.copy.backpacks.misc.util.ClientUtils;
import net.mcft.copy.backpacks.network.MessageOpenBackpack;

@SideOnly(Side.CLIENT)
public class KeyBindingHandler {
	
	public static final String CATEGORY = WearableBackpacks.MOD_NAME;
	
	public static final KeyBinding openBackpack = new KeyBinding(
		"key." + WearableBackpacks.MOD_ID + ".open",
		KeyConflictContext.IN_GAME, Keyboard.KEY_B, CATEGORY);
	
	public KeyBindingHandler() {
		ClientRegistry.registerKeyBinding(openBackpack);
	}
	
	@SubscribeEvent
	public void onKey(KeyInputEvent event) {
		if (openBackpack.isPressed() &&
		    (BackpackHelper.getBackpack(ClientUtils.getPlayer()) != null) &&
		    WearableBackpacks.CONFIG.enableSelfInteraction.get())
			WearableBackpacks.CHANNEL.sendToServer(MessageOpenBackpack.create());
	}
	
}
