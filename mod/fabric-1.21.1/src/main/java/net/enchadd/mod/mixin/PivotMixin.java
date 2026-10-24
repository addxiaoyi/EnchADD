package net.enchadd.mod.mixin;

import net.enchadd.mod.common.PivotBehavior;
import net.enchadd.mod.common.RiposteBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class PivotMixin {
    @Inject(method = "hurt", at = @At("TAIL"))
    private void enchadd$pivot(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
        if ((Object) this instanceof ServerPlayer player && callback.getReturnValueZ() && player.isBlocking()) {
            PivotBehavior.blocked(player, player.getUseItem());
            RiposteBehavior.blocked(player, player.getUseItem(), source.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker ? attacker : null);
        }
    }
}
