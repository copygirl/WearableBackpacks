package dev.sapphic.wearablebackpacks;

import com.google.common.collect.ImmutableSet;
import dev.sapphic.wearablebackpacks.block.BackpackBlock;
import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import dev.sapphic.wearablebackpacks.inventory.BackpackMenu;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import dev.sapphic.wearablebackpacks.mixin.BlockSettingsAccessor;
import dev.sapphic.wearablebackpacks.recipe.BackpackDyeingRecipe;
import dev.sapphic.wearablebackpacks.stat.BackpackStats;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class Backpacks implements ModInitializer {
  public static final String ID = "wearablebackpacks";

  public static final Identifier OPEN_BACKPACK_MESSAGE = new Identifier(ID, "open_backpack");

  @SuppressWarnings("CastToIncompatibleInterface")
  public static final Block BLOCK = new BackpackBlock((
    (BlockSettingsAccessor) AbstractBlock.Settings.of(Material.WOOL, MaterialColor.CLEAR).strength(0.5F, 0.5F)
  ).callSounds(BlockSoundGroup.WOOL));

  public static final BlockEntityType<BackpackBlockEntity> BLOCK_ENTITY =
    new BlockEntityType<>(BackpackBlockEntity::new, ImmutableSet.of(BLOCK), null);

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

    Registry.register(Registry.RECIPE_SERIALIZER,
      BackpackDyeingRecipe.ID, BackpackDyeingRecipe.SERIALIZER
    );

    ServerSidePacketRegistry.INSTANCE.register(OPEN_BACKPACK_MESSAGE, (ctx, buf) ->
      ctx.getTaskQueue().execute(() -> {
        final ServerPlayerEntity player = (ServerPlayerEntity) ctx.getPlayer();
        final ItemStack stack = player.getEquippedStack(EquipmentSlot.CHEST);
        if (stack.getItem() instanceof BackpackItem) {
          player.openHandledScreen(new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
              return stack.hasCustomName() ? stack.getName() : new TranslatableText("container." + ID);
            }

            @Override
            public ScreenHandler createMenu(final int id, final PlayerInventory inventory, final PlayerEntity player) {
              return new BackpackMenu(id, inventory, stack, Backpack.getRows(stack), Backpack.getColumns(stack));
            }
          });
          player.incrementStat(BackpackStats.OPENED);
        }
      }));
  }
}
