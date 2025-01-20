package japi.heist.mixins;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
  @ModifyVariable(method = "setSprinting", at = @At("HEAD"), argsOnly = true)
  @SuppressWarnings("ConstantConditions")
  private boolean hookSetSprinting(boolean sprinting) {
    return sprinting;
  }
}
