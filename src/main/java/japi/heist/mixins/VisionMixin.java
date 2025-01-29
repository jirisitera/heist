package japi.heist.mixins;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
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
    Vector3f eyeHeight = new Vector3f(0, entity.getStandingEyeHeight(), 0);
    int red = -65536;
    
    VertexRendering.drawVector(matrices, vertices, eyeHeight, topRight, red);
    VertexRendering.drawVector(matrices, vertices, eyeHeight, topLeft, red);
    VertexRendering.drawVector(matrices, vertices, eyeHeight, bottomRight, red);
    VertexRendering.drawVector(matrices, vertices, eyeHeight, bottomLeft, red);
    
    VertexRendering.drawVector(matrices, vertices, topRight.toVector3f().add(eyeHeight), topLeft.subtract(topRight), red);
    VertexRendering.drawVector(matrices, vertices, topLeft.toVector3f().add(eyeHeight), bottomLeft.subtract(topLeft), red);
    VertexRendering.drawVector(matrices, vertices, bottomLeft.toVector3f().add(eyeHeight), bottomRight.subtract(bottomLeft), red);
    VertexRendering.drawVector(matrices, vertices, bottomRight.toVector3f().add(eyeHeight), topRight.subtract(bottomRight), red);
  }
}
