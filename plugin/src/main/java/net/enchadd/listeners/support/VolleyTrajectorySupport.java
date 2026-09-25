package net.enchadd.listeners.support;

import java.util.Random;
import org.bukkit.util.Vector;

final class VolleyTrajectorySupport {
    private static final double MAX_SPREAD = 1.5;

    private VolleyTrajectorySupport() {
    }

    static int extraArrows(int level, int maxLevel, int perLevel, int limit) {
        if (level <= 0 || maxLevel <= 0 || perLevel <= 0 || limit <= 0) return 0;
        return (int) Math.min(limit, (long) Math.min(level, maxLevel) * perLevel);
    }

    static boolean hasVelocity(Vector velocity) {
        double squared = velocity.lengthSquared();
        return Double.isFinite(squared) && squared > 0;
    }

    static Vector spread(Vector velocity, double spread, Random random) {
        if (!hasVelocity(velocity)) return new Vector();
        if (!Double.isFinite(spread) || spread <= 0) return velocity.clone();
        double width = Math.min(MAX_SPREAD, spread);
        Vector offset = new Vector((random.nextDouble() - 0.5) * width,
                (random.nextDouble() - 0.5) * width, (random.nextDouble() - 0.5) * width);
        Vector direction = velocity.clone().add(offset);
        if (!hasVelocity(direction)) return velocity.clone();
        // Spread changes direction; it must not grant extra impact speed.
        return direction.normalize().multiply(velocity.length());
    }
}
