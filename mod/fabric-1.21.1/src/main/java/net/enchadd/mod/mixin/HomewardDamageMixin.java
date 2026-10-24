package net.enchadd.mod.mixin;

import net.enchadd.mod.common.HomewardBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
abstract class HomewardDamageMixin {
    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float enchadd$applyHomeward(float amount, DamageSource source) {
        LivingEntity entity = (LivingEntity) (Object) this;
        return entity instanceof ServerPlayer player ? HomewardBehavior.adjustDamage(player, source, amount) : amount;
    }
}
