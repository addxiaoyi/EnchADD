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

public final class LodestarBehavior {

    private static final ResourceKey<Enchantment> LODESTAR = key("lodestar");
    private static final int MAX_LEVEL = 3;
    private static final int FOCUS_WINDOW_TICKS = 80;
    private static final double BONUS_DAMAGE_PER_LEVEL = 0.75;
    private static final double MAX_BONUS_DAMAGE = 2.25;
    private static final Map<UUID, Focus> FOCUS = new ConcurrentHashMap<>();

    private LodestarBehavior() {
    }

    public static float adjustDamage(LivingEntity victim, DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f || source == null
                || !(source.getEntity() instanceof LivingEntity attacker)
                || source.getDirectEntity() != attacker) {
            return amount;
        }
        Holder<Enchantment> holder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(LODESTAR);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level <= 0) {
            return amount;
        }
        long now = attacker.level().getGameTime();
        Focus previous = FOCUS.get(attacker.getUUID());
        boolean focused = previous != null && previous.target().equals(victim.getUUID()) && now <= previous.until();
        FOCUS.put(attacker.getUUID(), new Focus(victim.getUUID(), now + FOCUS_WINDOW_TICKS));
        if (!focused) {
            return amount;
        }
        double bonus = Math.min(MAX_BONUS_DAMAGE, BONUS_DAMAGE_PER_LEVEL * level);
        double adjusted = amount + bonus;
        return Double.isFinite(adjusted) ? (float) adjusted : amount;
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }

    private record Focus(UUID target, long until) {}
}
