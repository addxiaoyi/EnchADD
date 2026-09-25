package net.enchadd.listeners.support;

import net.enchadd.enchants.HomewardEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HomewardEscapeSupport {

    private final HomewardEnchant config;
    private final NamespacedKey cooldownKey;
    private final NamespacedKey windowKey;
    private final NamespacedKey levelKey;

    public HomewardEscapeSupport(@NotNull HomewardEnchant config,
                                 @NotNull NamespacedKey cooldownKey,
                                 @NotNull NamespacedKey windowKey,
                                 @NotNull NamespacedKey levelKey) {
        this.config = config;
        this.cooldownKey = cooldownKey;
        this.windowKey = windowKey;
        this.levelKey = levelKey;
    }

    public void handleCombatDamage(@NotNull EntityDamageByEntityEvent event, @Nullable Enchantment enchant) {
        if (enchant == null || !HomewardRules.hasDamage(event.isCancelled(), event.getDamage(), event.getFinalDamage())) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!(event.getDamager() instanceof org.bukkit.entity.LivingEntity) && !(event.getDamager() instanceof Projectile)) {
            return;
        }
        if (!player.isSprinting()) {
            return;
        }

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return;
        }

        int level = HomewardRules.effectiveLevel(
                PerformanceUtils.getEnchantLevel(equipment.getLeggings(), enchant), config.getMaxLevel());
        if (level <= 0) {
            return;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return;
        }
        if (PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) {
            return;
        }

        int seconds = HomewardRules.speedSeconds(level, config.getMaxLevel(), config.getSpeedSecondsPerLevel());
        boolean speedApplied = ActiveBuffSupport.apply(player, PotionEffectType.SPEED, seconds,
                Math.min(2, Math.max(0, config.getSpeedAmplifier())));
        boolean windowApplied = armFallProtection(pdc, level);
        if (speedApplied || windowApplied) {
            PerformanceUtils.setCooldown(pdc, cooldownKey);
        }
    }

    private boolean armFallProtection(PersistentDataContainer pdc, int level) {
        int ticks = HomewardRules.windowTicks(config.getEscapeWindowTicks());
        double reduction = HomewardRules.fallReduction(level, config.getMaxLevel(),
                config.getFallDamageReductionPerLevel(), config.getMaxFallDamageReduction());
        if (ticks <= 0 || reduction <= 0) return false;

        boolean active = PerformanceUtils.isWindowActive(pdc, windowKey);
        int previous = pdc.getOrDefault(levelKey, PersistentDataType.INTEGER, 0);
        // A weaker loadout cannot keep an earlier, stronger rescue alive.
        if (!HomewardRules.canRefreshWindow(level, previous, active, config.getMaxLevel())) return false;
        int retained = HomewardRules.windowLevel(level, previous, active, config.getMaxLevel());
        boolean extended = PerformanceUtils.extendWindowUntilTicks(pdc, windowKey, ticks);
        pdc.set(levelKey, PersistentDataType.INTEGER, retained);
        return extended || retained != previous;
    }

    public void handleFallDamage(@NotNull EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL
                || !HomewardRules.hasDamage(event.isCancelled(), event.getDamage(), event.getFinalDamage())) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return;
        }
        if (!PerformanceUtils.isWindowActive(pdc, windowKey)) {
            pdc.remove(levelKey);
            return;
        }

        Integer level = pdc.get(levelKey, PersistentDataType.INTEGER);
        if (level == null || level <= 0) {
            return;
        }

        double reduction = HomewardRules.fallReduction(level, config.getMaxLevel(),
                config.getFallDamageReductionPerLevel(), config.getMaxFallDamageReduction());
        double damage = HomewardRules.reducedFallDamage(event.getDamage(), event.getFinalDamage(), reduction);
        if (Double.compare(damage, event.getDamage()) == 0) return;

        event.setDamage(damage);
        pdc.remove(windowKey);
        pdc.remove(levelKey);
    }
}
