package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class HighgroundBehavior {

    private static final ResourceKey<Enchantment> HIGHGROUND = key("highground");
    private static final int MAX_LEVEL = 3;
    private static final double REQUIRED_HEIGHT_ADVANTAGE = 1.25;
    private static final double BONUS_DAMAGE_PER_LEVEL = 0.75;
    private static final double MAX_BONUS_DAMAGE = 2.25;

    private HighgroundBehavior() {
    }

    public static float adjustDamage(LivingEntity victim, DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f || source == null
                || !(source.getEntity() instanceof LivingEntity attacker)
                || source.getDirectEntity() != attacker
                || attacker.getY() - victim.getY() < REQUIRED_HEIGHT_ADVANTAGE) {
            return amount;
        }
        Holder<Enchantment> holder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(HIGHGROUND);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level <= 0) {
            return amount;
        }
        double adjusted = amount + Math.min(MAX_BONUS_DAMAGE, BONUS_DAMAGE_PER_LEVEL * level);
        return Double.isFinite(adjusted) ? (float) adjusted : amount;
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }
}
