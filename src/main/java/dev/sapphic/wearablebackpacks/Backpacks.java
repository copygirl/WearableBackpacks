package dev.sapphic.wearablebackpacks;

import com.google.common.collect.ImmutableSet;
import dev.sapphic.wearablebackpacks.block.BackpackBlock;
import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import dev.sapphic.wearablebackpacks.inventory.BackpackMenu;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import dev.sapphic.wearablebackpacks.recipe.BackpackDyeingRecipe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class Backpacks implements ModInitializer {
    public static final String ID = "wearablebackpacks";

    public static final Block BLOCK = new BackpackBlock(FabricBlockSettings.of(Material.WOOL, MapColor.CLEAR).strength(0.5F, 0.5F).sounds(BlockSoundGroup.WOOL));
    public static final Item ITEM = new BackpackItem(BLOCK, new Item.Settings().group(ItemGroup.TOOLS));

    @Override
    public void onInitialize() {
        BackpackOptions.init(FabricLoader.getInstance().getConfigDir().resolve(ID + ".json"));

        final Identifier backpack = new Identifier(ID, "backpack");

        Registry.register(Registry.BLOCK, backpack, BLOCK);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, backpack, BLOCK_ENTITY);
        Registry.register(Registry.ITEM, backpack, ITEM);

        Item.BLOCK_ITEMS.put(BLOCK, ITEM);

        Registry.register(Registry.SCREEN_HANDLER, backpack, BackpackMenu.TYPE);
        Registry.register(Registry.RECIPE_SERIALIZER, BackpackDyeingRecipe.ID, BackpackDyeingRecipe.SERIALIZER);
    }    public static final BlockEntityType<BackpackBlockEntity> BLOCK_ENTITY = new BlockEntityType<>(BackpackBlockEntity::new, ImmutableSet.of(BLOCK), null);




}
