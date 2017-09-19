package net.mcft.copy.backpacks.client.gui.config;

import java.util.List;
import java.util.Optional;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.Status;

@SideOnly(Side.CLIENT)
public interface IConfigValue<T> {
	
	/** Returns the value currently represented by this
	 *  element, or Optional.empty() if none or invalid. */
	public Optional<T> getValue();
	/** Sets the value this element should represent. */
	public void setValue(T value);
	
	/** Implemented for elements that show status through their
	 *  visual appearance (text, border and background color). */
	public interface ShowsStatus {
		public void setStatus(List<Status> value);
	}
	
	/** Implemented for elements that need to be set up with
	 *  data from a setting. (Such as min/max for sliders.) */
	public interface Setup<T> {
		public void setup(Setting<T> setting);
	}
	
}
