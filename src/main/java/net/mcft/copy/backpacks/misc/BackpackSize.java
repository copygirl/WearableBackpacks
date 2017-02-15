package net.mcft.copy.backpacks.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.INBTSerializable;

import net.mcft.copy.backpacks.misc.util.NbtUtils;

public class BackpackSize implements INBTSerializable<NBTTagList> {
	
	private static final Pattern PATTERN = Pattern.compile("^\\[([1-9]\\d?)x([1-9])\\]$");
	
	public static final BackpackSize MIN = new BackpackSize(1, 1);
	public static final BackpackSize MAX = new BackpackSize(17, 6);
	
	private int _columns = -1;
	private int _rows    = -1;
	
	public int getColumns() { return _columns; }
	public int getRows() { return _rows; }
	
	public BackpackSize() {  }
	public BackpackSize(int columns, int rows) {
		_columns = columns;
		_rows    = rows;
	}
	
	public static BackpackSize parse(String str) {
		Matcher matcher = PATTERN.matcher(str);
		if (!matcher.matches()) throw new RuntimeException(
			"Invalid backpack size value '" + str + "'");
		int columns = Integer.parseInt(matcher.group(1));
		int rows    = Integer.parseInt(matcher.group(2));
		if ((columns > MAX.getColumns()) || (rows > MAX.getRows())) throw new RuntimeException(
			"Backpack size value " + str + " over maximum (" + BackpackSize.MAX + ")");
		return new BackpackSize(columns, rows);
	}
	
	public static BackpackSize parse(NBTBase tag) {
		BackpackSize size = new BackpackSize();
		size.deserializeNBT((NBTTagList)tag);
		return size;
	}
	
	
	@Override
	public NBTTagList serializeNBT() {
		return NbtUtils.createList((byte)_columns, (byte)_rows);
	}
	
	@Override
	public void deserializeNBT(NBTTagList nbt) {
		if (nbt.tagCount() != 2) throw new RuntimeException(
			"Invalid number of tags in list (" + nbt.tagCount() + " given, 2 required");
		// Cast to NBTPrimitive to allow list to be specified using [X,Y] instead of [Xb,Yb].
		int columns = ((NBTPrimitive)nbt.get(0)).getInt();
		int rows    = ((NBTPrimitive)nbt.get(1)).getInt();
		if ((columns < MIN.getColumns()) || (rows < MIN.getRows())) throw new RuntimeException(
			"Backpack size [" + columns + "x" + rows + "] under minimum (" + BackpackSize.MIN + ")");
		if ((columns > MAX.getColumns()) || (rows > MAX.getRows())) throw new RuntimeException(
			"Backpack size [" + columns + "x" + rows + "] over maximum (" + BackpackSize.MAX + ")");
		_columns = columns;
		_rows    = rows;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BackpackSize)) return false;
		if (obj == this) return true;
		BackpackSize size = (BackpackSize)obj;
		return (size._columns == _columns) && (size._rows == _rows);
	}
	
	@Override
	public String toString() { return "[" + _columns + "x" + _rows + "]"; }
	
}
