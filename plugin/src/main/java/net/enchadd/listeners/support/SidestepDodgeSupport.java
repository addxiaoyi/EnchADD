package net.enchadd.listeners.support;

import net.enchadd.enchants.SidestepEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SidestepDodgeSupport {

    public @Nullable SidestepContext resolveContext(@NotNull Player player, @NotNull Enchantment enchant) {
        if (!player.isSprinting()) {
            return null;
        }

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return null;
        }

        ItemStack leggings = equipment.getLeggings();
        int level = PerformanceUtils.getEnchantLevel(leggings, enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return null;
        }
        return new SidestepContext(level, pdc);
    }

    public boolean shouldTrigger(@NotNull SidestepContext context,
                                 @NotNull NamespacedKey key,
                                 @NotNull SidestepEnchant config) {
        if (PerformanceUtils.isOnCooldown(context.pdc(), key, config.getCooldownTicks())) {
            return false;
        }
        double chance = Math.min(config.getMaxTriggerChance(), config.getTriggerChance() * context.level());
        return PerformanceUtils.rollChance(chance);
    }

    public void apply(@NotNull EntityDamageByEntityEvent event,
                      @NotNull Player player,
                      @NotNull SidestepContext context,
                      @NotNull NamespacedKey key,
                      @NotNull SidestepEnchant config) {
        double reduction = PerformanceUtils.clamp(config.getDamageReductionPerLevel() * context.level(), 0.0, 0.8);
        double newDamage = Math.max(0.0, event.getDamage() * (1.0 - reduction));
        event.setDamage(newDamage);

        int duration = PerformanceUtils.calculateDurationTicksPerLevel(config.getSpeedSecondsPerLevel(), context.level());
        duration = Math.max(40, duration);
        PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, duration, 0, false, false, true);
        player.addPotionEffect(effect);

        PerformanceUtils.setCooldown(context.pdc(), key);
    }

    public record SidestepContext(int level, @NotNull PersistentDataContainer pdc) {
    }
}
