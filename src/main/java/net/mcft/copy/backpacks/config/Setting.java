package net.mcft.copy.backpacks.config;

import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Configuration;

import net.mcft.copy.backpacks.WearableBackpacks;

public abstract class Setting<T> {
	
	public final String category;
	public final String name;
	public final T defaultValue;
	public String comment;
	private T _value;
	
	public Setting(String category, String name, T defaultValue) {
		this.category = category;
		this.name = name;
		this.defaultValue = _value = defaultValue;
	}
	
	public T getValue() {
		return _value;
	}
	
	public Setting<T> setComment(String comment) {
		this.comment = comment;
		return this;
	}
	
	protected Property getProperty(Configuration config) {
		return config.get(category, name, String.valueOf(defaultValue), comment, getType());
	}
	
	protected final void load(Configuration config) {
		_value = load(getProperty(config));
		String validationError = validate(_value);
		if (validationError != null) {
			WearableBackpacks.LOG.error("Error validating config option '{}.{}': {}",
			                            category, name, validationError);
			_value = defaultValue;
		}
	}
	protected final void save(Configuration config) {
		save(getProperty(config), _value);
	}
	
	public abstract Property.Type getType();
	
	protected abstract T load(Property property);
	protected abstract void save(Property property, T value);
	
	protected String validate(T value) { return null; }
	
}
