package net.enchadd.mod.mixin;

import net.enchadd.mod.common.ThirstBehavior;
import net.enchadd.mod.common.MortalWoundBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
abstract class ThirstHealingMixin {
    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float enchadd$adjustThirstHealing(float amount) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof ServerPlayer player)) return amount;
        return MortalWoundBehavior.adjustHealing(player, ThirstBehavior.adjustHealing(player, amount));
    }
}
