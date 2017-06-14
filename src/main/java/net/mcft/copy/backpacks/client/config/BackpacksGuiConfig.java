package net.mcft.copy.backpacks.client.config;

import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.config.EntryCategory;
import net.mcft.copy.backpacks.client.config.EntrySetting;
import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.SettingSingleValue;

@SideOnly(Side.CLIENT)
public class BackpacksGuiConfig extends GuiConfig {
	
	public final String category;
	
	public BackpacksGuiConfig(GuiScreen parent) {
		this(parent, Configuration.CATEGORY_GENERAL, WearableBackpacks.MOD_ID,
		     false, false, WearableBackpacks.MOD_NAME, "");
	}
	public BackpacksGuiConfig(GuiScreen parentScreen, String category, String modID, 
	                          boolean allRequireWorldRestart, boolean allRequireMcRestart,
	                          String title, String titleLine2) {
		super(parentScreen, Collections.emptyList(), modID,
		      allRequireWorldRestart, allRequireMcRestart,
		      title, titleLine2);
		this.category = category;
		initGui();
	}
	
	@Override
	public void initGui() {
		super.initGui();
		if (!(entryList instanceof Entries))
			entryList = new Entries(this);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		// If the main backpacks config screen is closed,
		// reset the entry fields on the setting instances.
		if ((button.id == 2000) && !(this.parentScreen instanceof GuiConfig))
			WearableBackpacks.CONFIG.getSettings().forEach(Setting::resetEntry);
	}
	
	/** Custom GuiConfigEntries class which generates entries directly from the
	 *  parent BackpacksGuiConfig category without going through IConfigElement.
	 *  Also provides patches to allow for custom height entry slots. */
	// TODO: Currently only works if there's only one custom sized entry at the end of the list.
	private static class Entries extends GuiConfigEntries {
		
		public Entries(BackpacksGuiConfig parent) {
			super(parent, parent.mc);
			listEntries = new ArrayList<IConfigEntry>();
			
			for (Setting<?> setting : WearableBackpacks.CONFIG.getSettings(parent.category))
				listEntries.add(EntrySetting.Create(owningScreen, this, (SettingSingleValue<?>)setting));
			
			// If this is the general category, add category elements
			// leading to config sub-screens for all other categories.
			if (parent.category.equals(Configuration.CATEGORY_GENERAL))
				for (String cat : WearableBackpacks.CONFIG.getCategoryNames())
					if (!cat.equals(Configuration.CATEGORY_GENERAL))
						listEntries.add(new EntryCategory(owningScreen, this, cat));
			
			super.initGui();
		}
		
		private int getHeightForSlot(int slot) {
			IConfigEntry entry = getListEntry(slot);
			return ((entry instanceof ISlotCustomHeight) ?
				((ISlotCustomHeight)entry).getSlotHeight() : getSlotHeight());
		}
		
		@Override
		public int getSlotIndexFromScreenCoords(int posX, int posY) {
			if ((posX < left + width / 2 - getListWidth() / 2) || (posX > getScrollBarX())) return -1;
			int y = posY - top - headerPadding + (int)amountScrolled - 4;
			for (int slot = 0; slot < getSize(); slot++) {
				int slotHeight = getHeightForSlot(slot);
				if (y < slotHeight) return slot;
				y -= slotHeight;
			}
			return -1;
		}
		
		@Override
		protected int getContentHeight() {
			int height = headerPadding;
			for (int slot = 0; slot < getSize(); slot++)
				height += getHeightForSlot(slot);
			return height;
		}
		
	}
	
}
