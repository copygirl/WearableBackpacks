package net.mcft.copy.backpacks.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.nbt.NBTBase;


import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.INBTSerializable;

import net.mcft.copy.backpacks.misc.util.NbtUtils;

public class BackpackSize implements INBTSerializable<NBTTagList> {
	
	private static final Pattern PATTERN = Pattern.compile("^\\[([1-9]\\d?)x([1-9])\\]$");
	
	public static final BackpackSize MIN = new BackpackSize(1, 1);
	public static final BackpackSize MAX = new BackpackSize(17, 6);
	
	public int columns = -1;
	public int rows    = -1;
	
	public BackpackSize() {  }
	public BackpackSize(int columns, int rows) {
		this.columns = columns;
		this.rows    = rows;
	}
	public BackpackSize(NBTBase tag) {
		deserializeNBT((NBTTagList)tag);
	}
	
	public static BackpackSize parse(String str) {
		Matcher matcher = PATTERN.matcher(str);
		if (!matcher.matches()) throw new RuntimeException(
			"Invalid backpack size value '" + str + "'");
		int columns = Integer.parseInt(matcher.group(1));
		int rows    = Integer.parseInt(matcher.group(2));
		if ((columns > MAX.columns) || (rows > MAX.rows)) throw new RuntimeException(
			"Backpack size value '" + str + "' over maximum (" + BackpackSize.MAX + ")");
		return new BackpackSize(columns, rows);
	}
	
	public BackpackSize copy() { return new BackpackSize(columns, rows); }
	
	
	@Override
	public NBTTagList serializeNBT() {
		return NbtUtils.createList((byte)columns, (byte)rows);
	}
	
	@Override
	public void deserializeNBT(NBTTagList nbt) {
		columns = ((NBTTagByte)nbt.get(0)).getByte();
		rows    = ((NBTTagByte)nbt.get(1)).getByte();
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BackpackSize)) return false;
		if (obj == this) return true;
		BackpackSize size = (BackpackSize)obj;
		return (size.columns == columns) && (size.rows == rows);
	}
	
	@Override
	public String toString() { return "[" + columns + "x" + rows + "]"; }
	
}
