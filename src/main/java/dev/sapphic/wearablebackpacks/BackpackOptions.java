package dev.sapphic.wearablebackpacks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public final class BackpackOptions {
  private static final int MIN_ROWS = 1;
  private static final int DEFAULT_ROWS = 3;
  private static final int MAX_ROWS = 6;

  private static final int MIN_COLUMNS = 1;
  private static final int DEFAULT_COLUMNS = 9;
  private static final int MAX_COLUMNS = 18;

  private static final int MIN_DAMAGE = 0;
  private static final int DEFAULT_MAX_DAMAGE = 240;
  private static final int MAX_DAMAGE = 495;

  private static final int MIN_DEFENSE = 0;
  private static final int DEFAULT_DEFENSE = 5;
  private static final int MAX_DEFENSE = 6;

  private static final float MIN_TOUGHNESS = 0.0F;
  private static final float DEFAULT_TOUGHNESS = 0.0F;
  private static final float MAX_TOUGHNESS = 2.0F;

  private static final Logger LOGGER = LogManager.getLogger();

  private static BackpackOptions instance = new BackpackOptions();

  private final int rows;
  private final int columns;
  private final int maxDamage;
  private final int defense;
  private final float toughness;

  private BackpackOptions(
    final int rows, final int columns, final int maxDamage, final int defense, final float toughness
  ) {
    this.rows = getRows(rows);
    this.columns = getColumns(columns);
    this.maxDamage = getDamage(maxDamage);
    this.defense = getDefense(defense);
    this.toughness = getToughness(toughness);
  }

  private BackpackOptions() {
    this.rows = DEFAULT_ROWS;
    this.columns = DEFAULT_COLUMNS;
    this.maxDamage = DEFAULT_MAX_DAMAGE;
    this.defense = DEFAULT_DEFENSE;
    this.toughness = DEFAULT_TOUGHNESS;
  }

  static int getRows() {
    return instance.rows;
  }

  static int getColumns() {
    return instance.columns;
  }

  static int getMaxDamage() {
    return instance.maxDamage;
  }

  static int getDefense() {
    return instance.defense;
  }

  static float getToughness() {
    return instance.toughness;
  }

  public static int getRows(final int rows) {
    return Math.max(Math.min(rows, MAX_ROWS), MIN_ROWS);
  }

  public static int getColumns(final int columns) {
    return Math.max(Math.min(columns, MAX_COLUMNS), MIN_COLUMNS);
  }

  public static int getDamage(final int damage) {
    return Math.max(Math.min(damage, MAX_DAMAGE), MIN_DAMAGE);
  }

  static void init(final Path input) {
    try (final Reader reader = Files.newBufferedReader(input)) {
      final Gson gson = new GsonBuilder().setLenient().create();
      instance = gson.fromJson(reader, BackpackOptions.class);
    } catch (final NoSuchFileException ignored) {
    } catch (final IOException | IllegalArgumentException e) {
      LOGGER.warn("Failed to read options", e);
    }
    try (final Writer writer = Files.newBufferedWriter(input)) {
      final Gson gson = new GsonBuilder().setPrettyPrinting().create();
      gson.toJson(instance, writer);
    } catch (final IOException e) {
      throw new IllegalStateException("Failed to write options", e);
    }
  }

  private static int getDefense(final int defense) {
    return Math.max(Math.min(defense, MAX_DEFENSE), MIN_DEFENSE);
  }

  private static float getToughness(final float toughness) {
    if (!Float.isNaN(toughness)) {
      return Math.max(Math.min(toughness, MAX_TOUGHNESS), MIN_TOUGHNESS);
    }
    return DEFAULT_TOUGHNESS;
  }
}
