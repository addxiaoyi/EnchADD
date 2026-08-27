package net.enchadd.listeners.support;

import net.enchadd.enchants.BindEnchant;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public final class BindProjectileSupport {

    private final BindEnchant config;
    private final NamespacedKey key;

    public BindProjectileSupport(@NotNull BindEnchant config, @NotNull NamespacedKey key) {
        this.config = config;
        this.key = key;
    }

    public void handleShoot(@NotNull ProjectileLaunchEvent event, @NotNull Enchantment enchant) {
        if (!(event.getEntity() instanceof AbstractArrow arrow)) {
            return;
        }
        PersistentDataContainer arrowPdc = arrow.getPersistentDataContainer();
        if (arrowPdc.has(key, PersistentDataType.INTEGER)) {
            return;
        }
        if (!(event.getEntity().getShooter() instanceof LivingEntity shooter)) {
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
        arrowPdc.set(key, PersistentDataType.INTEGER, level);
    }

    public void handleHit(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (!(event.getDamager() instanceof AbstractArrow arrow)) {
            return;
        }

        PersistentDataContainer arrowPdc = arrow.getPersistentDataContainer();
        Integer level = arrowPdc.get(key, PersistentDataType.INTEGER);
        if (level == null || level <= 0) {
            return;
        }

        Player shooter = arrow.getShooter() instanceof Player p ? p : null;
        if (!PerformanceUtils.isPlayerValid(shooter)) {
            return;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(shooter);
        if (pdc == null) {
            return;
        }
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) {
            return;
        }
        if (!PerformanceUtils.rollChance(config.getTriggerChance())) {
            return;
        }

        int durationTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getSlowSecondsPerLevel(), level);
        PotionEffect slowness = new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 0, false, false, true);
        victim.addPotionEffect(slowness);
        PerformanceUtils.setCooldown(pdc, key);
    }
}
