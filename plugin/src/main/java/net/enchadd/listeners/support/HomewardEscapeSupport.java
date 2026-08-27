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
import org.bukkit.potion.PotionEffect;
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
        if (enchant == null) {
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

        int level = PerformanceUtils.getEnchantLevel(equipment.getLeggings(), enchant);
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

        PerformanceUtils.setCooldown(pdc, cooldownKey);
        PerformanceUtils.setWindowUntilTicks(pdc, windowKey, config.getEscapeWindowTicks());
        pdc.set(levelKey, PersistentDataType.INTEGER, level);

        int duration = PerformanceUtils.calculateDurationTicksPerLevel(config.getSpeedSecondsPerLevel(), level);
        if (duration <= 0) {
            return;
        }

        PotionEffect speed = new PotionEffect(
                PotionEffectType.SPEED,
                Math.max(20, duration),
                Math.max(0, config.getSpeedAmplifier()),
                false,
                false,
                true
        );
        player.addPotionEffect(speed);
    }

    public void handleFallDamage(@NotNull EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
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
            return;
        }

        Integer level = pdc.get(levelKey, PersistentDataType.INTEGER);
        if (level == null || level <= 0) {
            return;
        }

        double reduction = PerformanceUtils.clamp(
                level * config.getFallDamageReductionPerLevel(),
                0.0,
                config.getMaxFallDamageReduction()
        );
        if (reduction > 0.0) {
            event.setDamage(Math.max(0.0, event.getDamage() * (1.0 - reduction)));
        }
        pdc.remove(windowKey);
        pdc.remove(levelKey);
    }
}
