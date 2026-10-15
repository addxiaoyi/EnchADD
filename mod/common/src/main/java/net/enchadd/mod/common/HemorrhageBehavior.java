package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HemorrhageBehavior {
    private static final ResourceKey<Enchantment> HEMORRHAGE = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath("enchadd", "hemorrhage")
    );
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 60;
    private static final int WITHER_TICKS_PER_LEVEL = 40;
    private static final int MAX_EFFECT_TICKS = 2400;
    private static final double TRIGGER_CHANCE = 0.15;

    private HemorrhageBehavior() {
    }

    public static void onHit(LivingEntity victim, DamageSource source, float damage) {
        if (!(source.getEntity() instanceof ServerPlayer attacker)
                || source.getDirectEntity() != attacker
                || victim == attacker
                || !Float.isFinite(damage)
                || damage <= 0.0f) {
            return;
        }

        Holder<Enchantment> holder = attacker.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(HEMORRHAGE);
        int level = Math.min(MAX_LEVEL,
                EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level <= 0) {
            return;
        }

        long now = attacker.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(attacker.getUUID(), 0L)
                || attacker.getRandom().nextDouble() >= TRIGGER_CHANCE) {
            return;
        }

        int duration = Math.min(MAX_EFFECT_TICKS, level * WITHER_TICKS_PER_LEVEL);
        if (victim.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, 0, false, false, true))) {
            COOLDOWNS.put(attacker.getUUID(), now + COOLDOWN_TICKS);
        }
    }
}
