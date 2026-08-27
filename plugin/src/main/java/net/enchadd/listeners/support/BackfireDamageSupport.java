package net.enchadd.listeners.support;

import net.enchadd.enchants.BackfireEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public final class BackfireDamageSupport {

    public void apply(@NotNull EntityDamageByEntityEvent event,
                      @NotNull Enchantment enchant,
                      @NotNull org.bukkit.NamespacedKey key,
                      @NotNull BackfireEnchant config) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return;
        }

        ItemStack mainHand = equipment.getItemInMainHand();
        if (mainHand == null) {
            return;
        }

        int level = PerformanceUtils.getEnchantLevel(mainHand, enchant);
        if (level <= 0) {
            return;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return;
        }
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) {
            return;
        }

        double chance = config.getTriggerChancePerLevel() * level;
        double maxChance = config.getMaxTriggerChance();
        if (maxChance > 0) {
            chance = Math.min(maxChance, chance);
        }
        if (chance <= 0 || !PerformanceUtils.rollChance(chance)) {
            return;
        }

        double damage = event.getFinalDamage();
        if (damage <= 0) {
            return;
        }

        double scale = config.getSelfDamageMultiplier();
        if (scale <= 0) {
            return;
        }

        double selfDamage = damage * scale;
        if (selfDamage <= 0) {
            return;
        }

        player.damage(selfDamage);
        PerformanceUtils.setCooldown(pdc, key);
    }
}
