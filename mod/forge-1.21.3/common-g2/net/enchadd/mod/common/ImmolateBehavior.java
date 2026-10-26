package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ImmolateBehavior {
    private static final ResourceKey<Enchantment> IMMOLATE = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "immolate"));
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 60;
    private static final int FIRE_TICKS_PER_LEVEL = 20;
    private static final int MAX_FIRE_TICKS = 2400;
    private static final double CHANCE_PER_LEVEL = 0.20;
    private static final double MAX_CHANCE = 0.60;

    private ImmolateBehavior() {
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
                .getOrThrow(IMMOLATE);
        int level = Math.min(MAX_LEVEL,
                EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level <= 0) {
            return;
        }

        long now = attacker.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(attacker.getUUID(), 0L)) {
            return;
        }

        double chance = Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * level);
        if (attacker.getRandom().nextDouble() >= chance) {
            return;
        }

        int duration = Math.min(MAX_FIRE_TICKS, level * FIRE_TICKS_PER_LEVEL);
        victim.setRemainingFireTicks(Math.max(victim.getRemainingFireTicks(), duration));
        COOLDOWNS.put(attacker.getUUID(), now + COOLDOWN_TICKS);
    }
}
