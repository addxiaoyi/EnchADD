package net.enchadd.mod.mixin;

import net.enchadd.mod.common.BreakguardBehavior;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(LivingEntity.class)
abstract class BreakguardMixin {
    // Vanilla stores shield absorption, then resets incoming damage to zero.
    @ModifyVariable(method = "hurt", at = @At(value = "STORE", ordinal = 0), index = 5,
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtCurrentlyUsedShield(F)V")))
    private float enchadd$reduceAbsorption(float blocked, DamageSource source, float amount) {
        return BreakguardBehavior.reduceBlockedAmount((LivingEntity) (Object) this, source, amount, blocked);
    }

    @ModifyVariable(method = "hurt", at = @At(value = "STORE", ordinal = 0), index = 2,
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtCurrentlyUsedShield(F)V")))
    private float enchadd$retainUnblockedDamage(float remainder, DamageSource source, float amount,
                                               @Local(index = 3) float incoming) {
        float blocked = BreakguardBehavior.reduceBlockedAmount((LivingEntity) (Object) this, source, incoming, incoming);
        return incoming - blocked;
    }
}
