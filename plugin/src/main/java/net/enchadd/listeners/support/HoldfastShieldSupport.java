package net.enchadd.listeners.support;

import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import net.enchadd.enchants.HoldfastEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HoldfastShieldSupport {

    public boolean isAxeAttack(@Nullable Entity damager) {
        if (!(damager instanceof LivingEntity attacker)) {
            return false;
        }
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(attacker);
        if (equipment == null) {
            return false;
        }
        ItemStack weapon = equipment.getItemInMainHand();
        return weapon != null && weapon.getType().name().endsWith("_AXE");
    }

    public @Nullable HoldfastContext resolveContext(@NotNull Player player, @NotNull Enchantment enchant) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return null;
        }

        ItemStack shield = equipment.getItemInOffHand();
        if (shield == null || shield.getType().isAir()) {
            return null;
        }

        int level = PerformanceUtils.getEnchantLevel(shield, enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return null;
        }
        return new HoldfastContext(level, pdc);
    }

    public boolean shouldTrigger(@NotNull HoldfastContext context,
                                 @NotNull NamespacedKey key,
                                 @NotNull HoldfastEnchant config) {
        if (PerformanceUtils.isOnCooldown(context.pdc(), key, config.getCooldownTicks())) {
            return false;
        }
        double chance = Math.min(config.getMaxTriggerChance(), config.getTriggerChance() * context.level());
        return PerformanceUtils.rollChance(chance);
    }

    public void apply(@NotNull PlayerShieldDisableEvent event,
                      @NotNull HoldfastContext context,
                      @NotNull NamespacedKey key) {
        event.setCooldown(0);
        event.setCancelled(true);
        PerformanceUtils.setCooldown(context.pdc(), key);
    }

    public record HoldfastContext(int level, @NotNull PersistentDataContainer pdc) {
    }
}
