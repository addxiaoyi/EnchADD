package net.enchadd.mod.mixin;

import net.enchadd.mod.common.ProjectileStatusBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
abstract class ProjectileStatusMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void enchadd$recordStatus(CallbackInfo callback) {
        ProjectileStatusBehavior.recordLaunch((Projectile) (Object) this);
    }
}
