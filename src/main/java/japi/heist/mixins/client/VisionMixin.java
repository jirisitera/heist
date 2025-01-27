package japi.heist.mixins.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class VisionMixin {
  @Inject(method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
  private <E extends Entity, S extends EntityRenderState> void hookRenderHitbox(E entity, double x, double y, double z, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityRenderer<? super E, S> renderer, CallbackInfo ci) {
    Vec3d rotation = entity.getRotationVector().normalize().multiply(5);
    Vec3d above = entity.getRotationVector(entity.getPitch() + 90, entity.getYaw()).normalize();
    Vec3d perpendicular = rotation.crossProduct(above).normalize();
    
    Vec3d right = perpendicular.multiply(5);
    Vec3d left = perpendicular.multiply(-5);
    Vec3d top = above.multiply(2.5);
    Vec3d bottom = above.multiply(-2.5);
    
    Vec3d topRight = rotation.add(right).add(top);
    Vec3d topLeft = rotation.add(left).add(top);
    Vec3d bottomRight = rotation.add(right).add(bottom);
    Vec3d bottomLeft = rotation.add(left).add(bottom);
    
    VertexConsumer vertices = vertexConsumers.getBuffer(RenderLayer.getLines());
    Vector3f eyeHeight = new Vector3f(0.0F, entity.getStandingEyeHeight(), 0.0F);
    int color = -65536;
    
    drawLine(matrices,vertices, eyeHeight, topRight,color);
    drawLine(matrices,vertices, eyeHeight, topLeft,color);
    drawLine(matrices,vertices, eyeHeight, bottomRight,color);
    drawLine(matrices,vertices, eyeHeight, bottomLeft,color);
  
  }
  @Unique
  private static void drawLine(MatrixStack matrices, VertexConsumer vertexConsumers, Vector3f offset, Vec3d vec, int argb) {
    MatrixStack.Entry matrix = matrices.peek();
    float x = (float) vec.getX();
    float y = (float) vec.getY();
    float z = (float) vec.getZ();
    vertexConsumers.vertex(matrix, offset).normal(matrix,x,y,z).color(argb);
    vertexConsumers.vertex(matrix, (offset.x() + x), (offset.y() + y), (offset.z() + z)).normal(matrix,x,y,z).color(argb);
  }
}
