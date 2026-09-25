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
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof AbstractArrow arrow)) {
            return;
        }
        if (!(arrow.getShooter() instanceof LivingEntity shooter)) {
            return;
        }

        PersistentDataContainer arrowPdc = arrow.getPersistentDataContainer();
        NamespacedKey checkedKey = new NamespacedKey(levelKey.getNamespace(), levelKey.getKey() + "_launch_checked");
        if (arrowPdc.has(checkedKey, PersistentDataType.BYTE)) return;
        arrowPdc.set(checkedKey, PersistentDataType.BYTE, (byte) 1);

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(shooter);
        if (equipment == null) {
            return;
        }

        int level = Math.min(config.getMaxLevel(), PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant));
        if (level <= 0) {
            return;
        }

        double speed = arrow.getVelocity().length();
        if (!RangedDamageRules.isSteadySpeed(speed)) {
            return;
        }

        if (RangedDamageRules.bonus(level, config.getMaxLevel(), config.getBonusDamagePerLevel(), 1) <= 0) return;
        double chance = ProjectileStatusSupport.chance(config.getTriggerChance(), level, config.getMaxTriggerChance());
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

        arrowPdc.set(levelKey, PersistentDataType.INTEGER, level);
    }

    public void applyBonusDamage(@NotNull EntityDamageByEntityEvent event,
                                 @NotNull NamespacedKey levelKey,
                                 @NotNull SteadyAimEnchant config) {
        if (event.isCancelled() || !Double.isFinite(event.getFinalDamage()) || event.getFinalDamage() <= 0) return;
        if (!(event.getDamager() instanceof AbstractArrow arrow)) {
            return;
        }
        Integer level = arrow.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
        if (level == null || level <= 0) {
            return;
        }

        double base = event.getDamage();
        double bonus = RangedDamageRules.bonus(level, config.getMaxLevel(), config.getBonusDamagePerLevel(), 1);
        double scaled = RangedDamageRules.damage(base, bonus);
        if (Double.compare(scaled, base) == 0) return;
        event.setDamage(scaled);
    }
}
