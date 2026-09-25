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
        double chance = DefenseEffectRules.chance(context.level(), config.getMaxLevel(),
                config.getTriggerChance(), config.getMaxTriggerChance());
        return PerformanceUtils.rollChance(chance);
    }

    public void apply(@NotNull EntityDamageByEntityEvent event,
                      @NotNull Player player,
                      @NotNull SidestepContext context,
                      @NotNull NamespacedKey key,
                      @NotNull SidestepEnchant config) {
        if (event.isCancelled() || !Double.isFinite(event.getFinalDamage()) || event.getFinalDamage() <= 0) return;
        double damage = event.getDamage();
        if (!Double.isFinite(damage) || damage <= 0.0d) {
            return;
        }
        double newDamage = DefenseEffectRules.damage(damage, context.level(), config.getMaxLevel(),
                config.getDamageReductionPerLevel());
        boolean reduced = newDamage < damage;
        if (reduced) event.setDamage(newDamage);

        int seconds = DefenseEffectRules.seconds(context.level(), config.getMaxLevel(), config.getSpeedSecondsPerLevel());
        boolean accelerated = seconds > 0 && ActiveBuffSupport.apply(player, PotionEffectType.SPEED, Math.max(2, seconds), 0);
        if (reduced || accelerated) PerformanceUtils.setCooldown(context.pdc(), key);
    }

    public record SidestepContext(int level, @NotNull PersistentDataContainer pdc) {
    }
}
