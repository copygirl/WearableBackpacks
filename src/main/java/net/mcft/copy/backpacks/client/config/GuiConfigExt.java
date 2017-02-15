package net.mcft.copy.backpacks.client.config;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Patches GuiConfigExt to allow for variable height slots in its entries list. */
// TODO: Currently only works with a single variable height entry at the end, probably.
@SideOnly(Side.CLIENT)
public class GuiConfigExt extends GuiConfig {
	
	public GuiConfigExt(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, 
	                    boolean allRequireWorldRestart, boolean allRequireMcRestart,
	                    String title, String titleLine2) {
		super(parentScreen, configElements, modID,
		      allRequireWorldRestart, allRequireMcRestart,
		      title, titleLine2);
		entryList = new Entries(this, mc);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		if (!(entryList instanceof Entries))
			entryList = new Entries(this, mc).init();
	}
	
	private static class Entries extends GuiConfigEntries {
		
		public Entries(GuiConfig parent, Minecraft mc) { super(parent, mc); }
		
		public Entries init() { super.initGui(); return this; }
		
		private int getHeightForSlot(int slot) {
			IConfigEntry entry = getListEntry(slot);
			return ((entry instanceof IVarHeightEntry) ?
				((IVarHeightEntry)entry).getSlotHeight() : getSlotHeight());
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
			for (int slot = 0; slot < getSize(); slot++) height += getHeightForSlot(slot);
			return height;
		}
		
	}
	
	public interface IVarHeightEntry {
		int getSlotHeight();
	}
	
}
