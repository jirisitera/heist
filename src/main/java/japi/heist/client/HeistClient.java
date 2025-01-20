package japi.heist.client;

import japi.heist.Hud;
import japi.heist.Modes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class HeistClient implements ClientModInitializer {
  private static KeyBinding NEXT;
  private static KeyBinding PREVIOUS;
  
  @Override
  public void onInitializeClient() {
    NEXT = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.heist.next",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "category.heist.keybinds"
    ));
    PREVIOUS = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.heist.previous",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_G,
        "category.heist.keybinds"
    ));
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      while (NEXT.wasPressed()) {
        Modes.next();
      }
      while (PREVIOUS.wasPressed()) {
        Modes.previous();
      }
    });
    HudRenderCallback.EVENT.register(Hud::render);
  }
}
