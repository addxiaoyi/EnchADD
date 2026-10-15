package net.enchadd.mod.mixin;

import net.enchadd.mod.common.BraceBehavior;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
abstract class BraceMixin {
    @ModifyVariable(method = "knockback", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double enchadd$reduceKnockback(double strength) {
        return BraceBehavior.reduceStrength((LivingEntity) (Object) this, (float) strength);
    }
}
