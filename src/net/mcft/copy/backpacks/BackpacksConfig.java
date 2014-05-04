package net.mcft.copy.backpacks;

import java.io.File;

import net.mcft.copy.core.config.Config;

public class BackpacksConfig extends Config {
	
	// Categories
	public static final String BLOCKS = "blocks";
	
	
	// Settings
	// TODO: Add settings.
	
	
	public BackpacksConfig(String id, File file) {
		super(id, file);
		
		// Settings in this category are added
		// automatically when registering blocks.
		addCategoryComment(BLOCKS, "Controls whether or not certain blocks get added.");
		
	}
	
}
