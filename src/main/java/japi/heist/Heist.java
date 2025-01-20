package japi.heist;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Heist implements ModInitializer {
  public static final String MOD_ID = "heist";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  
  @Override
  public void onInitialize() {
    Items.initialize();
    Modes.initialize();
    LOGGER.info("Loading heist mod!");
  }
}
