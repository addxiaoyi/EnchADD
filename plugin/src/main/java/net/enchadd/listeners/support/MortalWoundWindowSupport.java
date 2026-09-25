package net.enchadd.listeners.support;

import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;

public final class MortalWoundWindowSupport {
    private final NamespacedKey effectKey;

    public MortalWoundWindowSupport(NamespacedKey key) {
        // A player can be both shooter and victim; never reuse the shooter cooldown key.
        effectKey = new NamespacedKey(key.getNamespace(), key.getKey() + "_antiheal");
    }

    public static int durationTicks(int level, int maxLevel, int seconds, double healScale) {
        if (level <= 0 || maxLevel <= 0 || seconds <= 0
                || !Double.isFinite(healScale) || healScale >= 1.0) return 0;
        return PerformanceUtils.calculateDurationTicksPerLevel(seconds, Math.min(level, maxLevel));
    }

    public boolean apply(PersistentDataContainer pdc, int ticks) {
        return PerformanceUtils.extendWindowUntilTicks(pdc, effectKey, ticks);
    }

    public boolean isActive(PersistentDataContainer pdc) {
        return PerformanceUtils.isWindowActive(pdc, effectKey);
    }
}
