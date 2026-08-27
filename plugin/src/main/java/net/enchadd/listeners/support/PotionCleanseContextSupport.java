package net.enchadd.listeners.support;

import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class PotionCleanseContextSupport {

    public @Nullable DispelContext resolveDispelContext(@NotNull EntityDamageByEntityEvent event,
                                                        @NotNull Enchantment enchant) {
        if (!(event.getDamager() instanceof Player player)) {
            return null;
        }
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return null;
        }

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return null;
        }
        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return null;
        }
        return new DispelContext(victim, level, pdc);
    }

    public @Nullable PurifyContext resolvePurifyContext(@NotNull PlayerItemConsumeEvent event,
                                                        @NotNull Enchantment enchant) {
        Player player = event.getPlayer();
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return null;
        }
        int level = PerformanceUtils.getEnchantLevel(equipment.getHelmet(), enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return null;
        }
        return new PurifyContext(player, level, pdc);
    }

    public @Nullable PotionEffectType findFirstEffect(@NotNull LivingEntity entity,
                                                      @NotNull List<PotionEffectType> effects) {
        for (PotionEffectType type : effects) {
            if (entity.hasPotionEffect(type)) {
                return type;
            }
        }
        return null;
    }

    public record DispelContext(@NotNull LivingEntity victim,
                                int level,
                                @NotNull PersistentDataContainer pdc) {
    }

    public record PurifyContext(@NotNull Player player,
                                int level,
                                @NotNull PersistentDataContainer pdc) {
    }
}
