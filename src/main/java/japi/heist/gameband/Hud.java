package japi.heist.gameband;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;

public class Hud {
  private static final Identifier BACKGROUND = Identifier.of("heist", "textures/gui/gameband_background.png");
  private static final Identifier SELECTION = Identifier.of("heist", "textures/gui/gameband_selection.png");
  private static final int TEXTURE_SIZE = 16;
  private static final int BACKGROUND_SIZE = 22;
  private static final int SELECTION_SIZE = 24;
  private static TriState animate = TriState.DEFAULT;
  private static float offset = 0F;
  
  private Hud() {
    throw new IllegalStateException("Not to be used directly");
  }
  
  public static TriState getAnimate() {
    return Hud.animate;
  }
  
  public static void setAnimate(TriState animate) {
    Hud.animate = animate;
  }
  
  public static void setOffset(float offset) {
    Hud.offset = offset;
  }
  
  public static void render(DrawContext context, RenderTickCounter renderTickCounter) {
    MatrixStack matrices = context.getMatrices();
    matrices.push();
    if (animate != TriState.DEFAULT) {
      offset += renderTickCounter.getTickDelta(false) * 2;
      if (offset >= BACKGROUND_SIZE) {
        if (animate == TriState.TRUE) {
          Modes.ALL.removeFirst();
          Modes.ALL.addFirst(Modes.ALL.removeLast());
          Modes.locateCurrent();
        } else {
          Modes.ALL.removeLast();
          Modes.ALL.addLast(Modes.ALL.removeFirst());
          Modes.locateCurrent();
        }
        offset = 0;
        animate = TriState.DEFAULT;
      }
      float currentOffset = offset;
      if (animate == TriState.TRUE) {
        currentOffset *= -1;
      }
      matrices.translate(0, currentOffset, 0);
    }
    int width = context.getScaledWindowWidth() - SELECTION_SIZE;
    int height = context.getScaledWindowHeight() / 2;
    int heightOffset = 0;
    int half = (Modes.ALL.size() - 1) / 2;
    if (animate == TriState.TRUE) {
      half++;
    }
    int offset = half * BACKGROUND_SIZE;
    heightOffset += offset;
    for (int i = 0; i < Modes.ALL.size(); i++) {
      Mode mode = Modes.ALL.get(i);
      heightOffset -= BACKGROUND_SIZE;
      int alphaIndex = i;
      if (i > half) {
        alphaIndex = half * 2 - i;
      }
      int alpha = 100 * alphaIndex + 50;
      int argb = (alpha << 24) | (255 << 16) | (255 << 8) | 255;
      context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, width + 1, height + heightOffset, 0, 0, BACKGROUND_SIZE, BACKGROUND_SIZE, BACKGROUND_SIZE, BACKGROUND_SIZE, argb);
      context.drawTexture(RenderLayer::getGuiTextured, mode.getTexture(), width + 4, height + heightOffset + 3, 0, 0, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, argb);
    }
    matrices.pop();
    context.drawTexture(RenderLayer::getGuiTextured, SELECTION, width, height - SELECTION_SIZE + 1, 0, 0, SELECTION_SIZE, SELECTION_SIZE, SELECTION_SIZE, SELECTION_SIZE);
  }
}
