package net.enchadd.mod.mixin;

import net.enchadd.mod.common.HomecomingBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class HomecomingMixin {
    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"))
    private void enchadd$captureHomecoming(DamageSource source, CallbackInfoReturnable<Boolean> callback) {
        if ((Object) this instanceof ServerPlayer player) HomecomingBehavior.capture(player, source);
    }

    @Inject(method = "checkTotemDeathProtection", at = @At("RETURN"))
    private void enchadd$finishHomecoming(DamageSource source, CallbackInfoReturnable<Boolean> callback) {
        if ((Object) this instanceof ServerPlayer player) HomecomingBehavior.finish(player, callback.getReturnValue());
    }
}
