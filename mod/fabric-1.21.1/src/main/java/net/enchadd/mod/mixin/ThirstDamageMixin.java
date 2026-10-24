package net.enchadd.mod.mixin;

import net.enchadd.mod.common.ThirstBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class ThirstDamageMixin {
    @Inject(method = "hurt", at = @At("HEAD"))
    private void enchadd$markThirst(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
        ThirstBehavior.markFromDamage((LivingEntity) (Object) this, source, amount);
    }
}
