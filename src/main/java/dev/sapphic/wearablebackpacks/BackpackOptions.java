package dev.sapphic.wearablebackpacks;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = Backpacks.ID)
public class BackpackOptions implements ConfigData {
  @Comment("The number of rows in the backpack. [Default: 3]")
  public int rows = 3;

  @Comment("The number of columns in the backpack. [Default: 9]")
  public int cols = 9;

  @Comment("The durability of the backpack. [Default: 240]")
  public int damage = 240;

  @Comment("The amount of protection the backpack provides. [Default: 5]")
  public int defense = 5;

  @Comment("The toughness of the backpack. [Default: 0.0]")
  public float toughness = 0.0f;
  @Comment("Enable backpacks to be equipped in the chest armor slot. (Requires restart)")
  public boolean enableChestArmorEquip = true;

  @Comment("Enable other players to open your backpacks when equipped. (Requires restart)")
  public boolean enableEquippedInteraction = true;

  public static int getRows(final int rows) {
    return Math.max(Math.min(rows, 6), 1);
  }

  public static int getColumns(final int columns) {
    return Math.max(Math.min(columns, 18), 1);
  }
}
