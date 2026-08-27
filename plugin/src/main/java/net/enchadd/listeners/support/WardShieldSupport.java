package net.enchadd.listeners.support;

import net.enchadd.enchants.WardEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WardShieldSupport {

    public @Nullable ItemStack resolveWardShield(@NotNull Player player, @NotNull Enchantment ward) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return null;
        }

        ItemStack shield = equipment.getItemInOffHand();
        if (shield == null || shield.getType().isAir()) {
            return null;
        }
        if (PerformanceUtils.getEnchantLevel(shield, ward) <= 0) {
            return null;
        }
        return shield;
    }

    public boolean isOnCooldown(@NotNull LivingEntity entity,
                                @NotNull ItemStack shield,
                                @NotNull NamespacedKey wardKey,
                                @NotNull WardEnchant config) {
        int cooldownTicks = config.getCooldownTicks();
        if (cooldownTicks <= 0) {
            return false;
        }
        if (entity instanceof HumanEntity humanEntity) {
            return humanEntity.getCooldown(shield.getType()) > 0;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(entity);
        return pdc != null && PerformanceUtils.isOnCooldown(pdc, wardKey, cooldownTicks);
    }

    public void triggerCooldown(@NotNull LivingEntity entity,
                                @NotNull ItemStack shield,
                                @NotNull NamespacedKey wardKey,
                                @NotNull WardEnchant config) {
        int cooldownTicks = config.getCooldownTicks();
        if (cooldownTicks <= 0) {
            return;
        }
        if (entity instanceof HumanEntity humanEntity) {
            humanEntity.setCooldown(shield.getType(), cooldownTicks);
            return;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(entity);
        if (pdc != null) {
            PerformanceUtils.setCooldown(pdc, wardKey);
        }
    }

    public void applyWardBlock(@NotNull Player player,
                               @NotNull ItemStack shield,
                               @NotNull EntityDamageByEntityEvent event,
                               @NotNull WardEnchant config) {
        shield.damage((int) Math.ceil(event.getFinalDamage()), player);
        player.getWorld().playSound(player, config.getBlockSound(), SoundCategory.MASTER, 1f, 1f);
        event.setCancelled(true);
    }
}
