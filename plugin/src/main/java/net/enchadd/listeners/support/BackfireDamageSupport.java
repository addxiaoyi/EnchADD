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
        if (event.isCancelled()) return;
        double selfDamage = CursePenaltySupport.selfDamage(event.getFinalDamage(), config.getSelfDamageMultiplier());
        if (selfDamage <= 0) return;
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

        double chance = CursePenaltySupport.backfireChance(level, config.getMaxLevel(),
                config.getTriggerChancePerLevel(), config.getMaxTriggerChance());
        if (chance <= 0 || !PerformanceUtils.rollChance(chance)) {
            return;
        }

        player.damage(selfDamage);
        PerformanceUtils.setCooldown(pdc, key);
    }
}
