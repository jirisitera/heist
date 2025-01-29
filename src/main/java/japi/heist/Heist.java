package japi.heist;

import japi.heist.gameband.Modes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Heist implements ModInitializer {
  public static final String MOD_ID = "heist";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  public static final SimpleParticleType VISION_PARTICLE = FabricParticleTypes.simple();
  
  @Override
  public void onInitialize() {
    Items.initialize();
    Modes.initialize();
    Entities.initialize();
    Registry.register(Registries.PARTICLE_TYPE, Identifier.of(Heist.MOD_ID, "vision_particle"), VISION_PARTICLE);
    LOGGER.info("Loading heist mod!");
  }
}
