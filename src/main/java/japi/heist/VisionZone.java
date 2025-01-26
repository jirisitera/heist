package japi.heist;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VisionZone {
  private static final double SPACE = 0.3;
  
  private VisionZone() {
    throw new IllegalStateException("Not to be used directly");
  }
  
  public static void render(World world, Entity entity) {
    if (world.isClient) {
      Vec3d eyePos = entity.getEyePos();
      Vec3d rotation = entity.getRotationVector().normalize().multiply(10);
      
      Vec3d up = entity.getRotationVector(entity.getPitch() + 90, entity.getYaw()).normalize();
      Vec3d perpendicular = rotation.crossProduct(up).normalize();
      
      Vec3d center = eyePos.add(rotation);
      
      Vec3d right = perpendicular.multiply(5);
      Vec3d left = perpendicular.multiply(-5);
      Vec3d top = up.multiply(2.5);
      Vec3d bottom = up.multiply(-2.5);
      
      Vec3d topRight = center.add(right).add(top);
      Vec3d topLeft = center.add(left).add(top);
      Vec3d bottomRight = center.add(right).add(bottom);
      Vec3d bottomLeft = center.add(left).add(bottom);
      
      drawLine(world, eyePos, topRight);
      drawLine(world, eyePos, topLeft);
      drawLine(world, eyePos, bottomRight);
      drawLine(world, eyePos, bottomLeft);
      
      drawLine(world, topRight, bottomRight);
      drawLine(world, topLeft, bottomLeft);
      drawLine(world, topRight, topLeft);
      drawLine(world, bottomRight, bottomLeft);
    }
  }
  
  private static void drawLine(World world, Vec3d point1, Vec3d point2) {
    double distance = point1.distanceTo(point2);
    Vec3d step = point2.subtract(point1).normalize().multiply(SPACE);
    double covered = 0;
    Vec3d current = point1;
    while (covered < distance) {
      world.addParticle(Heist.VISION_PARTICLE, true, true, current.getX(), current.getY(), current.getZ(), 0, 0, 0);
      covered += SPACE;
      current = current.add(step);
    }
  }
}
