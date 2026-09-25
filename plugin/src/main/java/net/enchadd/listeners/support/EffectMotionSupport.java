package net.enchadd.listeners.support;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

final class EffectMotionSupport {
    private static final double MIN_DIRECTION_LENGTH_SQUARED = 1.0e-12;

    private EffectMotionSupport() {
    }

    static boolean withinRadius(Vector offset, double radius) {
        double distanceSquared = offset.lengthSquared();
        return Double.isFinite(radius) && radius > 0.0
                && Double.isFinite(distanceSquared) && distanceSquared <= radius * radius;
    }

    static @Nullable Vector directedVelocity(Vector offset, double speed) {
        double lengthSquared = offset.lengthSquared();
        if (!Double.isFinite(speed) || speed <= 0.0 || !Double.isFinite(lengthSquared)
                || lengthSquared <= MIN_DIRECTION_LENGTH_SQUARED) {
            return null;
        }
        Vector velocity = offset.clone().normalize().multiply(speed);
        return finite(velocity) ? velocity : null;
    }

    static boolean finite(Vector velocity) {
        return Double.isFinite(velocity.getX()) && Double.isFinite(velocity.getY())
                && Double.isFinite(velocity.getZ());
    }
}
