package net.mcft.copy.backpacks.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.INBTSerializable;

import net.mcft.copy.backpacks.misc.util.NbtUtils;

public class BackpackSize implements INBTSerializable<NBTTagByteArray> {
	
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
		int columns, rows;
		if (tag instanceof NBTTagByteArray) {
			NBTTagByteArray array = (NBTTagByteArray)tag;
			columns = array.getByteArray()[0];
			rows    = array.getByteArray()[1];
		} else if (tag instanceof NBTTagIntArray) {
			NBTTagIntArray array = (NBTTagIntArray)tag;
			columns = array.getIntArray()[0];
			rows    = array.getIntArray()[1];
		} else if (tag instanceof NBTTagList) {
			NBTTagList list = (NBTTagList)tag;
			columns = ((NBTPrimitive)list.get(0)).getInt();
			rows    = ((NBTPrimitive)list.get(1)).getInt();
		} else throw new RuntimeException("Invalid tag type " + NBTBase.NBT_TYPES[tag.getId()]);
		return new BackpackSize(columns, rows);
	}
	
	
	@Override
	public NBTTagByteArray serializeNBT() {
		return new NBTTagByteArray(new byte[]{ (byte)_columns, (byte)_rows });
	}
	
	@Override
	public void deserializeNBT(NBTTagByteArray nbt) {
		_columns = nbt.getByteArray()[0];
		_rows    = nbt.getByteArray()[1];
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
