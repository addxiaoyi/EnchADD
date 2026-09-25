package net.enchadd.listeners.support;

import net.enchadd.enchants.UndertowEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class UndertowPullSupport {

    public void captureLaunchLevel(@NotNull ProjectileLaunchEvent event,
                                   @NotNull Enchantment enchant,
                                   @NotNull NamespacedKey key) {
        if (!(event.getEntity() instanceof Trident trident)) {
            return;
        }

        PersistentDataContainer tridentPdc = trident.getPersistentDataContainer();
        if (tridentPdc.has(key, PersistentDataType.INTEGER)) {
            return;
        }
        if (!(trident.getShooter() instanceof LivingEntity shooter)) {
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
        tridentPdc.set(key, PersistentDataType.INTEGER, level);
    }

    public void applyOnHit(@NotNull EntityDamageByEntityEvent event,
                           @NotNull NamespacedKey key,
                           @NotNull UndertowEnchant config) {
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (!(event.getDamager() instanceof Trident trident)) {
            return;
        }

        Integer level = trident.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        if (level == null || level <= 0) {
            return;
        }

        if (!(trident.getShooter() instanceof Player shooter)) {
            return;
        }
        if (!PerformanceUtils.isPlayerValid(shooter)) {
            return;
        }
        if (!PerformanceUtils.isEntityValid(victim) || !shooter.getWorld().equals(victim.getWorld())) {
            return;
        }
        level = Math.min(level, config.getMaxLevel());

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(shooter);
        if (pdc == null) {
            return;
        }
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) {
            return;
        }

        double chance = config.getTriggerChance() * level;
        if (!Double.isFinite(chance)) {
            return;
        }
        chance = Math.min(0.85d, Math.max(0.0d, chance));
        if (!PerformanceUtils.rollChance(chance)) {
            return;
        }

        double strength = config.getPullStrengthBase() + 0.05d * level;
        if (!Double.isFinite(strength)) {
            return;
        }
        strength = Math.min(1.2d, Math.max(0.0d, strength));
        Vector pull = EffectMotionSupport.directedVelocity(
                shooter.getLocation().toVector().subtract(victim.getLocation().toVector()), strength);
        if (pull == null) return;
        Vector velocity = victim.getVelocity().add(pull);
        if (!EffectMotionSupport.finite(velocity)) return;
        victim.setVelocity(velocity);
        PerformanceUtils.setCooldown(pdc, key);
    }
}
