package net.enchadd.mod.mixin;

import net.enchadd.mod.common.SteadfastBehavior;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
abstract class SteadfastMixin {
    @ModifyVariable(method = "knockback", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double enchadd$applySteadfast(double strength) {
        LivingEntity entity = (LivingEntity) (Object) this;
        return entity instanceof Player player ? SteadfastBehavior.reduceStrength(player, strength) : strength;
    }
}
