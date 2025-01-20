package japi.heist.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public abstract class HungerMixin {
  @Inject(method = "setSaturationLevel", at = @At("HEAD"), cancellable = true)
  private void hookSetSaturationLevel(float saturationLevel, CallbackInfo ci) {
    ci.cancel();
  }
  
  @Inject(method = "update", at = @At("HEAD"), cancellable = true)
  private void hookUpdate(ServerPlayerEntity player, CallbackInfo ci) {
    ci.cancel();
  }
  
  @Inject(method = "addExhaustion", at = @At("HEAD"), cancellable = true)
  private void hookAddExhaustion(float exhaustion, CallbackInfo ci) {
    ci.cancel();
  }
  
  @Inject(method = "setFoodLevel", at = @At("HEAD"), cancellable = true)
  private void hookSetFoodLevel(int foodLevel, CallbackInfo ci) {
    ci.cancel();
  }
  
  @Inject(method = "readNbt", at = @At("HEAD"), cancellable = true)
  private void hookReadNbt(NbtCompound nbt, CallbackInfo ci) {
    ci.cancel();
  }
  
  @Inject(method = "writeNbt", at = @At("HEAD"), cancellable = true)
  private void hookWriteNbt(NbtCompound nbt, CallbackInfo ci) {
    ci.cancel();
  }
  
  @Inject(method = "add", at = @At("HEAD"), cancellable = true)
  private void hookAdd(int food, float saturationModifier, CallbackInfo ci) {
    ci.cancel();
  }
  
  @Inject(method = "eat", at = @At("HEAD"), cancellable = true)
  private void hookEat(FoodComponent foodComponent, CallbackInfo ci) {
    ci.cancel();
  }
  
  @ModifyReturnValue(method = "getFoodLevel", at = @At("RETURN"))
  private int hookGetFoodLevel(int original) {
    return 20;
  }
  
  @ModifyReturnValue(method = "getSaturationLevel", at = @At("RETURN"))
  private float hookGetSaturationLevel(float original) {
    return 20;
  }
}
