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

public final class MeasuredBehavior {

    private static final ResourceKey<Enchantment> MEASURED = key("measured");
    private static final int MAX_LEVEL = 3;
    private static final float REQUIRED_ATTACK_COOLDOWN = 0.9f;
    private static final double BONUS_DAMAGE_PER_LEVEL = 0.8;
    private static final double MAX_BONUS_DAMAGE = 2.4;

    private MeasuredBehavior() {
    }

    public static float adjustDamage(LivingEntity victim, DamageSource source, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f || source == null
                || !(source.getEntity() instanceof ServerPlayer attacker)
                || source.getDirectEntity() != attacker
                || attacker.getAttackStrengthScale(0.0f) < REQUIRED_ATTACK_COOLDOWN) {
            return amount;
        }
        Holder<Enchantment> holder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(MEASURED);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level <= 0) {
            return amount;
        }
        double adjusted = amount + Math.min(MAX_BONUS_DAMAGE, BONUS_DAMAGE_PER_LEVEL * level);
        return Double.isFinite(adjusted) ? (float) adjusted : amount;
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", id));
    }
}
