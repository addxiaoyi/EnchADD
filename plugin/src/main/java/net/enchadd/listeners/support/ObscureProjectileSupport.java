package net.enchadd.listeners.support;

import net.enchadd.enchants.ObscureEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public final class ObscureProjectileSupport {

    public void captureLaunchLevel(@NotNull ProjectileLaunchEvent event,
                                   @NotNull Enchantment enchant,
                                   @NotNull NamespacedKey key) {
        if (!(event.getEntity() instanceof Projectile projectile)) {
            return;
        }
        PersistentDataContainer projectilePdc = projectile.getPersistentDataContainer();
        if (projectilePdc.has(key, PersistentDataType.INTEGER)) {
            return;
        }
        if (!(projectile.getShooter() instanceof LivingEntity shooter)) {
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
        projectilePdc.set(key, PersistentDataType.INTEGER, level);
    }

    public void applyOnHit(@NotNull EntityDamageByEntityEvent event,
                           @NotNull NamespacedKey key,
                           @NotNull ObscureEnchant config) {
        if (!(event.getDamager() instanceof Projectile projectile)) {
            return;
        }
        if (!(projectile.getShooter() instanceof Player shooter)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }

        Integer level = projectile.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        if (level == null || level <= 0) {
            return;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(shooter);
        if (pdc == null) {
            return;
        }
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) {
            return;
        }

        double chance = Math.min(0.75, config.getTriggerChance() * level);
        if (!PerformanceUtils.rollChance(chance)) {
            return;
        }

        int durationTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getBlindSecondsPerLevel(), level);
        PotionEffect effect = new PotionEffect(PotionEffectType.BLINDNESS, durationTicks, 0, false, false, true);
        victim.addPotionEffect(effect);
        PerformanceUtils.setCooldown(pdc, key);
    }
}
