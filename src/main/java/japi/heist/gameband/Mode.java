package japi.heist.gameband;

import net.minecraft.util.Identifier;

public class Mode {
  private final String name;
  private final Identifier texture;
  
  public Mode(String name, String texture) {
    this.name = name;
    this.texture = Identifier.of("minecraft", texture);
  }
  
  public String getName() {
    return name;
  }
  
  public Identifier getTexture() {
    return texture;
  }
}
