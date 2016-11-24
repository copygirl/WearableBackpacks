package net.mcft.copy.backpacks.config;

import net.minecraftforge.common.config.Property;

public class SettingBoolean extends Setting<Boolean> {
	
	public SettingBoolean(String category, String name, boolean defaultValue) {
		super(category, name, defaultValue);
	}
	
	@Override
	public Property.Type getType() { return Property.Type.BOOLEAN; }
	
	@Override
	public Boolean load(Property property) { return property.getBoolean(); }
	
	@Override
	public void save(Property property, Boolean value) { property.set(value); }
	
}