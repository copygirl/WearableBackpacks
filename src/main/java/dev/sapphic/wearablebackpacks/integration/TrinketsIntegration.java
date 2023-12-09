package dev.sapphic.wearablebackpacks.integration;

import dev.emi.trinkets.TrinketsClient;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import dev.sapphic.wearablebackpacks.Backpacks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TrinketsIntegration {

    /**
     * Attempts to equip the backpack on player's trinket slot
     */
    public static void equipBackpack(ItemStack backpack, PlayerEntity entity) {
        // TODO: find a proper way of doing this
        final Optional<TrinketComponent> maybeComponent = TrinketsApi.getTrinketComponent(entity);
        if (maybeComponent.isPresent()) {
            final TrinketComponent component = maybeComponent.get();
            for (Map<String, TrinketInventory> value : component.getInventory().values()) {
                for (TrinketInventory trinketInventory : value.values()) {
                    trinketInventory.setStack(0, backpack);
                }
            }
        }
    }

    /***
     * Gets the backpack from the trinket slot
     * @param entity The Living Entity to check.
     * @return The backpack ItemStack
     */
    public static ItemStack getBackpack(LivingEntity entity) {
        // TODO: find a proper way of doing this
        final Optional<TrinketComponent> maybeComponent = TrinketsApi.getTrinketComponent(entity);
        if (maybeComponent.isPresent()) {
            final TrinketComponent component = maybeComponent.get();
            for (Map<String, TrinketInventory> value : component.getInventory().values()) {
                for (TrinketInventory trinketInventory : value.values()) {
                    return trinketInventory.getStack(0);
                }
            }
        }
        return ItemStack.EMPTY;
    }
    /**
     * Whether the backpack is equipped on the Trinket Slot.
     * @param entity The Living Entity to check.
     */
    public static boolean isBackpackEquipped(LivingEntity entity) {
        final Optional<TrinketComponent> cmp = TrinketsApi.getTrinketComponent(entity);
        if (cmp.isPresent()) {
            final TrinketComponent component = cmp.get();
            return component.isEquipped(Backpacks.backpackItem);
        }
        return false;
    }
}
