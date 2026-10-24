package net.enchadd.mod.mixin;

import net.enchadd.mod.common.BulwarkBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class BulwarkMixin {
    @Inject(method = "applyItemBlocking(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)F", at = @At("RETURN"))
    private void enchadd$applyBulwark(ServerLevel level, DamageSource source, float incoming,
                                     CallbackInfoReturnable<Float> callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (callback.getReturnValue() > 0.0f && entity instanceof ServerPlayer) {
            BulwarkBehavior.maybeApply(entity);
        }
    }
}
