package net.enchadd.listeners.support;

import net.enchadd.enchants.GreedEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GreedEffectSupport {

    private static final double MAX_EFFECTIVE_DAMAGE_MULTIPLIER = 2.0;

    private final GreedEnchant config;
    private final NamespacedKey untilKey;
    private final NamespacedKey scaleKey;

    public GreedEffectSupport(@NotNull GreedEnchant config,
                       @NotNull NamespacedKey untilKey,
                       @NotNull NamespacedKey scaleKey) {
        this.config = config;
        this.untilKey = untilKey;
        this.scaleKey = scaleKey;
    }

    public void applyXpBonus(@NotNull EntityDeathEvent event, int level) {
        int originalExp = event.getDroppedExp();
        int adjusted = GreedRules.experience(originalExp, level, config.getMaxLevel(),
                config.getXpBonusPerLevel(), config.getMaxXpMultiplier());
        if (adjusted != originalExp) event.setDroppedExp(adjusted);
    }

    public void applyVulnerability(@NotNull Player killer, int level) {
        double finalScale = GreedRules.vulnerability(level, config.getMaxLevel(),
                config.getVulnerabilityPerLevel(), config.getMaxVulnerabilityMultiplier());
        int ticks = GreedRules.durationTicks(level, config.getMaxLevel(), config.getVulnerabilitySecondsPerLevel());
        if (finalScale <= 1.0 || ticks <= 0) return;

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(killer);
        if (pdc == null) {
            return;
        }

        refreshVulnerability(pdc, ticks, finalScale);
    }

    public void refreshVulnerability(@NotNull PersistentDataContainer pdc, int ticks, double scale) {
        if (ticks <= 0 || !Double.isFinite(scale) || scale <= 1.0) return;
        Double previous = getActiveScale(pdc);
        // Switching to a weaker weapon must not erase the price of earlier kills.
        PerformanceUtils.extendWindowUntilTicks(pdc, untilKey, Math.min(1200, ticks));
        pdc.set(scaleKey, PersistentDataType.DOUBLE,
                Math.max(previous == null ? 1.0 : previous, Math.min(MAX_EFFECTIVE_DAMAGE_MULTIPLIER, scale)));
    }

    public boolean applyActiveDamageScale(@NotNull EntityDamageEvent event, @NotNull PersistentDataContainer pdc) {
        Double scale = getActiveScale(pdc);
        if (scale == null) {
            return false;
        }
        double damage = event.getDamage();
        double adjusted = GreedRules.damage(damage, scale);
        if (Double.compare(adjusted, damage) == 0) {
            return false;
        }
        event.setDamage(adjusted);
        return true;
    }

    public @Nullable Double getActiveScale(@NotNull PersistentDataContainer pdc) {
        if (!PerformanceUtils.isWindowActive(pdc, untilKey)) {
            pdc.remove(untilKey);
            pdc.remove(scaleKey);
            return null;
        }
        Double scale = pdc.get(scaleKey, PersistentDataType.DOUBLE);
        if (scale == null || !Double.isFinite(scale) || scale <= 1.0) {
            pdc.remove(untilKey);
            pdc.remove(scaleKey);
            return null;
        }
        return Math.min(MAX_EFFECTIVE_DAMAGE_MULTIPLIER, scale);
    }
}
