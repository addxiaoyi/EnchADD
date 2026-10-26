package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
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

public final class SunderBehavior {
    private static final ResourceKey<Enchantment> SUNDER = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "sunder"));
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 80;
    private static final double CHANCE_PER_LEVEL = 0.25;
    private static final double MAX_CHANCE = 0.65;
    private static final double BONUS_PER_ARMOR_POINT_PER_LEVEL = 0.01;
    private static final double MAX_BONUS_MULTIPLIER = 0.50;

    private SunderBehavior() {}

    public static float adjustDamage(LivingEntity target, DamageSource source, float amount) {
        if (!(source.getEntity() instanceof ServerPlayer attacker)
                || source.getDirectEntity() != attacker
                || target == attacker
                || !Float.isFinite(amount) || amount <= 0.0f
                || !isAxe(attacker.getMainHandItem().getItem())) return amount;

        double armor = target.getArmorValue();
        if (!Double.isFinite(armor) || armor <= 0.0) return amount;
        Holder<Enchantment> holder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(SUNDER);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level <= 0) return amount;

        long now = attacker.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(attacker.getUUID(), 0L)) return amount;
        double chance = Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * level);
        if (attacker.getRandom().nextDouble() >= chance) return amount;
        double multiplier = Math.min(MAX_BONUS_MULTIPLIER, armor * BONUS_PER_ARMOR_POINT_PER_LEVEL * level);
        double adjusted = amount * (1.0 + multiplier);
        if (!Double.isFinite(adjusted) || adjusted <= amount) return amount;
        COOLDOWNS.put(attacker.getUUID(), now + COOLDOWN_TICKS);
        return (float) adjusted;
    }

    private static boolean isAxe(net.minecraft.world.item.Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath().endsWith("_axe");
    }
}
