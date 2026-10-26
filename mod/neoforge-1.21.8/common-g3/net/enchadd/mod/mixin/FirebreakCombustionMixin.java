package net.enchadd.mod.mixin;

import net.enchadd.mod.common.FirebreakBehavior;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
abstract class FirebreakCombustionMixin {
    @ModifyVariable(method = "igniteForSeconds", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float enchadd$shortenCombustion(float seconds) {
        return FirebreakBehavior.adjustIgnitionSeconds((Entity) (Object) this, seconds);
    }
}
