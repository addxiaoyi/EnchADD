package net.enchadd.mod.mixin;

import net.enchadd.mod.common.ClairvoyanceBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class ClairvoyanceDamageMixin {
    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void enchadd$revealInvisible(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
        ClairvoyanceBehavior.onDamage((LivingEntity) (Object) this, source);
    }
}
