package net.enchadd.mod.mixin;

import net.enchadd.mod.common.LodestarBehavior;
import net.enchadd.mod.common.MeleeEffectBehavior;
import net.enchadd.mod.common.HighgroundBehavior;
import net.enchadd.mod.common.DamageProtectionBehavior;
import net.enchadd.mod.common.MeasuredBehavior;
import net.enchadd.mod.common.ResonanceBehavior;
import net.enchadd.mod.common.SpearDefenseBehavior;
import net.enchadd.mod.common.SpearCombatBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
abstract class ComboDamageMixin {
    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float enchadd$applyComboEffects(float amount, DamageSource source) {
        LivingEntity victim = (LivingEntity) (Object) this;
        float adjusted = HighgroundBehavior.adjustDamage(victim, source, amount);
        adjusted = MeasuredBehavior.adjustDamage(victim, source, adjusted);
        adjusted = LodestarBehavior.adjustDamage(victim, source, adjusted);
        adjusted = ResonanceBehavior.adjustDamage(victim, source, adjusted);
        adjusted = MeleeEffectBehavior.adjustDamage(victim, source, adjusted);
        adjusted = DamageProtectionBehavior.adjustDamage(victim, source, adjusted);
        adjusted = SpearDefenseBehavior.adjustDamage(victim, source, adjusted);
        LivingEntity attacker = source.getEntity() instanceof LivingEntity living ? living : null;
        if (attacker != null && source.getDirectEntity() == attacker) SpearCombatBehavior.onHit(victim, attacker, source, amount);
        return adjusted;
    }
}
