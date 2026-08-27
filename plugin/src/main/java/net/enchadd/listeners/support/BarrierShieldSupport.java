package net.enchadd.listeners.support;

import net.enchadd.enchants.BarrierEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class BarrierShieldSupport {

    private final BarrierEnchant config;

    public BarrierShieldSupport(@NotNull BarrierEnchant config) {
        this.config = config;
    }

    public boolean shouldTrigger(int level) {
        if (level <= 0) {
            return false;
        }
        double chance = Math.min(config.getMaxTriggerChance(), config.getTriggerChance() * level);
        if (chance <= 0) {
            return false;
        }
        return PerformanceUtils.rollChance(chance);
    }

    public void knockbackNearby(@NotNull Player player, int level) {
        double radius = Math.max(1.5, config.getKnockbackRadiusPerLevel() * level);
        Location loc = player.getLocation();
        Vector origin = loc.toVector();
        double horizontalStrength = 0.6 + 0.2 * level;
        double verticalStrength = 0.4 + 0.1 * level;

        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(loc, radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof LivingEntity target)) {
                continue;
            }
            if (target.equals(player)) {
                continue;
            }

            Vector direction = target.getLocation().toVector().subtract(origin);
            if (direction.lengthSquared() == 0.0) {
                continue;
            }
            direction = direction.normalize().multiply(horizontalStrength);
            direction.setY(verticalStrength);
            target.setVelocity(direction);
        }
    }
}
