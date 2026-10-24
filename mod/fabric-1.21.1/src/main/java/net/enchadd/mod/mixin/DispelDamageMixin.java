package net.enchadd.mod.mixin;

import net.enchadd.mod.common.DispelBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class DispelDamageMixin {
    @Inject(method = "hurt", at = @At("HEAD"))
    private void enchadd$dispel(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
        DispelBehavior.onDamage((LivingEntity) (Object) this, source);
    }
}
