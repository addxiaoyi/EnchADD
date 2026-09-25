package net.enchadd.listeners.support;

import net.enchadd.enchants.FleetfootEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FleetfootSprintSupport {

    public @Nullable FleetfootContext resolveContext(@NotNull Player player, @NotNull Enchantment enchant) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return null;
        }

        ItemStack boots = equipment.getBoots();
        if (boots == null) {
            return null;
        }

        int level = PerformanceUtils.getEnchantLevel(boots, enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return null;
        }
        return new FleetfootContext(level, pdc);
    }

    public boolean shouldTrigger(@NotNull FleetfootContext context,
                                 @NotNull NamespacedKey key,
                                 int cooldownTicks) {
        return !PerformanceUtils.isOnCooldown(context.pdc(), key, cooldownTicks);
    }

    public void apply(@NotNull PlayerToggleSprintEvent event,
                      @NotNull FleetfootContext context,
                      @NotNull NamespacedKey key,
                      @NotNull FleetfootEnchant config) {
        if (event.isCancelled() || !event.isSprinting()) return;
        int seconds = SprintEffectRules.seconds(context.level(), config.getMaxLevel(), config.getSpeedSecondsPerLevel());
        int amplifier = Math.min(2, Math.max(0, config.getSpeedAmplifier()));
        if (ActiveBuffSupport.apply(event.getPlayer(), PotionEffectType.SPEED, seconds, amplifier)) {
            PerformanceUtils.setCooldown(context.pdc(), key);
        }
    }

    public record FleetfootContext(int level, @NotNull PersistentDataContainer pdc) {
    }
}
