package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FarshotBehavior {

    private static final ResourceKey<Enchantment> FARSHOT = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath("enchadd", "farshot")
    );
    private static final Map<UUID, Launch> LAUNCHES = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final int MAX_LEVEL = 3;
    private static final int COOLDOWN_TICKS = 100;
    private static final double MIN_DISTANCE = 12.0;
    private static final double MAX_DISTANCE = 40.0;
    private static final double BONUS_PER_LEVEL = 0.18;
    private static final double MAX_BONUS = 0.75;
    private static final int MAX_TRACKED_ARROWS = 4096;
    private static final long MAX_TRACK_TICKS = 12000;

    private FarshotBehavior() {
    }

    public static void recordLaunch(AbstractArrow arrow) {
        if (!(arrow.level() instanceof ServerLevel level) || arrow.getOwner() == null) return;
        if (LAUNCHES.containsKey(arrow.getUUID())) return;
        Entity owner = arrow.getOwner();
        if (!(owner instanceof ServerPlayer player)) return;
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FARSHOT);
        int levelValue = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, player.getMainHandItem()));
        if (levelValue <= 0) return;

        prune(level.getGameTime());
        LAUNCHES.putIfAbsent(arrow.getUUID(), new Launch(player.getUUID(), arrow.position().x(),
                arrow.position().y(), arrow.position().z(), levelValue, level.getGameTime()));
    }

    public static float adjustDamage(LivingEntity victim, DamageSource source, float amount) {
        if (!(victim.level() instanceof ServerLevel level) || !Float.isFinite(amount) || amount <= 0.0f) return amount;
        if (!(source.getDirectEntity() instanceof AbstractArrow arrow)) return amount;
        Launch launch = LAUNCHES.remove(arrow.getUUID());
        if (launch == null || level.getGameTime() - launch.tick() > MAX_TRACK_TICKS) return amount;
        ServerPlayer shooter = level.getServer().getPlayerList().getPlayer(launch.shooter());
        if (shooter == null) return amount;

        long now = level.getGameTime();
        if (now < COOLDOWNS.getOrDefault(shooter.getUUID(), 0L)) return amount;
        double distance = victim.position().distanceTo(new net.minecraft.world.phys.Vec3(launch.x(), launch.y(), launch.z()));
        if (!Double.isFinite(distance) || distance <= MIN_DISTANCE) return amount;

        double factor = Math.min(1.0, (Math.min(distance, MAX_DISTANCE) - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE));
        double bonus = Math.min(MAX_BONUS, BONUS_PER_LEVEL * launch.level() * factor);
        double adjusted = amount * (1.0 + bonus);
        if (!Double.isFinite(adjusted) || adjusted <= amount) return amount;
        COOLDOWNS.put(shooter.getUUID(), now + COOLDOWN_TICKS);
        return (float) adjusted;
    }

    private static void prune(long now) {
        LAUNCHES.entrySet().removeIf(entry -> now - entry.getValue().tick() > MAX_TRACK_TICKS);
        if (LAUNCHES.size() <= MAX_TRACKED_ARROWS) return;
        LAUNCHES.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((left, right) -> Long.compare(left.tick(), right.tick())))
                .limit(LAUNCHES.size() - MAX_TRACKED_ARROWS)
                .map(Map.Entry::getKey)
                .forEach(LAUNCHES::remove);
    }

    private record Launch(UUID shooter, double x, double y, double z, int level, long tick) {
    }
}
