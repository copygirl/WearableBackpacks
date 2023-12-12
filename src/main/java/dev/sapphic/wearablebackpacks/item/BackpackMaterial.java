package dev.sapphic.wearablebackpacks.item;

import dev.sapphic.wearablebackpacks.Backpack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

final class BackpackMaterial implements ArmorMaterial {
  static final ArmorMaterial INSTANCE = new BackpackMaterial();
  
  private BackpackMaterial() {
  }
  
  @Override
  public int getDurability(final EquipmentSlot slot) {
    return (slot == EquipmentSlot.CHEST) ? Backpack.getMaxDamage() : 0;
  }
  
  @Override
  public int getProtectionAmount(final EquipmentSlot slot) {
    return (slot == EquipmentSlot.CHEST) ? Backpack.getDefense() : 0;
  }
  
  @Override
  public int getEnchantability() {
    return 0;
  }
  
  @Override
  public SoundEvent getEquipSound() {
    return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
  }
  
  @Override
  public Ingredient getRepairIngredient() {
    return Ingredient.ofItems(Items.LEATHER);
  }
  
  @Override
  @Environment(EnvType.CLIENT)
  public String getName() {
    return "backpack";
  }
  
  @Override
  public float getToughness() {
    return Backpack.getToughness();
  }
  
  @Override
  public float getKnockbackResistance() {
    return 0.0F;
  }
}
