package net.enchadd.mod.mixin;

import net.enchadd.mod.common.BreakguardBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
abstract class BreakguardMixin {
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float enchadd$applyBreakguard(float amount, ServerLevel level, DamageSource source) {
        LivingEntity defender = (LivingEntity) (Object) this;
        if (amount > 0.0f && defender.isDamageSourceBlocked(source)) {
            return BreakguardBehavior.reduceBlockedAmount(defender, source, amount, amount);
        }
        return amount;
    }
}
