package net.mcft.copy.backpacks.config;

import java.util.Objects;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public abstract class SettingSingleValue<T> extends Setting<T> {
	
	public SettingSingleValue(T defaultValue) { super(defaultValue); }
	
	
	@Override
	protected void loadFromConfiguration(Configuration config)
		{ set(parse(getPropertyFromConfig(config).getString())); }
	
	@Override
	protected void saveToConfiguration(Configuration config)
		{ getPropertyFromConfig(config).set(stringify(get())); }
	
	
	/** Returns the Forge Property.Type used for this setting. Defaults to STRING. */
	protected Property.Type getPropertyType() { return Property.Type.STRING; }
	
	/** Returns the Property object from the Forge Configuration. */
	protected Property getPropertyFromConfig(Configuration config) {
		return config.get(getCategory(), getName(),
			String.valueOf(getDefault()), getComment(), getPropertyType());
	}
	
	
	/** Attempts to parse the specified string as a value of this setting.
	 *  Throws an exception if the specified string is not valid. */
	public abstract T parse(String str);
	
	/** Turns the value into a string to be saved to the config */
	public String stringify(T value) { return Objects.toString(value); }
	
}
