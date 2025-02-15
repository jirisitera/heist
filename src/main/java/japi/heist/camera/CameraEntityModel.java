package japi.heist.camera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class CameraEntityModel extends EntityModel<CameraEntityRenderState> {
  /**
   * The key of the lid model part, whose value is {@value}.
   */
  public static final String LID = "lid";
  /**
   * The key of the base model part, whose value is {@value}.
   */
  private static final String BASE = "base";
  private final ModelPart lid;
  private final ModelPart head;
  
  public CameraEntityModel(ModelPart modelPart) {
    super(modelPart, RenderLayer::getEntityCutoutNoCullZOffset);
    this.lid = modelPart.getChild("lid");
    this.head = modelPart.getChild(EntityModelPartNames.HEAD);
  }
  
  private static ModelData getModelData() {
    ModelData modelData = new ModelData();
    ModelPartData modelPartData = modelData.getRoot();
    modelPartData.addChild("lid", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -16.0F, -8.0F, 16.0F, 12.0F, 16.0F), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
    modelPartData.addChild("base", ModelPartBuilder.create().uv(0, 28).cuboid(-8.0F, -8.0F, -8.0F, 16.0F, 8.0F, 16.0F), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
    return modelData;
  }
  
  public static TexturedModelData getTexturedModelData() {
    ModelData modelData = getModelData();
    modelData.getRoot()
        .addChild(
            EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 52).cuboid(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F), ModelTransform.pivot(0.0F, 12.0F, 0.0F)
        );
    return TexturedModelData.of(modelData, 64, 64);
  }
  
  @Override
  public void setAngles(CameraEntityRenderState cameraEntityRenderState) {
    super.setAngles(cameraEntityRenderState);
    float f = (0.5F + cameraEntityRenderState.openProgress) * (float) Math.PI;
    float g = -1.0F + MathHelper.sin(f);
    float h = 0.0F;
    if (f > (float) Math.PI) {
      h = MathHelper.sin(cameraEntityRenderState.age * 0.1F) * 0.7F;
    }
    
    this.lid.setPivot(0.0F, 16.0F + MathHelper.sin(f) * 8.0F + h, 0.0F);
    if (cameraEntityRenderState.openProgress > 0.3F) {
      this.lid.yaw = g * g * g * g * (float) Math.PI * 0.125F;
    } else {
      this.lid.yaw = 0.0F;
    }
    
    this.head.pitch = cameraEntityRenderState.pitch * (float) (Math.PI / 180.0);
    this.head.yaw = (cameraEntityRenderState.headYaw - 180.0F - cameraEntityRenderState.shellYaw) * (float) (Math.PI / 180.0);
  }
}
