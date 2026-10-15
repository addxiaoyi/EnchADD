package net.enchadd.mod.mixin;

import net.enchadd.mod.common.LucidityBehavior;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(LivingEntity.class)
abstract class LucidityMixin {
    private static final Set<UUID> SUPPRESS = ConcurrentHashMap.newKeySet();

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z", at = @At("HEAD"), cancellable = true)
    private void enchadd$reduceDuration(MobEffectInstance effect, CallbackInfoReturnable<Boolean> callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        UUID id = entity.getUUID();
        if (SUPPRESS.remove(id)) return;
        MobEffectInstance adjusted = LucidityBehavior.adjust(entity, effect);
        if (adjusted == effect || adjusted.getDuration() >= effect.getDuration()) return;
        SUPPRESS.add(id);
        callback.setReturnValue(entity.addEffect(adjusted));
    }
}
