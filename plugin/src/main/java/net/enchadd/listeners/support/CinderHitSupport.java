package net.enchadd.listeners.support;

import net.enchadd.enchants.CinderEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;

public final class CinderHitSupport {

    public void apply(@NotNull EntityDamageByEntityEvent event,
                      @NotNull Enchantment enchant,
                      @NotNull CinderEnchant config) {
        if (!(event.getDamager() instanceof LivingEntity attacker)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity target)) {
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
        if (target.getFireTicks() < config.getRequiredFireTicks()) {
            return;
        }

        double bonus = EnchantDamageSupport.bonusDamage(
                level, config.getBonusDamagePerLevel(), config.getMaxBonusDamage());
        double damage = event.getDamage();
        double adjusted = EnchantDamageSupport.addBonus(damage, bonus);
        if (!Double.isFinite(adjusted) || adjusted <= damage) {
            return;
        }
        event.setDamage(adjusted);
    }
}
