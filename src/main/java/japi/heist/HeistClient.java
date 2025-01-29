package japi.heist;

import japi.heist.camera.CameraEntityModel;
import japi.heist.camera.CameraEntityRenderer;
import japi.heist.gameband.Hud;
import japi.heist.gameband.Modes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class HeistClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    KeyBinding next = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.heist.next",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "category.heist.keybindings"
    ));
    KeyBinding previous = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.heist.previous",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_G,
        "category.heist.keybindings"
    ));
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      while (next.wasPressed()) {
        Modes.next();
      }
      while (previous.wasPressed()) {
        Modes.previous();
      }
    });
    ParticleFactoryRegistry.getInstance().register(Heist.VISION_PARTICLE, VisionParticle.Factory::new);
    HudRenderCallback.EVENT.register(Hud::render);
    EntityRendererRegistry.register(Entities.CAMERA, CameraEntityRenderer::new);
    EntityModelLayerRegistry.registerModelLayer(new EntityModelLayer(Identifier.of(Heist.MOD_ID, "camera"), "camera"), CameraEntityModel::getTexturedModelData);
  }
}
