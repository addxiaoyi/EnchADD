package net.enchadd.listeners.support;

import net.enchadd.enchants.AfterglideEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AfterglideGlideSupport {

    public @Nullable AfterglideContext resolveContext(@NotNull Player player, @NotNull Enchantment enchant) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return null;
        }

        int level = PerformanceUtils.getEnchantLevel(equipment.getChestplate(), enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return null;
        }
        return new AfterglideContext(level, pdc);
    }

    public boolean isOnCooldown(@NotNull AfterglideContext context,
                                @NotNull NamespacedKey key,
                                int cooldownTicks) {
        return PerformanceUtils.isOnCooldown(context.pdc(), key, cooldownTicks);
    }

    public @Nullable PotionEffect createSlowFallingEffect(@NotNull AfterglideEnchant config, int level) {
        int durationTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getSlowFallingSecondsPerLevel(), level);
        if (durationTicks <= 0) {
            return null;
        }
        int amplifier = Math.max(0, config.getSlowFallingAmplifier());
        return new PotionEffect(PotionEffectType.SLOW_FALLING, durationTicks, amplifier, false, false, true);
    }

    public void apply(@NotNull Player player,
                      @NotNull PotionEffect effect,
                      @NotNull AfterglideContext context,
                      @NotNull NamespacedKey key) {
        player.addPotionEffect(effect);
        PerformanceUtils.setCooldown(context.pdc(), key);
    }

    public record AfterglideContext(int level, @NotNull PersistentDataContainer pdc) {
    }
}
