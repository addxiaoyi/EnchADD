package net.enchadd.mod.mixin;

import net.enchadd.mod.common.HeadDropBehavior;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class HeadDropsMixin {
    @Inject(method = "die", at = @At("HEAD"))
    private void enchadd$headDrops(DamageSource source, CallbackInfo callback) {
        HeadDropBehavior.onDeath((LivingEntity) (Object) this, source);
    }
}
