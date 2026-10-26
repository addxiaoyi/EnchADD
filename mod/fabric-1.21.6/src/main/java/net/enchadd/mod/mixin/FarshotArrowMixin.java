package net.enchadd.mod.mixin;

import net.enchadd.mod.common.FarshotBehavior;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
abstract class FarshotArrowMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void enchadd$recordLaunch(CallbackInfo callback) {
        FarshotBehavior.recordLaunch((AbstractArrow) (Object) this);
    }
}
