package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class PoiseBehavior {

    private static final ResourceKey<Enchantment> POISE = key("poise");
    private static final int MAX_LEVEL = 3;
    private static final double MOVEMENT_THRESHOLD_SQUARED = 0.04;
    private static final double BONUS_DAMAGE_PER_LEVEL = 0.7;
    private static final double MAX_BONUS_DAMAGE = 2.2;

    private PoiseBehavior() {
    }

    public static float adjustDamage(LivingEntity victim, DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f || source == null) {
            return amount;
        }
        if (!(source.getEntity() instanceof LivingEntity attacker)
                || source.getDirectEntity() != attacker
                || !attacker.onGround()
                || attacker.isSprinting()
                || attacker.getDeltaMovement().horizontalDistanceSqr() > MOVEMENT_THRESHOLD_SQUARED) {
            return amount;
        }
        Holder<Enchantment> holder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(POISE);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level <= 0) {
            return amount;
        }
        double bonus = Math.min(MAX_BONUS_DAMAGE, BONUS_DAMAGE_PER_LEVEL * level);
        double adjusted = amount + bonus;
        return Double.isFinite(adjusted) ? (float) adjusted : amount;
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", id));
    }
}
