package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ResonanceBehavior {

    private static final ResourceKey<Enchantment> RESONANCE = key("resonance");
    private static final int MAX_LEVEL = 3;
    private static final int HITS_PER_PROC_BASE = 5;
    private static final int HITS_REDUCTION_PER_LEVEL = 1;
    private static final int MIN_HITS_PER_PROC = 2;
    private static final double BONUS_DAMAGE_PER_LEVEL = 0.9;
    private static final double MAX_BONUS_DAMAGE = 3.0;
    private static final Map<UUID, Integer> COMBOS = new ConcurrentHashMap<>();

    private ResonanceBehavior() {
    }

    public static float adjustDamage(LivingEntity victim, DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f || source == null
                || !(source.getEntity() instanceof LivingEntity attacker)
                || source.getDirectEntity() != attacker) {
            return amount;
        }
        Holder<Enchantment> holder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(RESONANCE);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level <= 0) {
            return amount;
        }
        int requiredHits = Math.max(MIN_HITS_PER_PROC, HITS_PER_PROC_BASE - (level - 1) * HITS_REDUCTION_PER_LEVEL);
        int combo = Math.min(Integer.MAX_VALUE, COMBOS.getOrDefault(attacker.getUUID(), 0) + 1);
        if (combo < requiredHits) {
            COMBOS.put(attacker.getUUID(), combo);
            return amount;
        }
        COMBOS.put(attacker.getUUID(), 0);
        double bonus = Math.min(MAX_BONUS_DAMAGE, BONUS_DAMAGE_PER_LEVEL * level);
        double adjusted = amount + bonus;
        return Double.isFinite(adjusted) ? (float) adjusted : amount;
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }
}
