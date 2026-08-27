package net.enchadd.listeners.support;

import net.enchadd.enchants.SteadyAimEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public final class SteadyAimProjectileSupport {

    public void captureLaunchLevel(@NotNull ProjectileLaunchEvent event,
                                   @NotNull Enchantment enchant,
                                   @NotNull NamespacedKey levelKey,
                                   @NotNull NamespacedKey cooldownKey,
                                   @NotNull SteadyAimEnchant config) {
        if (!(event.getEntity() instanceof AbstractArrow arrow)) {
            return;
        }
        if (!(arrow.getShooter() instanceof LivingEntity shooter)) {
            return;
        }

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(shooter);
        if (equipment == null) {
            return;
        }

        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant);
        if (level <= 0) {
            return;
        }

        double speed = arrow.getVelocity().length();
        if (speed < 2.8) {
            return;
        }

        double chance = Math.min(config.getMaxTriggerChance(), config.getTriggerChance() * level);
        if (shooter instanceof Player player) {
            PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
            if (pdc == null) {
                return;
            }
            if (PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) {
                return;
            }
            if (!PerformanceUtils.rollChance(chance)) {
                return;
            }
            PerformanceUtils.setCooldown(pdc, cooldownKey);
        } else if (!PerformanceUtils.rollChance(chance)) {
            return;
        }

        arrow.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level);
    }

    public void applyBonusDamage(@NotNull EntityDamageByEntityEvent event,
                                 @NotNull NamespacedKey levelKey,
                                 @NotNull SteadyAimEnchant config) {
        if (!(event.getDamager() instanceof AbstractArrow arrow)) {
            return;
        }
        Integer level = arrow.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
        if (level == null || level <= 0) {
            return;
        }

        double base = event.getDamage();
        double bonus = Math.max(0.0, level * config.getBonusDamagePerLevel());
        event.setDamage(base * (1.0 + bonus));
    }
}
