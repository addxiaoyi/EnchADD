package net.enchadd.mod.mixin;

import net.enchadd.mod.common.ShroudBehavior;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
abstract class ShroudMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void enchadd$shroud(LivingEntity target, CallbackInfo callback) {
        if (target != null && ShroudBehavior.shouldBreakTarget((Mob) (Object) this, target)) callback.cancel();
    }
}
