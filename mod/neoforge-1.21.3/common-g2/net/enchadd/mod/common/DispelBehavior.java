package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DispelBehavior {

    private static final ResourceKey<Enchantment> DISPEL = key("dispel");
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 100;
    private static final double CHANCE_PER_LEVEL = 0.15;
    private static final double MAX_CHANCE = 0.95;
    private static final List<Holder<MobEffect>> POSITIVE_EFFECTS = List.of(
            MobEffects.MOVEMENT_SPEED,
            MobEffects.REGENERATION,
            MobEffects.DAMAGE_RESISTANCE,
            MobEffects.ABSORPTION,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.WATER_BREATHING,
            MobEffects.NIGHT_VISION,
            MobEffects.DAMAGE_BOOST,
            MobEffects.JUMP,
            MobEffects.DIG_SPEED
    );
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    private DispelBehavior() {
    }

    public static void onDamage(LivingEntity victim, DamageSource source) {
        if (victim == null || source == null || !(source.getEntity() instanceof ServerPlayer attacker)
                || source.getDirectEntity() != attacker) {
            return;
        }
        Holder<Enchantment> holder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(DISPEL);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level <= 0) {
            return;
        }
        long now = attacker.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(attacker.getUUID(), 0L)
                || attacker.getRandom().nextDouble() >= Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * level)) {
            return;
        }
        for (Holder<MobEffect> effect : POSITIVE_EFFECTS) {
            if (victim.hasEffect(effect) && victim.removeEffect(effect)) {
                COOLDOWNS.put(attacker.getUUID(), now + COOLDOWN_TICKS);
                return;
            }
        }
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", id));
    }
}
