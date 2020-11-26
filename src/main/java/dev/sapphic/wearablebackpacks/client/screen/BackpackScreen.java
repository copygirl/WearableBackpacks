package dev.sapphic.wearablebackpacks.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.sapphic.wearablebackpacks.Backpacks;
import dev.sapphic.wearablebackpacks.client.BackpacksClient;
import dev.sapphic.wearablebackpacks.inventory.BackpackMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class BackpackScreen extends HandledScreen<BackpackMenu> {
  private static final Identifier TEXTURE = new Identifier(Backpacks.ID, "textures/gui/container/backpack.png");

  public BackpackScreen(final BackpackMenu menu, final PlayerInventory inventory, final Text name) {
    super(menu, inventory, name);
    this.backgroundWidth = menu.getColumns() * 18;
    // todo menu height, fix menu width to include padding
  }

  @Override
  public void render(final MatrixStack stack, final int mouseX, final int mouseY, final float delta) {
    this.renderBackground(stack);
    super.render(stack, mouseX, mouseY, delta);
    this.drawMouseoverTooltip(stack, mouseX, mouseY);
  }

  @Override
  protected void drawForeground(final MatrixStack stack, final int mouseX, final int mouseY) {
    this.textRenderer.draw(stack, this.title, 8.0F, 6.0F, 0x404040);
    final Text inventoryName = this.playerInventory.getDisplayName();
    this.textRenderer.draw(stack, inventoryName, 8.0F, (this.backgroundHeight - 96) + 2, 0x404040);
  }

  @Override
  protected void drawBackground(final MatrixStack stack, final float tickDelta, final int mx, final int my) {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    //noinspection ConstantConditions
    this.client.getTextureManager().bindTexture(TEXTURE);
    // TODO
    for (final Slot slot : this.getScreenHandler().slots) {
      this.drawTexture(stack, (this.x + slot.x) - 1, (this.y + slot.y) - 1, 7, 17, 18, 18);
    }
  }

  @Override
  public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
    super.keyPressed(keyCode, scanCode, modifiers); // Can't check this, Mojang always return true >:(
    if (BackpacksClient.isBackpackKeyBinding(keyCode, scanCode)) {
      //noinspection ConstantConditions
      this.client.player.closeHandledScreen();
    }
    return true;
  }
}
