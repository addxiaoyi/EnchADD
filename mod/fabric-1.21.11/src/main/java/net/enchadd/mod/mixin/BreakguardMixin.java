package net.enchadd.mod.mixin;

import net.enchadd.mod.common.BreakguardBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class BreakguardMixin {
    @Inject(method = "applyItemBlocking", at = @At("RETURN"), cancellable = true)
    private void enchadd$applyBreakguard(ServerLevel level, DamageSource source, float incoming,
                                         CallbackInfoReturnable<Float> callback) {
        LivingEntity defender = (LivingEntity) (Object) this;
        callback.setReturnValue(BreakguardBehavior.reduceBlockedAmount(
                defender, source, incoming, callback.getReturnValue()));
    }
}
