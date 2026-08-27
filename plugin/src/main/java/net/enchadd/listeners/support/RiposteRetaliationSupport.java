package net.enchadd.listeners.support;

import net.enchadd.enchants.RiposteEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RiposteRetaliationSupport {

    public @Nullable RiposteContext resolveContext(@NotNull Player victim,
                                                   @NotNull LivingEntity attacker,
                                                   @NotNull Enchantment enchant) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(victim);
        if (equipment == null) {
            return null;
        }

        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInOffHand(), enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(victim);
        if (pdc == null) {
            return null;
        }
        return new RiposteContext(attacker, level, pdc);
    }

    public boolean isOnCooldown(@NotNull RiposteContext context,
                                @NotNull NamespacedKey key,
                                int cooldownTicks) {
        return PerformanceUtils.isOnCooldown(context.pdc(), key, cooldownTicks);
    }

    public boolean shouldTrigger(@NotNull RiposteEnchant config, int level) {
        double chance = Math.min(0.85, config.getTriggerChance() * level);
        return PerformanceUtils.rollChance(chance);
    }

    public @NotNull PotionEffect createWeaknessEffect(@NotNull RiposteEnchant config, int level) {
        int durationTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getWeaknessSecondsPerLevel(), level);
        return new PotionEffect(PotionEffectType.WEAKNESS, durationTicks, 0, false, false, true);
    }

    public void apply(@NotNull RiposteContext context,
                      @NotNull PotionEffect effect,
                      @NotNull NamespacedKey key) {
        context.attacker().addPotionEffect(effect);
        PerformanceUtils.setCooldown(context.pdc(), key);
    }

    public record RiposteContext(@NotNull LivingEntity attacker,
                                 int level,
                                 @NotNull PersistentDataContainer pdc) {
    }
}
