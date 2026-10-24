package net.enchadd.mod.mixin;

import net.enchadd.mod.common.ParryBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
abstract class ParryDamageMixin {
    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float enchadd$applyParry(float amount, DamageSource source) {
        return ParryBehavior.adjustCounterattack((LivingEntity) (Object) this, source, amount);
    }
}
