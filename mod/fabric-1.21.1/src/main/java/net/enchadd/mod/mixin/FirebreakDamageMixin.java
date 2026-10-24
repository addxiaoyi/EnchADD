package net.enchadd.mod.mixin;
import net.enchadd.mod.common.FirebreakBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
@Mixin(LivingEntity.class)
abstract class FirebreakDamageMixin {
    @ModifyVariable(method="hurt", at=@At("HEAD"), argsOnly=true, ordinal=0)
    private float enchadd$adjustFireDamage(float amount, DamageSource source) {
        return FirebreakBehavior.adjustDamage((LivingEntity)(Object)this, source, amount);
    }
}
