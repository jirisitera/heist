package japi.heist.camera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CameraEntityRenderState extends LivingEntityRenderState {
  public Vec3d renderPositionOffset = Vec3d.ZERO;
  @Nullable
  public DyeColor color;
  public float openProgress;
  public float headYaw;
  public float shellYaw;
  public Direction facing = Direction.DOWN;
}
