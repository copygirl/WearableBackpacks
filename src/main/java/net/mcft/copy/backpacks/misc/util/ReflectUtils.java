package net.mcft.copy.backpacks.misc.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

public final class ReflectUtils {
	
	private ReflectUtils() {  }
	
	
	// Fields
	
	private static Map<Pair<Class<?>, String>, Field> _cachedFields =
		new HashMap<Pair<Class<?>, String>, Field>();
	
	/** Returns a field on the specified class with the specified SRG
	 *  or MCP names. The field is cached, speeding up future calls. */
	public static Field findField(Class<?> clazz, String mcpName, String srgName) {
		return _cachedFields.computeIfAbsent(Pair.of(clazz, mcpName), key -> {
			Field field = null;
			for (String name : new String[]{ srgName, mcpName }) {
				// Attempt to find the field on the class.
				try { field = clazz.getDeclaredField(name); }
				catch (NoSuchFieldException ex) {  }
				// If not found, try next name or exit the loop.
				if (field == null) continue;
				// Set field to be accessible, allowing its value to be get/set.
				try { field.setAccessible(true); }
				catch (SecurityException ex) { throw new RuntimeException(ex); }
				return field;
			}
			throw new RuntimeException(String.format("Could not find field '%s'/'%s' for class %s",
			                                         mcpName, srgName, clazz.getName()));
		});
	}
	
	/** Gets the value of the field with the specified SRG or MCP names
	 *  on the specified class and instance (may be null if static). */
	@SuppressWarnings("unchecked")
	public static <T, V> V get(Class<? extends T> clazz, T instance, String mcpName, String srgName) {
		try { return (V)findField(clazz, mcpName, srgName).get(instance); }
		catch (ReflectiveOperationException ex) { throw new RuntimeException(ex); }
	}
	/** Gets the value of the static field with the specified SRG or MCP names on the specified class. */
	public static <T, V> V get(Class<T> clazz, String mcpName, String srgName) {
		return get(clazz, null, mcpName, srgName);
	}
	/** Gets the value of the field with the specified SRG or MCP names on the specified instance. */
	public static <T, V> V get(T instance, String mcpName, String srgName) {
		return get(instance.getClass(), instance, mcpName, srgName);
	}
	
	/** Sets the value of the field with the specified SRG or MCP names
	 *  on the specified class and instance (may be null if static). */
	public static <T, V> void set(Class<? extends T> clazz, T instance, String mcpName, String srgName, V value) {
		try { findField(clazz, mcpName, srgName).set(instance, value); }
		catch (ReflectiveOperationException ex) { throw new RuntimeException(ex); }
	}
	/** Sets the value of the static field with the specified SRG or MCP names on the specified class. */
	public static <T, V> void set(Class<T> clazz, String mcpName, String srgName, V value) {
		set(clazz, null, mcpName, srgName, value);
	}
	/** Sets the value of the field with the specified SRG or MCP names on the specified instance. */
	public static <T, V> void set(T instance, String mcpName, String srgName, V value) {
		set(instance.getClass(), instance, mcpName, srgName, value);
	}
	
	
	// Methods
	
	private static Map<Triple<Class<?>, String, Class<?>[]>, Method> _cachedMethods =
		new HashMap<Triple<Class<?>, String, Class<?>[]>, Method>();
	
	/** Returns a method on the specified class with the specified SRG
	 *  or MCP names. The method is cached, speeding up future calls. */
	public static Method findMethod(Class<?> clazz, String mcpName, String srgName, Class<?>... parameterTypes) {
		return _cachedMethods.computeIfAbsent(Triple.of(clazz, mcpName, parameterTypes), key -> {
			Method method;
			for (String name : new String[]{ srgName, mcpName }) {
				// Attempt to find the method on the class.
				try { method = clazz.getDeclaredMethod(name, parameterTypes); }
				catch (NoSuchMethodException ex) { continue; }
				// If not found, try next name or exit the loop.
				if (method == null) continue;
				// Set method to be accessible, allowing it to be called.
				try { method.setAccessible(true); }
				catch (SecurityException ex) { throw new RuntimeException(ex); }
				return method;
			}
			throw new RuntimeException(String.format("Could not find method '%s'/'%s' for class %s",
			                                         mcpName, srgName, clazz.getName()));
		});
	}
	
	// TODO: Add invoke helper methods?
	
}
