package net.mcft.copy.backpacks.client.gui;

public abstract class Alignment {
	
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
	}
	
	public static class Center extends Alignment {  }
	
}