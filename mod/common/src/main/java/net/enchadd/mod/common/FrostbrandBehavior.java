package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FrostbrandBehavior {
    private static final ResourceKey<Enchantment> FROSTBRAND = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "frostbrand"));
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private FrostbrandBehavior() {}

    public static void onHit(LivingEntity victim, DamageSource source, float damage) {
        if (!(source.getEntity() instanceof ServerPlayer attacker) || source.getDirectEntity() != attacker || victim == attacker) return;
        if (!Float.isFinite(damage) || damage <= 0.0f) return;
        Holder<Enchantment> holder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FROSTBRAND);
        int level = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level <= 0) return;
        long now = attacker.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(attacker.getUUID(), 0L)) return;
        if (attacker.getRandom().nextDouble() >= 0.2) return;
        int duration = Math.min(2400, level * 2 * 20);
        if (victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 0, false, false, true))) COOLDOWNS.put(attacker.getUUID(), now + 40);
    }
}
