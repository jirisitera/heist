package japi.heist.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class MixinEntity {
  @ModifyReturnValue(method = "isSprinting", at = @At("RETURN"))
  @SuppressWarnings("ConstantConditions")
  private boolean hookIsSprinting(boolean original) {
    return original;
  }
}
