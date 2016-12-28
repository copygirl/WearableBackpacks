package net.mcft.copy.backpacks.misc.util;

import java.util.List;
import java.util.Random;

public final class RandomUtils {
	
	private RandomUtils() {  }
	
	
	public static final Random RAND = new Random();
	
	/** Returns a random integer between 0 (inclusive) and the specified max value (exclusive). */
	public static int getInt(int max) { return RAND.nextInt(max); }
	/** Returns a random integer between the specified min (inclusive) and max (exclusive) values. */
	public static int getInt(int min, int max) {
		return ((max > min) ? (min + getInt(max - min)) : min);
	}
	
	/** Returns a random float between 0.0 (inclusive) and 1.0 (exclusive). */
	public static float getFloat() { return RAND.nextFloat(); }
	/** Returns a random float between 0.0 (inclusive) and the specified max value (exclusive). */
	public static float getFloat(float max) { return getFloat() * max; }
	/** Returns a random float between the specified min (inclusive) and max (exclusive) values. */
	public static float getFloat(float min, float max) {
		return ((max > min) ? (min + getFloat(max - min)) : min);
	}
	
	/** Returns a random double between 0.0 (inclusive) and 1.0 (exclusive). */
	public static double getDouble() { return RAND.nextDouble(); }
	/** Returns a random double between 0.0 (inclusive) and the specified max value (exclusive). */
	public static double getDouble(double max) { return getDouble() * max; }
	/** Returns a random double between the specified min (inclusive) and max (exclusive) values. */
	public static double getDouble(double min, double max) {
		return ((max > min) ? (min + getDouble(max - min)) : min);
	}
	
	/** Returns true randomly with the specified probability, false otherwise. */
	public static boolean getBoolean(double probability) { return (getDouble() < probability); }
	
	/** Returns a random gaussian (normally) distributed value. */
	public static double getGaussian() { return RAND.nextGaussian(); }
	
	
	/** Returns a random element from the specified array or null if empty. */
	public static <T>   T getElement(      T[] array) { return ((array.length > 0) ? array[getInt(array.length)] : null); }
	/** Returns a random element from the specified array or false if empty. */
	public static boolean getElement(boolean[] array) { return ((array.length > 0) ? array[getInt(array.length)] : false); }
	/** Returns a random element from the specified array or 0 if empty. */
	public static    byte getElement(   byte[] array) { return ((array.length > 0) ? array[getInt(array.length)] : 0); }
	/** Returns a random element from the specified array or 0 if empty. */
	public static   short getElement(  short[] array) { return ((array.length > 0) ? array[getInt(array.length)] : 0); }
	/** Returns a random element from the specified array or 0 if empty. */
	public static     int getElement(    int[] array) { return ((array.length > 0) ? array[getInt(array.length)] : 0); }
	/** Returns a random element from the specified array or 0 if empty. */
	public static    long getElement(   long[] array) { return ((array.length > 0) ? array[getInt(array.length)] : 0); }
	/** Returns a random element from the specified array or 0 if empty. */
	public static   float getElement(  float[] array) { return ((array.length > 0) ? array[getInt(array.length)] : 0); }
	/** Returns a random element from the specified array or 0 if empty. */
	public static  double getElement( double[] array) { return ((array.length > 0) ? array[getInt(array.length)] : 0); }
	
	/** Returns a random element from the specified list or null if empty. */
	public static <T> T getElement(List<T> list) { return (!list.isEmpty() ? list.get(getInt(list.size())) : null); }
	
}
