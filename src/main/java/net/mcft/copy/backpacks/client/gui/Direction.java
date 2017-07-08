package net.mcft.copy.backpacks.client.gui;

public enum Direction {
	
	HORIZONTAL,
	VERTICAL;
	
	public Direction perpendicular()
		{ return (this == HORIZONTAL) ? VERTICAL : HORIZONTAL; }
	
}
