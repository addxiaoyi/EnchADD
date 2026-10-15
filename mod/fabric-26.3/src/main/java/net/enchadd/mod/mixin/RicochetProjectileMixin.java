package net.enchadd.mod.mixin;

import net.enchadd.mod.common.RicochetBehavior;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
abstract class RicochetProjectileMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void enchadd$recordRicochet(CallbackInfo callback) {
        RicochetBehavior.recordLaunch((AbstractArrow) (Object) this);
    }
}
