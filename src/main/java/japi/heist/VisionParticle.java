package japi.heist;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class VisionParticle extends SpriteBillboardParticle {
  VisionParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
    super(clientWorld, d, e, f);
    this.setBoundingBoxSpacing(0.02F, 0.02F);
    this.scale = 0.2F;
    this.velocityX = g;
    this.velocityY = h;
    this.velocityZ = i;
    this.maxAge = 0;
  }
  
  @Override
  public void tick() {
    this.markDead();
  }
  
  @Override
  public ParticleTextureSheet getType() {
    return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
  }
  
  @Environment(EnvType.CLIENT)
  public static class Factory implements ParticleFactory<SimpleParticleType> {
    private final SpriteProvider spriteProvider;
    
    public Factory(SpriteProvider spriteProvider) {
      this.spriteProvider = spriteProvider;
    }
    
    public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
      VisionParticle visionParticle = new VisionParticle(clientWorld, d, e, f, g, h, i);
      visionParticle.setSprite(this.spriteProvider);
      return visionParticle;
    }
  }
}
