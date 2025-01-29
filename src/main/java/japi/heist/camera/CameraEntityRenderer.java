package japi.heist.camera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class CameraEntityRenderer extends MobEntityRenderer<CameraEntity, CameraEntityRenderState, CameraEntityModel> {
  private static final Identifier TEXTURE = TexturedRenderLayers.SHULKER_TEXTURE_ID
      .getTextureId()
      .withPath(string -> "textures/" + string + ".png");
  private static final Identifier[] COLORED_TEXTURES = TexturedRenderLayers.COLORED_SHULKER_BOXES_TEXTURES
      .stream()
      .map(spriteId -> spriteId.getTextureId().withPath(string -> "textures/" + string + ".png"))
      .toArray(Identifier[]::new);
  
  public CameraEntityRenderer(EntityRendererFactory.Context context) {
    super(context, new CameraEntityModel(context.getPart(EntityModelLayers.SHULKER)), 0.0F);
  }
  
  public static Identifier getTexture(@Nullable DyeColor cameraColor) {
    return cameraColor == null ? TEXTURE : COLORED_TEXTURES[cameraColor.getId()];
  }
  
  @Override
  public Vec3d getPositionOffset(CameraEntityRenderState cameraEntityRenderState) {
    return cameraEntityRenderState.renderPositionOffset;
  }
  
  @Override
  public boolean shouldRender(CameraEntity cameraEntity, Frustum frustum, double d, double e, double f) {
    if (super.shouldRender(cameraEntity, frustum, d, e, f)) {
      return true;
    } else {
      Vec3d vec3d = cameraEntity.getRenderPositionOffset(0.0F);
      if (vec3d == null) {
        return false;
      } else {
        EntityType<?> entityType = cameraEntity.getType();
        float g = entityType.getHeight() / 2.0F;
        float h = entityType.getWidth() / 2.0F;
        Vec3d vec3d2 = Vec3d.ofBottomCenter(cameraEntity.getBlockPos());
        return frustum.isVisible(new Box(vec3d.x, vec3d.y + g, vec3d.z, vec3d2.x, vec3d2.y + g, vec3d2.z).expand(h, g, h));
      }
    }
  }
  
  public Identifier getTexture(CameraEntityRenderState cameraEntityRenderState) {
    return getTexture(cameraEntityRenderState.color);
  }
  
  public CameraEntityRenderState createRenderState() {
    return new CameraEntityRenderState();
  }
  
  @Override
  public void updateRenderState(CameraEntity cameraEntity, CameraEntityRenderState cameraEntityRenderState, float f) {
    super.updateRenderState(cameraEntity, cameraEntityRenderState, f);
    cameraEntityRenderState.renderPositionOffset = Objects.requireNonNullElse(cameraEntity.getRenderPositionOffset(f), Vec3d.ZERO);
    cameraEntityRenderState.color = cameraEntity.getColor();
    cameraEntityRenderState.openProgress = cameraEntity.getOpenProgress(f);
    cameraEntityRenderState.headYaw = cameraEntity.headYaw;
    cameraEntityRenderState.shellYaw = cameraEntity.bodyYaw;
    cameraEntityRenderState.facing = cameraEntity.getAttachedFace();
  }
  
  @Override
  protected void setupTransforms(CameraEntityRenderState cameraEntityRenderState, MatrixStack matrixStack, float f, float g) {
    super.setupTransforms(cameraEntityRenderState, matrixStack, f + 180.0F, g);
    matrixStack.multiply(cameraEntityRenderState.facing.getOpposite().getRotationQuaternion(), 0.0F, 0.5F, 0.0F);
  }
}
