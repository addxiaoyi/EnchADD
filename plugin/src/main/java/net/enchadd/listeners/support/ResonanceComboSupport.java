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

        int requiredHits = Math.max(
                config.getMinHitsPerProc(),
                config.getHitsPerProcBase() - (level - 1) * config.getHitsPerProcReductionPerLevel()
        );
        requiredHits = Math.max(1, requiredHits);

        int currentCombo = pdc.getOrDefault(comboKey, PersistentDataType.INTEGER, 0) + 1;
        if (currentCombo < requiredHits) {
            pdc.set(comboKey, PersistentDataType.INTEGER, currentCombo);
            return;
        }

        double bonusDamage = Math.min(config.getMaxBonusDamage(), level * config.getBonusDamagePerLevel());
        if (bonusDamage > 0.0) {
            event.setDamage(event.getDamage() + bonusDamage);
        }
        pdc.set(comboKey, PersistentDataType.INTEGER, 0);
    }
}
