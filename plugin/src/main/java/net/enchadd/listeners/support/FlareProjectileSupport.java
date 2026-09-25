package net.enchadd.listeners.support;

import net.enchadd.enchants.FlareEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
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

public final class FlareProjectileSupport {

    public void captureLaunchLevel(@NotNull ProjectileLaunchEvent event,
                                   @NotNull Enchantment enchant,
                                   @NotNull NamespacedKey levelKey) {
        if (!(event.getEntity() instanceof Firework firework)) {
            return;
        }
        PersistentDataContainer fireworkPdc = firework.getPersistentDataContainer();
        if (fireworkPdc.has(levelKey, PersistentDataType.INTEGER)) {
            return;
        }
        if (!(firework.getShooter() instanceof LivingEntity shooter)) {
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
        fireworkPdc.set(levelKey, PersistentDataType.INTEGER, level);
    }

    public void applyOnDamage(@NotNull EntityDamageByEntityEvent event,
                              @NotNull NamespacedKey levelKey,
                              @NotNull NamespacedKey cooldownKey,
                              @NotNull FlareEnchant config) {
        if (!(event.getDamager() instanceof Firework firework)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }

        Entity shooterEntity = firework.getShooter() instanceof Entity e ? e : null;
        if (!(shooterEntity instanceof Player shooter)) {
            return;
        }

        Integer level = firework.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
        if (level == null || level <= 0) {
            return;
        }

        level = Math.min(level, config.getMaxLevel());
        int durationTicks = ProjectileStatusSupport.durationTicks(event.getFinalDamage(), level,
                config.getMaxLevel(), config.getGlowSecondsPerLevel());
        if (event.isCancelled() || durationTicks <= 0) return;

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(shooter);
        if (pdc == null) {
            return;
        }
        if (PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) {
            return;
        }

        double chance = ProjectileStatusSupport.chance(config.getTriggerChance(), level, config.getMaxTriggerChance());
        if (!PerformanceUtils.rollChance(chance)) {
            return;
        }

        if (!victim.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, durationTicks, 0, true, true, true))) return;
        PerformanceUtils.setCooldown(pdc, cooldownKey);
    }
}
