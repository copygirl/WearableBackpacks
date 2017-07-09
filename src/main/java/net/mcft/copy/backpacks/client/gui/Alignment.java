package net.mcft.copy.backpacks.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class Alignment {
	
	/** Returns whether an element with this alignment can
	 *  set its size depending on its child elements. */
	public boolean canExpand() { return true; }
	
	public static class Min extends Alignment {
		public final int min;
		public Min(int min) { this.min = min; }
	}
	
	public static class Max extends Alignment {
		public final int max;
		public Max(int max) { this.max = max; }
	}
	
	public static class Both extends Alignment {
		public final int min, max;
		public Both(int min, int max) { this.min = min; this.max = max; }
		@Override public boolean canExpand() { return false; }
	}
	
	public static class Center extends Alignment {  }
	
}