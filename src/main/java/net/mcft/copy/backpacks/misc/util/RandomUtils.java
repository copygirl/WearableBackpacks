package net.mcft.copy.backpacks.misc.util;

import java.util.List;
import java.util.Random;

public final class RandomUtils {
	
	private RandomUtils() {  }
	
	public static final Random RAND = new Random();
	
	public static int getInt(int max) { return RAND.nextInt(max); }
	public static int getInt(int min, int max) {
		return ((max > min) ? (min + getInt(max - min)) : min);
	}
	
	public static float getFloat() { return RAND.nextFloat(); }
	public static float getFloat(float max) { return getFloat() * max; }
	public static float getFloat(float min, float max) {
		return ((max > min) ? (min + getFloat(max - min)) : min);
	}
	
	public static double getDouble() { return RAND.nextDouble(); }
	public static double getDouble(double max) { return getDouble() * max; }
	public static double getDouble(double min, double max) {
		return ((max > min) ? (min + getDouble(max - min)) : min);
	}
	
	public static boolean getBoolean(double probability) { return (getDouble() < probability); }
	
	public static double getGaussian() { return RAND.nextGaussian(); }
	
	
	public static <T>   T getElement(      T[] array) { return ((array.length > 0) ? array[getInt(array.length)] : null); }
	public static boolean getElement(boolean[] array) { return ((array.length > 0) ? array[getInt(array.length)] : false); }
	public static    byte getElement(   byte[] array) { return ((array.length > 0) ? array[getInt(array.length)] : 0); }
	public static   short getElement(  short[] array) { return ((array.length > 0) ? array[getInt(array.length)] : 0); }
	public static     int getElement(    int[] array) { return ((array.length > 0) ? array[getInt(array.length)] : 0); }
	public static    long getElement(   long[] array) { return ((array.length > 0) ? array[getInt(array.length)] : 0); }
	public static   float getElement(  float[] array) { return ((array.length > 0) ? array[getInt(array.length)] : 0); }
	public static  double getElement( double[] array) { return ((array.length > 0) ? array[getInt(array.length)] : 0); }
	
	public static <T> T getElement (List<T> list) { return (!list.isEmpty() ? list.get(getInt(list.size())) : null); }
	
}
