package net.mcft.copy.backpacks.client.gui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum Direction {
	
	HORIZONTAL,
	VERTICAL;
	
	public Direction perpendicular()
		{ return (this == HORIZONTAL) ? VERTICAL : HORIZONTAL; }
	
}
