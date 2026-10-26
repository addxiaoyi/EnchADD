package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MortalWoundBehavior {

    private static final ResourceKey<Enchantment> MORTAL_WOUND = key("mortal_wound");
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 80;
    private static final int SECONDS_PER_LEVEL = 3;
    private static final double HEALING_SCALE = 0.5;
    private static final double CHANCE_PER_LEVEL = 0.2;
    private static final double MAX_CHANCE = 0.6;
    private static final long MAX_TRACK_TICKS = 12000;
    private static final Map<UUID, Mark> ARROWS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> SHOOTER_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> ANTI_HEAL_UNTIL = new ConcurrentHashMap<>();

    private MortalWoundBehavior() {
    }

    public static void recordLaunch(Projectile projectile) {
        if (!(projectile instanceof AbstractArrow)
                || !(projectile.getOwner() instanceof ServerPlayer shooter)) return;
        Holder<Enchantment> holder = shooter.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(MORTAL_WOUND);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, shooter.getMainHandItem()));
        if (level <= 0) return;
        long now = shooter.level().getGameTime();
        prune(now);
        ARROWS.putIfAbsent(projectile.getUUID(), new Mark(shooter.getUUID(), level, now));
    }

    public static void onHit(LivingEntity victim, DamageSource source, float damage) {
        if (!(source.getDirectEntity() instanceof Projectile projectile) || !Float.isFinite(damage) || damage <= 0.0f) return;
        Mark mark = ARROWS.get(projectile.getUUID());
        if (mark == null || victim.level().getServer() == null) return;
        ServerPlayer shooter = victim.level().getServer().getPlayerList().getPlayer(mark.shooter());
        if (shooter == null) return;
        long now = shooter.level().getGameTime();
        if (now < SHOOTER_COOLDOWNS.getOrDefault(shooter.getUUID(), 0L)) return;
        if (shooter.getRandom().nextDouble() >= Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * mark.level())) return;
        ANTI_HEAL_UNTIL.merge(victim.getUUID(), now + SECONDS_PER_LEVEL * mark.level() * 20L, Math::max);
        SHOOTER_COOLDOWNS.put(shooter.getUUID(), now + COOLDOWN_TICKS);
    }

    public static float adjustHealing(LivingEntity entity, float amount) {
        if (!Float.isFinite(amount) || amount <= 0.0f) return amount;
        long until = ANTI_HEAL_UNTIL.getOrDefault(entity.getUUID(), 0L);
        if (entity.level().getGameTime() >= until) {
            ANTI_HEAL_UNTIL.remove(entity.getUUID(), until);
            return amount;
        }
        return (float) Math.max(0.0, amount * HEALING_SCALE);
    }

    private static void prune(long now) {
        ARROWS.entrySet().removeIf(entry -> now - entry.getValue().tick() > MAX_TRACK_TICKS);
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", id));
    }

    private record Mark(UUID shooter, int level, long tick) {}
}
