package dev.sapphic.wearablebackpacks.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.sapphic.wearablebackpacks.Backpacks;
import dev.sapphic.wearablebackpacks.client.mixin.DrawableHelperAccessor;
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
    this.backgroundWidth = (7 * 2) + (Math.max(menu.getColumns(), 9) * 18);
    this.backgroundHeight = 114 + (menu.getRows() * 18);
    this.playerInventoryTitleY = this.backgroundHeight - 94;
  }

  private static void drawTexture(
    final MatrixStack stack, final int x, final int y, final int z, final int w, final int h,
    final float u, final float v, final int rw, final int rh, final int tw, final int th
  ) {
    DrawableHelperAccessor.invokeDrawTexture(stack, x, x + w, y, y + h, z, rw, rh, u, v, tw, th);
  }

  @Override
  public void render(final MatrixStack stack, final int mouseX, final int mouseY, final float delta) {
    this.renderBackground(stack);
    super.render(stack, mouseX, mouseY, delta);
    this.drawMouseoverTooltip(stack, mouseX, mouseY);
  }

  @Override
  protected void drawBackground(final MatrixStack stack, final float tickDelta, final int mx, final int my) {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    //noinspection ConstantConditions
    this.client.getTextureManager().bindTexture(TEXTURE);

    final int bgW = this.x + this.backgroundWidth;
    final int bgH = this.y + this.backgroundHeight;
    final int fillW = this.backgroundWidth - (4 * 2);
    final int fillH = this.backgroundHeight - (4 * 2);

    drawTexture(stack, this.x, this.y, this.getZOffset(), 18.0F, 0.0F, 4, 10, 64, 64); // TOP LEFT
    drawTexture(stack, bgW - 4, this.y, this.getZOffset(), 18.0F + 14.0F, 0.0F, 4, 10, 64, 64); // TOP RIGHT
    drawTexture(stack, this.x, bgH - 4, this.getZOffset(), 18.0F, 14.0F, 4, 10, 64, 64); //BOTTOM LEFT
    drawTexture(stack, bgW - 4, bgH - 4, this.getZOffset(), 18.0F + 14.0F, 14.0F, 4, 10, 64, 64); // BOTTOM RIGHT
    // FIXME Respect Z offset of the GUI (currently delegates to private method with depth of 0)
    drawTexture(stack, this.x + 4, this.y, this.getZOffset(), fillW, 4, 18.0F + 4.0F, 0.0F, 10, 4, 64, 64); // TOP
    drawTexture(stack, this.x, this.y + 4, this.getZOffset(), 4, fillH, 18.0F, 4.0F, 4, 10, 64, 64); // LEFT
    drawTexture(stack, this.x + 4, bgH - 4, this.getZOffset(), fillW, 4, 18.0F + 4.0F, 14.0F, 10, 4, 64, 64); // BOTTOM
    drawTexture(stack, bgW - 4, this.y + 4, this.getZOffset(), 4, fillH, 18.0F + 14.0F, 4.0F, 4, 10, 64, 64); // RIGHT
    drawTexture(stack, this.x + 4, this.y + 4, this.getZOffset(), fillW, fillH, 22.0F, 4.0F, 10, 10, 64, 64); // FILL

    for (final Slot slot : this.getScreenHandler().slots) {
      final int x = (this.x + slot.x) - 1;
      final int y = (this.y + slot.y) - 1;
      drawTexture(stack, x, y, this.getZOffset(), 0.0F, 0.0F, 18, 18, 64, 64);
    }
  }
}
