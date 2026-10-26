package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class RashnessBehavior {
    private static final ResourceKey<Enchantment> RASHNESS = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "rashness"));
    private static final int MAX_LEVEL = 3;
    private static final double REQUIRED_COOLDOWN = 0.85;
    private static final double SELF_DAMAGE_PER_LEVEL = 0.5;
    private static final double MAX_SELF_DAMAGE = 1.5;

    private RashnessBehavior() {}

    public static void maybeApply(LivingEntity victim, DamageSource source) {
        if (!(source.getEntity() instanceof Player attacker) || source.getDirectEntity() != attacker || victim == attacker) return;
        if (attacker.getAttackStrengthScale(0.0f) >= REQUIRED_COOLDOWN) return;
        Holder<Enchantment> holder = attacker.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(RASHNESS);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, attacker.getMainHandItem()));
        if (level <= 0 || !(attacker.level() instanceof ServerLevel levelWorld)) return;
        float selfDamage = (float) Math.min(MAX_SELF_DAMAGE, SELF_DAMAGE_PER_LEVEL * level);
        if (selfDamage > 0.0f) attacker.hurtServer(levelWorld, levelWorld.damageSources().generic(), selfDamage);
    }
}
