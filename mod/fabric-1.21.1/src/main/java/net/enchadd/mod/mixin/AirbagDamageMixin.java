package net.enchadd.mod.mixin;

import net.enchadd.mod.common.AirbagBehavior;
import net.enchadd.mod.common.DamageProtectionBehavior;
import net.enchadd.mod.common.MobExperienceBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
abstract class AirbagDamageMixin {
    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float enchadd$applyAirbag(float amount, DamageSource source) {
        LivingEntity entity = (LivingEntity) (Object) this;
        amount = DamageProtectionBehavior.adjustDamage(entity, source, amount);
        if (entity instanceof net.minecraft.world.entity.player.Player player) amount = MobExperienceBehavior.adjustIncomingDamage(player, amount);
        return AirbagBehavior.adjustDamage(entity, source, amount);
    }
}
