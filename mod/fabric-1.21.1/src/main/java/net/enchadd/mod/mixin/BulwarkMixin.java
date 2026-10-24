package net.enchadd.mod.mixin;

import net.enchadd.mod.common.BulwarkBehavior;
import net.enchadd.mod.common.BreakguardBehavior;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class BulwarkMixin {
    @Inject(method = "hurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;hurtCurrentlyUsedShield(F)V"))
    private void enchadd$applyBulwark(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        float blocked = BreakguardBehavior.reduceBlockedAmount(entity, source, amount, amount);
        if (blocked > 0.0f) {
            BulwarkBehavior.maybeApply(entity);
        }
    }
}
