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
        if (originalExp <= 0) {
            return;
        }
        double baseMultiplier = 1.0 + config.getXpBonusPerLevel() * level;
        double multiplier = Math.min(config.getMaxXpMultiplier(), baseMultiplier);
        if (multiplier <= 0) {
            return;
        }
        int newExp = (int) Math.round(originalExp * multiplier);
        event.setDroppedExp(newExp < 0 ? originalExp : newExp);
    }

    public void applyVulnerability(@NotNull Player killer, int level) {
        double vulnerability = config.getVulnerabilityPerLevel() * level;
        if (vulnerability <= 0) {
            return;
        }
        double maxVulnerability = config.getMaxVulnerabilityMultiplier();
        if (maxVulnerability <= 0) {
            return;
        }
        double scaled = Math.min(maxVulnerability, vulnerability);
        double finalScale = 1.0 + scaled;
        if (finalScale <= 1.0) {
            return;
        }
        int seconds = config.getVulnerabilitySecondsPerLevel() * level;
        if (seconds <= 0) {
            return;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(killer);
        if (pdc == null) {
            return;
        }

        PerformanceUtils.setWindowUntilSeconds(pdc, untilKey, seconds);
        pdc.set(scaleKey, PersistentDataType.DOUBLE, finalScale);
    }

    public boolean applyActiveDamageScale(@NotNull EntityDamageEvent event, @NotNull PersistentDataContainer pdc) {
        Double scale = getActiveScale(pdc);
        if (scale == null) {
            return false;
        }
        double damage = event.getDamage();
        if (damage <= 0) {
            return false;
        }
        event.setDamage(damage * scale);
        return true;
    }

    public @Nullable Double getActiveScale(@NotNull PersistentDataContainer pdc) {
        if (!PerformanceUtils.isWindowActive(pdc, untilKey)) {
            pdc.remove(untilKey);
            pdc.remove(scaleKey);
            return null;
        }
        Double scale = pdc.get(scaleKey, PersistentDataType.DOUBLE);
        if (scale == null || scale <= 1.0) {
            return null;
        }
        return scale;
    }
}
