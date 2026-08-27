package net.enchadd.listeners.support;

import net.enchadd.enchants.WingguardEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WingguardRescueSupport {

    public @Nullable WingguardContext resolveContext(@NotNull Player player, @NotNull Enchantment enchant) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return null;
        }

        ItemStack chest = equipment.getChestplate();
        int level = chest == null ? 0 : PerformanceUtils.getEnchantLevel(chest, enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return null;
        }
        return new WingguardContext(level, pdc);
    }

    public boolean isLethal(@NotNull Player player, double finalDamage) {
        return player.getHealth() - finalDamage <= 0.0;
    }

    public boolean shouldTrigger(@NotNull WingguardContext context,
                                 @NotNull NamespacedKey key,
                                 @NotNull WingguardEnchant config) {
        if (PerformanceUtils.isOnCooldown(context.pdc(), key, config.getCooldownTicks())) {
            return false;
        }
        double chance = Math.min(config.getMaxTriggerChance(), config.getTriggerChance() * context.level());
        return PerformanceUtils.rollChance(chance);
    }

    public void applyRescue(@NotNull Player player,
                            @NotNull WingguardContext context,
                            @NotNull NamespacedKey key,
                            @NotNull WingguardEnchant config) {
        int duration = PerformanceUtils.calculateDurationTicksPerLevel(config.getSafeSecondsPerLevel(), context.level());
        PotionEffect slowFalling = new PotionEffect(PotionEffectType.SLOW_FALLING, duration, 0, false, false, true);
        PotionEffect resistance = new PotionEffect(PotionEffectType.RESISTANCE, duration, 0, false, false, true);
        player.addPotionEffect(slowFalling);
        player.addPotionEffect(resistance);
        PerformanceUtils.setCooldown(context.pdc(), key);
    }

    public record WingguardContext(int level, @NotNull PersistentDataContainer pdc) {
    }
}
