package dev.sapphic.wearablebackpacks.mixin.client;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DrawableHelper.class)
public interface DrawableHelperAccessor {
  /**
   * Draws a textured rectangle onto the screen. This is the only method in the GUI library that allows
   * for both texture regions and screen depth to be defined, but it is private, necessitating this hook
   *
   * @param stack Matrix stack to be peeked
   * @param x0    Leftmost position of the rectangle
   * @param x1    Rightmost position of the rectangle
   * @param y0    Topmost position of the rectangle
   * @param y1    Bottommost position of the rectangle
   * @param z     Screen depth of the rectangle
   * @param rw    Sample region width on the texture
   * @param rh    Sample region height on the texture
   * @param u     Horizontal coordinate on the texture
   * @param v     Vertical coordinate on the texture
   * @param tw    Total width of the texture sheet
   * @param th    Total height of the texture sheet
   */
  @Invoker
  static void invokeDrawTexture(
    final MatrixStack stack, final int x0, final int x1, final int y0, final int y1, final int z,
    final int rw, final int rh, final float u, final float v, final int tw, final int th
  ) {
    throw new AssertionError();
  }
}
