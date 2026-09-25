package net.enchadd.listeners.support;

import net.enchadd.enchants.LethargyEnchant;
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

public final class LethargySprintSupport {

    public void apply(@NotNull PlayerToggleSprintEvent event,
                      @NotNull Enchantment enchant,
                      @NotNull NamespacedKey key,
                      @NotNull LethargyEnchant config) {
        if (event.isCancelled() || !event.isSprinting()) {
            return;
        }

        Player player = event.getPlayer();
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return;
        }

        ItemStack boots = equipment.getBoots();
        if (boots == null) {
            return;
        }

        int level = PerformanceUtils.getEnchantLevel(boots, enchant);
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

        int seconds = SprintEffectRules.seconds(level, config.getMaxLevel(), config.getSlowSecondsPerLevel());
        int amplifier = SprintEffectRules.slowAmplifier(config.getSlowAmplifier(), level, config.getMaxLevel());
        if (ActiveBuffSupport.apply(player, PotionEffectType.SLOWNESS, seconds, amplifier)) {
            PerformanceUtils.setCooldown(pdc, key);
        }
    }
}
