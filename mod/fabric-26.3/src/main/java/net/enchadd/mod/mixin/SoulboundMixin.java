package net.enchadd.mod.mixin;

import net.enchadd.mod.common.SoulboundBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
abstract class SoulboundMixin {
    @Inject(method = "die", at = @At("HEAD"))
    private void enchadd$captureSoulbound(DamageSource source, CallbackInfo callback) {
        SoulboundBehavior.capture((ServerPlayer) (Object) this);
    }
}
