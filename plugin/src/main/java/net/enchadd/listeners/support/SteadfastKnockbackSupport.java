package net.enchadd.listeners.support;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import net.enchadd.enchants.SteadfastEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SteadfastKnockbackSupport {

    public @Nullable SteadfastContext resolveContext(@NotNull Player player, @NotNull Enchantment enchant) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return null;
        }

        int level = PerformanceUtils.getEnchantLevel(equipment.getBoots(), enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return null;
        }
        return new SteadfastContext(level, pdc);
    }

    public boolean isOnCooldown(@NotNull SteadfastContext context,
                                @NotNull NamespacedKey key,
                                int cooldownTicks) {
        return PerformanceUtils.isOnCooldown(context.pdc(), key, cooldownTicks);
    }

    public boolean shouldTrigger(@NotNull SteadfastEnchant config, int level) {
        double chance = Math.min(0.6, config.getTriggerChance() * level);
        return PerformanceUtils.rollChance(chance);
    }

    public boolean applyKnockbackReduction(@NotNull EntityKnockbackEvent event,
                                           @NotNull SteadfastEnchant config,
                                           int level) {
        Vector knockback = event.getKnockback();
        if (knockback == null) {
            return false;
        }
        double scale = Math.max(0.4, 1.0 - level * config.getReductionPerLevel());
        event.setKnockback(knockback.clone().multiply(scale));
        return true;
    }

    public void triggerCooldown(@NotNull SteadfastContext context, @NotNull NamespacedKey key) {
        PerformanceUtils.setCooldown(context.pdc(), key);
    }

    public record SteadfastContext(int level, @NotNull PersistentDataContainer pdc) {
    }
}
