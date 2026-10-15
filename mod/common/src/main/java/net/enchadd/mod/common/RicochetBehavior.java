package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RicochetBehavior {
    private static final ResourceKey<Enchantment> RICOCHET = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath("enchadd", "ricochet")
    );
    private static final Map<UUID, Integer> LEVELS = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> BOUNCED = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final int MAX_LEVEL = 2;
    private static final int COOLDOWN_TICKS = 100;
    private static final double CHANCE_PER_LEVEL = 0.20;
    private static final double MAX_CHANCE = 0.50;
    private static final double SPEED_SCALE = 0.70;
    private static final double RADIUS = 6.0;

    private RicochetBehavior() {}

    public static void recordLaunch(AbstractArrow arrow) {
        if (!(arrow.level() instanceof ServerLevel) || !(arrow.getOwner() instanceof ServerPlayer shooter)) return;
        Holder<Enchantment> holder = shooter.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(RICOCHET);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, shooter.getMainHandItem()));
        if (level > 0) LEVELS.putIfAbsent(arrow.getUUID(), level);
    }

    public static void onHit(LivingEntity victim, DamageSource source) {
        if (!(victim.level() instanceof ServerLevel level) || !(source.getDirectEntity() instanceof AbstractArrow arrow)) return;
        if (BOUNCED.getOrDefault(arrow.getUUID(), false)) return;
        Integer enchantLevel = LEVELS.get(arrow.getUUID());
        if (enchantLevel == null || !(arrow.getOwner() instanceof ServerPlayer shooter)) return;
        long now = level.getGameTime();
        if (now < COOLDOWNS.getOrDefault(shooter.getUUID(), 0L)) return;
        if (shooter.getRandom().nextDouble() >= Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * enchantLevel)) return;

        LivingEntity target = nearestTarget(level, arrow, shooter, victim);
        if (target == null) return;
        Vec3 offset = target.getEyePosition().subtract(arrow.position());
        double speed = arrow.getDeltaMovement().length() * Math.min(1.25, Math.max(0.25, SPEED_SCALE));
        if (!Double.isFinite(speed) || speed <= 0.0 || offset.lengthSqr() <= 0.0) return;
        Vec3 velocity = offset.normalize().scale(speed);

        Arrow bounced = new Arrow(level, arrow.getX(), arrow.getY(), arrow.getZ(), ItemStack.EMPTY, ItemStack.EMPTY);
        bounced.setOwner(shooter);
        bounced.setBaseDamage(baseDamage(arrow));
        bounced.setDeltaMovement(velocity);
        bounced.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        level.addFreshEntity(bounced);
        BOUNCED.put(arrow.getUUID(), true);
        BOUNCED.put(bounced.getUUID(), true);
        COOLDOWNS.put(shooter.getUUID(), now + COOLDOWN_TICKS);
    }

    private static LivingEntity nearestTarget(ServerLevel level, AbstractArrow arrow,
                                              ServerPlayer shooter, LivingEntity original) {
        Vec3 origin = arrow.position();
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(origin, origin).inflate(RADIUS), entity -> entity.isAlive()
                        && entity != shooter && entity != original)) {
            double distance = candidate.position().distanceToSqr(origin);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private static double baseDamage(AbstractArrow arrow) {
        try {
            var field = AbstractArrow.class.getDeclaredField("baseDamage");
            field.setAccessible(true);
            Object value = field.get(arrow);
            return value instanceof Number number && Double.isFinite(number.doubleValue())
                    ? Math.max(0.0, number.doubleValue()) : 2.0;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return 2.0;
        }
    }
}
