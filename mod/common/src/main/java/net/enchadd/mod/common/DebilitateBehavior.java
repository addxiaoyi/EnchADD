package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DebilitateBehavior {
    private static final ResourceKey<Enchantment> DEBILITATE = ResourceKey.create(
            Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "debilitate"));
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 100;
    private static final int WEAKNESS_TICKS_PER_LEVEL = 40;
    private static final double CHANCE_PER_LEVEL = 0.30;
    private static final double MAX_CHANCE = 0.60;

    private DebilitateBehavior() {}

    public static void onDamage(LivingEntity wearer, DamageSource source, float damage) {
        if (!(wearer.level() instanceof ServerLevel level)
                || !Float.isFinite(damage) || damage <= 0.0f
                || !(source.getEntity() instanceof LivingEntity attacker)
                || source.getDirectEntity() != attacker) return;

        Holder<Enchantment> holder = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(DEBILITATE);
        int enchantmentLevel = Math.min(MAX_LEVEL,
                EnchantmentHelper.getItemEnchantmentLevel(holder, wearer.getItemBySlot(EquipmentSlot.CHEST)));
        if (enchantmentLevel <= 0) return;

        long now = level.getGameTime();
        if (now < COOLDOWNS.getOrDefault(wearer.getUUID(), 0L)) return;
        double chance = Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * enchantmentLevel);
        if (wearer.getRandom().nextDouble() >= chance) return;

        int duration = WEAKNESS_TICKS_PER_LEVEL * enchantmentLevel;
        if (attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0, false, false, true))) {
            COOLDOWNS.put(wearer.getUUID(), now + COOLDOWN_TICKS);
        }
    }
}
