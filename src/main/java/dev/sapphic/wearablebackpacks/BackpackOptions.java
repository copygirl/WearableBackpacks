package dev.sapphic.wearablebackpacks;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Config(name = Backpacks.ID)
public final class BackpackOptions implements ConfigData {

  public static int rows = 3;
  public static int cols = 9;
  public static int damage = 240;

  public static int defense = 5;
  public static float toughness = 0.0f;

  public static boolean enableChestArmorEquip = true;

  private static final Logger LOGGER = LogManager.getLogger();

  public static BackpackOptions instance = new BackpackOptions();

  static int getRows() {
    return rows;
  }

  static int getColumns() {
    return cols;
  }

  static int getMaxDamage() {
    return damage;
  }

  static int getDefense() {
    return defense;
  }

  static float getToughness() {
    return toughness;
  }

  public static int getRows(final int rows) {
    return Math.max(Math.min(rows, 6), 1);
  }

  public static int getColumns(final int columns) {
    return Math.max(Math.min(columns, 18), 1);
  }

}
