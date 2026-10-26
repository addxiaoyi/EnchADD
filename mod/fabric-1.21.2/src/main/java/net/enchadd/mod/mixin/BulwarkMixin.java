package net.enchadd.mod.mixin;

import net.enchadd.mod.common.BulwarkBehavior;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class BulwarkMixin {
    @Inject(method = "isDamageSourceBlocked", at = @At("RETURN"))
    private void enchadd$applyBulwark(DamageSource source, CallbackInfoReturnable<Boolean> callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (Boolean.TRUE.equals(callback.getReturnValue()) && entity instanceof net.minecraft.server.level.ServerPlayer) {
            BulwarkBehavior.maybeApply(entity);
        }
    }
}
