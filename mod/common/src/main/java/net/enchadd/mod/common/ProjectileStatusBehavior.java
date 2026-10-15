package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProjectileStatusBehavior {

    private static final ResourceKey<Enchantment> HUNTERS_MARK = key("hunters_mark");
    private static final ResourceKey<Enchantment> OBSCURE = key("obscure");
    private static final ResourceKey<Enchantment> BIND = key("bind");
    private static final ResourceKey<Enchantment> UNDERTOW = key("undertow");
    private static final ResourceKey<Enchantment> FLARE = key("flare");
    private static final ResourceKey<Enchantment> STILLNESS = key("stillness");
    private static final ResourceKey<Enchantment> STEADY_AIM = key("steady_aim");
    private static final Map<UUID, Mark> MARKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Mark> OBSCURES = new ConcurrentHashMap<>();
    private static final Map<UUID, Mark> BINDS = new ConcurrentHashMap<>();
    private static final Map<UUID, Mark> UNDERTOWS = new ConcurrentHashMap<>();
    private static final Map<UUID, Mark> FLARES = new ConcurrentHashMap<>();
    private static final Map<UUID, Mark> STILLNESSES = new ConcurrentHashMap<>();
    private static final Map<UUID, Mark> STEADY_AIMS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> MARK_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> OBSCURE_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> BIND_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> UNDERTOW_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> FLARE_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> STILLNESS_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> STEADY_AIM_COOLDOWNS = new ConcurrentHashMap<>();
    private static final int MAX_MARK_LEVEL = 3;
    private static final int MAX_OBSCURE_LEVEL = 2;
    private static final int MARK_COOLDOWN_TICKS = 60;
    private static final int OBSCURE_COOLDOWN_TICKS = 120;
    private static final int BIND_COOLDOWN_TICKS = 80;
    private static final int UNDERTOW_COOLDOWN_TICKS = 80;
    private static final int MARK_SECONDS_PER_LEVEL = 2;
    private static final int OBSCURE_SECONDS_PER_LEVEL = 2;
    private static final int BIND_SECONDS_PER_LEVEL = 2;
    private static final double MARK_CHANCE = 0.25;
    private static final double OBSCURE_CHANCE = 0.15;
    private static final double BIND_CHANCE = 0.25;
    private static final double MAX_CHANCE = 0.75;
    private static final long MAX_TRACK_TICKS = 12000;
    private static final int MAX_TRACKED_STEADY_AIMS = 4096;
    private static final int FLARE_COOLDOWN_TICKS = 100;
    private static final int FLARE_SECONDS_PER_LEVEL = 4;
    private static final int FLARE_MAX_DURATION_TICKS = 120;
    private static final double FLARE_CHANCE_PER_LEVEL = 0.25;
    private static final double FLARE_MAX_CHANCE = 0.6;
    private static final int STILLNESS_COOLDOWN_TICKS = 80;
    private static final double STILLNESS_CHANCE_PER_LEVEL = 0.25;
    private static final double STILLNESS_MAX_CHANCE = 0.6;
    private static final double STILLNESS_MOVEMENT_THRESHOLD = 0.05;
    private static final double STILLNESS_BONUS_PER_LEVEL = 0.18;
    private static final int STEADY_AIM_COOLDOWN_TICKS = 100;
    private static final double STEADY_AIM_MIN_SPEED = 2.8;
    private static final double STEADY_AIM_CHANCE_PER_LEVEL = 0.30;
    private static final double STEADY_AIM_MAX_CHANCE = 0.60;
    private static final double STEADY_AIM_BONUS_PER_LEVEL = 0.20;

    private ProjectileStatusBehavior() {
    }

    public static void recordLaunch(Projectile projectile) {
        MortalWoundBehavior.recordLaunch(projectile);
        if (!(projectile.getOwner() instanceof ServerPlayer shooter)) return;
        long now = shooter.level().getGameTime();
        prune(now);
        Holder<Enchantment> markHolder = shooter.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(HUNTERS_MARK);
        int markLevel = Math.min(MAX_MARK_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(markHolder, shooter.getMainHandItem()));
        if (projectile instanceof AbstractArrow && markLevel > 0) {
            MARKS.putIfAbsent(projectile.getUUID(), new Mark(shooter.getUUID(), markLevel, now));
        }
        Holder<Enchantment> steadyHolder = shooter.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(STEADY_AIM);
        int steadyLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(steadyHolder, shooter.getMainHandItem()));
        if (projectile instanceof AbstractArrow arrow && steadyLevel > 0
                && arrow.getDeltaMovement().length() >= STEADY_AIM_MIN_SPEED
                && !isCoolingDown(STEADY_AIM_COOLDOWNS, shooter, STEADY_AIM_COOLDOWN_TICKS)
                && roll(shooter, Math.min(STEADY_AIM_MAX_CHANCE, STEADY_AIM_CHANCE_PER_LEVEL * steadyLevel))) {
            if (STEADY_AIMS.size() < MAX_TRACKED_STEADY_AIMS) STEADY_AIMS.putIfAbsent(projectile.getUUID(), new Mark(shooter.getUUID(), steadyLevel, now));
            STEADY_AIM_COOLDOWNS.put(shooter.getUUID(), now + STEADY_AIM_COOLDOWN_TICKS);
        }

        Holder<Enchantment> obscureHolder = shooter.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(OBSCURE);
        int obscureLevel = Math.min(MAX_OBSCURE_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(obscureHolder, shooter.getMainHandItem()));
        if (obscureLevel > 0) {
            OBSCURES.putIfAbsent(projectile.getUUID(), new Mark(shooter.getUUID(), obscureLevel, now));
        }
        int bindLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(
                shooter.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(BIND), shooter.getMainHandItem()));
        if (bindLevel > 0) BINDS.putIfAbsent(projectile.getUUID(), new Mark(shooter.getUUID(), bindLevel, now));
        int undertowLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(
                shooter.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(UNDERTOW), shooter.getMainHandItem()));
        if (projectile instanceof ThrownTrident && undertowLevel > 0) UNDERTOWS.putIfAbsent(projectile.getUUID(), new Mark(shooter.getUUID(), undertowLevel, now));
        int flareLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(
                shooter.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FLARE), shooter.getMainHandItem()));
        if (projectile instanceof FireworkRocketEntity && flareLevel > 0) FLARES.putIfAbsent(projectile.getUUID(), new Mark(shooter.getUUID(), flareLevel, now));
        int stillnessLevel = Math.min(3, EnchantmentHelper.getItemEnchantmentLevel(
                shooter.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(STILLNESS), shooter.getMainHandItem()));
        if (projectile instanceof AbstractArrow && stillnessLevel > 0
                && shooter.getDeltaMovement().horizontalDistanceSqr() <= STILLNESS_MOVEMENT_THRESHOLD
                && !isCoolingDown(STILLNESS_COOLDOWNS, shooter, STILLNESS_COOLDOWN_TICKS)
                && roll(shooter, Math.min(STILLNESS_MAX_CHANCE, STILLNESS_CHANCE_PER_LEVEL * stillnessLevel))) {
            STILLNESSES.putIfAbsent(projectile.getUUID(), new Mark(shooter.getUUID(), stillnessLevel, now));
            STILLNESS_COOLDOWNS.put(shooter.getUUID(), now + STILLNESS_COOLDOWN_TICKS);
        }
    }

    public static void applyOnHit(LivingEntity victim, DamageSource source, float damage) {
        if (!(source.getDirectEntity() instanceof Projectile projectile) || !Float.isFinite(damage) || damage <= 0.0f) return;
        MortalWoundBehavior.onHit(victim, source, damage);
        Mark mark = MARKS.get(projectile.getUUID());
        if (mark != null) applyMark(victim, mark, damage);
        Mark obscure = OBSCURES.get(projectile.getUUID());
        if (obscure != null) applyObscure(victim, obscure, damage);
        Mark bind = BINDS.get(projectile.getUUID());
        if (bind != null) applyBind(victim, bind);
        Mark undertow = UNDERTOWS.get(projectile.getUUID());
        if (undertow != null) applyUndertow(victim, undertow);
        Mark flare = FLARES.get(projectile.getUUID());
        if (flare != null) applyFlare(victim, flare);
    }

    public static float adjustDamage(LivingEntity victim, DamageSource source, float damage) {
        if (!Float.isFinite(damage) || damage <= 0.0f || source == null
                || !(source.getDirectEntity() instanceof Projectile projectile)) return damage;
        Mark steadyAim = STEADY_AIMS.remove(projectile.getUUID());
        if (steadyAim != null) {
            double bonus = Math.min(0.75, STEADY_AIM_BONUS_PER_LEVEL * steadyAim.level());
            double adjusted = damage * (1.0 + bonus);
            if (Double.isFinite(adjusted)) damage = (float) adjusted;
        }
        Mark stillness = STILLNESSES.get(projectile.getUUID());
        if (stillness == null) return damage;
        double adjusted = damage * (1.0 + Math.min(0.75, STILLNESS_BONUS_PER_LEVEL * stillness.level()));
        return Double.isFinite(adjusted) ? (float) adjusted : damage;
    }

    private static void applyMark(LivingEntity victim, Mark mark, float damage) {
        ServerPlayer shooter = findPlayer(victim, mark.shooter());
        if (shooter == null || isCoolingDown(MARK_COOLDOWNS, shooter, MARK_COOLDOWN_TICKS)) return;
        if (!roll(shooter, MARK_CHANCE)) return;
        int duration = Math.min(120 * 20, MARK_SECONDS_PER_LEVEL * mark.level() * 20);
        if (victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0, false, false, true))) {
            MARK_COOLDOWNS.put(shooter.getUUID(), shooter.level().getGameTime() + MARK_COOLDOWN_TICKS);
        }
    }

    private static void applyObscure(LivingEntity victim, Mark obscure, float damage) {
        ServerPlayer shooter = findPlayer(victim, obscure.shooter());
        if (shooter == null || isCoolingDown(OBSCURE_COOLDOWNS, shooter, OBSCURE_COOLDOWN_TICKS)) return;
        if (!roll(shooter, Math.min(MAX_CHANCE, OBSCURE_CHANCE * obscure.level()))) return;
        int duration = Math.min(120 * 20, OBSCURE_SECONDS_PER_LEVEL * obscure.level() * 20);
        if (victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0, false, false, true))) {
            OBSCURE_COOLDOWNS.put(shooter.getUUID(), shooter.level().getGameTime() + OBSCURE_COOLDOWN_TICKS);
        }
    }

    private static void applyBind(LivingEntity victim, Mark bind) {
        ServerPlayer shooter = findPlayer(victim, bind.shooter());
        if (shooter == null || isCoolingDown(BIND_COOLDOWNS, shooter, BIND_COOLDOWN_TICKS)) return;
        if (!roll(shooter, BIND_CHANCE)) return;
        int duration = Math.min(120 * 20, BIND_SECONDS_PER_LEVEL * bind.level() * 20);
        if (victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 0, false, false, true))) {
            BIND_COOLDOWNS.put(shooter.getUUID(), shooter.level().getGameTime() + BIND_COOLDOWN_TICKS);
        }
    }

    private static void applyUndertow(LivingEntity victim, Mark undertow) {
        ServerPlayer shooter = findPlayer(victim, undertow.shooter());
        if (shooter == null || isCoolingDown(UNDERTOW_COOLDOWNS, shooter, UNDERTOW_COOLDOWN_TICKS)) return;
        if (!roll(shooter, Math.min(0.85, 0.25 * undertow.level()))) return;
        Vec3 offset = shooter.position().subtract(victim.position());
        if (offset.lengthSqr() <= 0.0) return;
        double strength = Math.min(1.2, Math.max(0.0, 0.2 + 0.05 * undertow.level()));
        victim.setDeltaMovement(victim.getDeltaMovement().add(offset.normalize().scale(strength)));
        UNDERTOW_COOLDOWNS.put(shooter.getUUID(), shooter.level().getGameTime() + UNDERTOW_COOLDOWN_TICKS);
    }

    private static void applyFlare(LivingEntity victim, Mark flare) {
        ServerPlayer shooter = findPlayer(victim, flare.shooter());
        if (shooter == null || isCoolingDown(FLARE_COOLDOWNS, shooter, FLARE_COOLDOWN_TICKS)) return;
        if (!roll(shooter, Math.min(FLARE_MAX_CHANCE, FLARE_CHANCE_PER_LEVEL * flare.level()))) return;
        int duration = Math.min(FLARE_MAX_DURATION_TICKS, FLARE_SECONDS_PER_LEVEL * flare.level() * 20);
        if (victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0, true, true, true))) {
            FLARE_COOLDOWNS.put(shooter.getUUID(), shooter.level().getGameTime() + FLARE_COOLDOWN_TICKS);
        }
    }


    private static ServerPlayer findPlayer(LivingEntity victim, UUID id) {
        return victim.level().getServer() == null ? null : victim.level().getServer().getPlayerList().getPlayer(id);
    }

    private static boolean isCoolingDown(Map<UUID, Long> cooldowns, ServerPlayer player, int ticks) {
        return player.level().getGameTime() < cooldowns.getOrDefault(player.getUUID(), 0L);
    }

    private static boolean roll(ServerPlayer player, double chance) {
        return chance > 0.0 && player.getRandom().nextDouble() < Math.min(MAX_CHANCE, chance);
    }

    private static void prune(long now) {
        MARKS.entrySet().removeIf(entry -> now - entry.getValue().tick() > MAX_TRACK_TICKS);
        OBSCURES.entrySet().removeIf(entry -> now - entry.getValue().tick() > MAX_TRACK_TICKS);
        BINDS.entrySet().removeIf(entry -> now - entry.getValue().tick() > MAX_TRACK_TICKS);
        UNDERTOWS.entrySet().removeIf(entry -> now - entry.getValue().tick() > MAX_TRACK_TICKS);
        FLARES.entrySet().removeIf(entry -> now - entry.getValue().tick() > MAX_TRACK_TICKS);
        STEADY_AIMS.entrySet().removeIf(entry -> now - entry.getValue().tick() > MAX_TRACK_TICKS);
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", id));
    }

    private record Mark(UUID shooter, int level, long tick) {
    }
}



