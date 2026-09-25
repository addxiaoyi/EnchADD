package net.enchadd.listeners.support;

import net.enchadd.enchants.ResonanceEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public final class ResonanceComboSupport {

    public void apply(@NotNull EntityDamageByEntityEvent event,
                      @NotNull Enchantment enchant,
                      @NotNull NamespacedKey comboKey,
                      @NotNull ResonanceEnchant config) {
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

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(attacker);
        if (pdc == null) {
            return;
        }

        double damage = event.getDamage();
        if (!Double.isFinite(damage) || damage <= 0.0) {
            return;
        }
        level = Math.min(level, config.getMaxLevel());
        double bonusDamage = EnchantDamageSupport.bonusDamage(
                level, config.getBonusDamagePerLevel(), config.getMaxBonusDamage());
        double adjusted = EnchantDamageSupport.addBonus(damage, bonusDamage);
        if (!Double.isFinite(adjusted) || adjusted <= damage) {
            return;
        }
        int requiredHits = CombatTriggerSupport.requiredHits(config.getHitsPerProcBase(),
                config.getHitsPerProcReductionPerLevel(), config.getMinHitsPerProc(), level);
        int currentCombo = CombatTriggerSupport.nextCombo(
                pdc.getOrDefault(comboKey, PersistentDataType.INTEGER, 0));
        if (currentCombo < requiredHits) {
            pdc.set(comboKey, PersistentDataType.INTEGER, currentCombo);
            return;
        }

        event.setDamage(adjusted);
        pdc.set(comboKey, PersistentDataType.INTEGER, 0);
    }
}
