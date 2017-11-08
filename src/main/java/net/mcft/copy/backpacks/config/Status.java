package net.mcft.copy.backpacks.config;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.resources.I18n;

import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;

/** Represent a status about a setting or its
 *  config GUI entry: A hint, warning or error. */
public class Status {
	
	public static final Status NONE = FINE();
	public static final Status EMPTY = ERROR();
	public static final Status INVALID = ERROR();
	public static Status OUT_OF_RANGE(Object min, Object max)
		{ return ERROR("general", "outOfRange", min, max); }
	public static Status REQUIRED(Setting<?> value)
		{ return ERROR("general", "required", value.getFullName()); }
	public static Status RECOMMENDED(Setting<?> value, String key)
		{ return HINT("general", key, value.getFullName()); }
	
	
	public final Severity severity;
	private final String _translateKey;
	private final Object[] _translateParams;
	
	private Status(Severity severity, String translateKey, Object[] translateParams)
		{ this.severity = severity; _translateKey = translateKey; _translateParams = translateParams; }
	
	
	private static Status constructStatus(Severity severity, String category, String key, Object... args)
		{ return new Status(severity, "config." + WearableBackpacks.MOD_ID + "." + category + ".status." + key, args); }
	
	public static Status FINE() { return new Status(Severity.FINE, null, null); }
	public static Status FINE(String category, String key, Object... args)
		{ return constructStatus(Severity.FINE, category, key, args); }
	
	public static Status HINT() { return new Status(Severity.HINT, null, null); }
	public static Status HINT(String category, String key, Object... args)
		{ return constructStatus(Severity.HINT, category, key, args); }
	
	public static Status WARN() { return new Status(Severity.WARN, null, null); }
	public static Status WARN(String category, String key, Object... args)
		{ return constructStatus(Severity.WARN, category, key, args); }
	
	public static Status ERROR() { return new Status(Severity.ERROR, null, null); }
	public static Status ERROR(String category, String key, Object... args)
		{ return constructStatus(Severity.ERROR, category, key, args); }
	
	
	
	public static Severity getSeverity(List<Status> status) {
		return status.stream()
			.map(s -> s.severity)
			.max(Severity::compareTo)
			.orElse(Severity.FINE);
	}
	
	@SideOnly(Side.CLIENT)
	public static List<String> getMessage(List<Status> status) {
		return status.stream()
			.filter(s -> (s._translateKey != null))
			.sorted(Comparator.comparingInt(s -> -s.severity.ordinal()))
			.map(s -> "\u00a7" + s.severity.colorChar +
			          I18n.format(s._translateKey, s._translateParams))
			.collect(Collectors.toList());
	}
	
	
	public enum Severity {
		FINE('f', 0x00000000),
		HINT('2', 0x2000D000),
		WARN('e', 0x30E0D000),
		ERROR('c', 0x40D00000);
		
		public final char colorChar;
		public final int foregroundColor;
		public final int backgroundColor;
		public final int guiIconIndex;
		
		private Severity(char chr, int background) {
			colorChar = chr;
			foregroundColor = 0xFF000000 | GuiUtils.getColorCode(chr, true);
			backgroundColor = background;
			guiIconIndex = ordinal();
		}
	}
	
}
