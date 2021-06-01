package dev.sapphic.wearablebackpacks.inventory;

import net.minecraft.inventory.Inventory;

public interface BackpackContainer extends Inventory {
  int getRows();
  
  int getColumns();
}
