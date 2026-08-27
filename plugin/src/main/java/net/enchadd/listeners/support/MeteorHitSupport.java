package net.enchadd.listeners.support;

import net.enchadd.enchants.MeteorEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;

public final class MeteorHitSupport {

    public void apply(@NotNull EntityDamageByEntityEvent event,
                      @NotNull Enchantment enchant,
                      @NotNull MeteorEnchant config) {
        if (!(event.getDamager() instanceof LivingEntity attacker)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(attacker);
        if (equipment == null) {
            return;
        }

        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant);
        if (level <= 0) {
            return;
        }
        if (attacker.getFallDistance() < config.getRequiredFallDistance()) {
            return;
        }

        double bonusDamage = Math.min(config.getMaxBonusDamage(), level * config.getBonusDamagePerLevel());
        if (bonusDamage <= 0.0) {
            return;
        }

        event.setDamage(event.getDamage() + bonusDamage);
    }
}
