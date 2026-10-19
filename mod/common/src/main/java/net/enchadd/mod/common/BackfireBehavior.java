package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BackfireBehavior {
    private static final ResourceKey<Enchantment> BACKFIRE = ResourceKey.create(
            Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "backfire"));
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final int MAX_LEVEL = 1;
    private static final double TRIGGER_CHANCE = 0.05;
    private static final double MAX_CHANCE = 0.5;
    private static final double SELF_DAMAGE_MULTIPLIER = 0.5;

    private BackfireBehavior() {}

    public static void onAttack(LivingEntity attacker, DamageSource source, float finalDamage) {
        if (!(attacker instanceof ServerPlayer player) || !Float.isFinite(finalDamage) || finalDamage <= 0.0f
                || source.getEntity() != player || source.getDirectEntity() != player) return;
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(BACKFIRE);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, player.getMainHandItem()));
        if (level <= 0) return;
        long now = player.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return;
        double chance = Math.min(MAX_CHANCE, TRIGGER_CHANCE * level);
        if (player.getRandom().nextDouble() >= chance) return;
        float selfDamage = (float) Math.min(player.getHealth(), finalDamage * SELF_DAMAGE_MULTIPLIER);
        if (!Float.isFinite(selfDamage) || selfDamage <= 0.0f) return;
        var levelWorld = (net.minecraft.server.level.ServerLevel) player.level();
        long pendingUntil = now + 1L;
        COOLDOWNS.put(player.getUUID(), pendingUntil);
        levelWorld.getServer().execute(() -> {
            if (!player.isAlive()) {
                COOLDOWNS.remove(player.getUUID(), pendingUntil);
                return;
            }
            boolean applied = player.hurtServer(levelWorld, player.damageSources().generic(), selfDamage);
            if (applied) {
                COOLDOWNS.put(player.getUUID(), levelWorld.getGameTime());
            } else {
                COOLDOWNS.remove(player.getUUID(), pendingUntil);
            }
        });
    }
}
