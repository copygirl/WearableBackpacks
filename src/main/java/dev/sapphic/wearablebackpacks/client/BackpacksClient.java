package dev.sapphic.wearablebackpacks.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import dev.sapphic.wearablebackpacks.Backpack;
import dev.sapphic.wearablebackpacks.Backpacks;
import dev.sapphic.wearablebackpacks.block.BackpackBlock;
import dev.sapphic.wearablebackpacks.client.mixin.ModelLoaderAccessor;
import dev.sapphic.wearablebackpacks.client.render.BackpackBlockRenderer;
import dev.sapphic.wearablebackpacks.client.screen.BackpackScreen;
import dev.sapphic.wearablebackpacks.inventory.BackpackMenu;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public final class BackpacksClient implements ClientModInitializer {
  private static final Identifier BACKPACK_LID = new Identifier(Backpacks.ID, "backpack_lid");

  private static final KeyBinding BACKPACK_KEY_BINDING =
    new KeyBinding("key." + Backpacks.ID + ".backpack", GLFW.GLFW_KEY_B, "key.categories.inventory");

  @SuppressWarnings("RedundantTypeArguments")
  private static final ImmutableMap<Direction, ModelIdentifier> LID_MODELS = Arrays.stream(Direction.values())
    .filter(Direction.Type.HORIZONTAL).collect(Maps.toImmutableEnumMap(Function.<Direction>identity(), facing ->
      new ModelIdentifier(BACKPACK_LID, String.format(Locale.ROOT, "facing=%s", facing.asString()))
    ));

  private static final PacketByteBuf EMPTY_PACKET_BUFFER = new PacketByteBuf(Unpooled.EMPTY_BUFFER);

  public static ModelIdentifier getLidModel(final Direction facing) {
    return LID_MODELS.getOrDefault(facing, ModelLoader.MISSING);
  }

  private static void addLidStateDefinitions() {
    ModelLoaderAccessor.setStaticDefinitions(
      ImmutableMap.<Identifier, StateManager<Block, BlockState>>builder()
        .putAll(ModelLoaderAccessor.getStaticDefinitions())
        .put(BACKPACK_LID, new StateManager.Builder<Block, BlockState>(Blocks.AIR)
          .add(BackpackBlock.FACING).build(Block::getDefaultState, BlockState::new)
        ).build()
    );
  }

  private static void pollBackpackKey(final MinecraftClient client) {
    if ((client.player != null) && client.player.world.isClient) {
      while (BACKPACK_KEY_BINDING.wasPressed()) {
        final ItemStack stack = client.player.getEquippedStack(EquipmentSlot.CHEST);
        if (stack.getItem() instanceof BackpackItem) {
          final float pitch = (client.player.world.random.nextFloat() * 0.1F) + 0.9F;
          client.player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.5F, pitch);
          ClientPlayNetworking.send(Backpacks.OPEN_BACKPACK_MESSAGE, EMPTY_PACKET_BUFFER);
        }
      }
    }
  }

  @Override
  public void onInitializeClient() {
    addLidStateDefinitions();
    ScreenRegistry.register(BackpackMenu.TYPE, BackpackScreen::new);
    KeyBindingHelper.registerKeyBinding(BACKPACK_KEY_BINDING);
    ClientTickEvents.END_CLIENT_TICK.register(BackpacksClient::pollBackpackKey);
    BlockEntityRendererRegistry.INSTANCE.register(Backpacks.BLOCK_ENTITY, BackpackBlockRenderer::new);
    ColorProviderRegistry.BLOCK.register((state, world, pos, tint) -> {
      return ((world != null) && (pos != null)) ? Backpack.getColor(world, pos) : Backpack.DEFAULT_COLOR;
    }, Backpacks.BLOCK);
    ColorProviderRegistry.ITEM.register((stack, tintIndex) -> Backpack.getColor(stack), Backpacks.ITEM);
  }
}
