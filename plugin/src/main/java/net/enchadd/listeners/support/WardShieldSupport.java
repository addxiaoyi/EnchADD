package net.enchadd.listeners.support;

import net.enchadd.enchants.WardEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WardShieldSupport {

    public @Nullable ItemStack resolveWardShield(@NotNull Player player, @NotNull Enchantment ward) {
        ItemStack activeItem = player.getActiveItem();
        if (!isActiveWardShield(player.getActiveItemHand(), activeItem == null ? null : activeItem.getType())) {
            return null;
        }
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return null;
        }

        ItemStack shield = equipment.getItemInOffHand();
        if (shield == null || shield.getType() != Material.SHIELD) {
            return null;
        }
        if (PerformanceUtils.getEnchantLevel(shield, ward) <= 0) {
            return null;
        }
        return shield;
    }

    static boolean isActiveWardShield(EquipmentSlot hand, Material activeType) {
        return hand == EquipmentSlot.OFF_HAND && activeType == Material.SHIELD;
    }

    public int resolveDurabilityCost(@NotNull ItemStack shield, double finalDamage) {
        if (shield.getType() != Material.SHIELD || !(shield.getItemMeta() instanceof Damageable meta)) return 0;
        int maximum = meta.hasMaxDamage() ? meta.getMaxDamage() : shield.getType().getMaxDurability();
        return durabilityCost(finalDamage, maximum, meta.getDamage());
    }

    static int durabilityCost(double finalDamage, int maximum, int used) {
        if (!Double.isFinite(finalDamage) || finalDamage <= 0 || maximum <= 0 || used < 0 || used >= maximum) return 0;
        long remaining = (long) maximum - used;
        return (int) Math.min(remaining, Math.ceil(finalDamage));
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
                               @NotNull WardEnchant config,
                               int durabilityCost) {
        event.setCancelled(true);
        shield.damage(durabilityCost, player);
        player.getWorld().playSound(player, config.getBlockSound(), SoundCategory.MASTER, 1f, 1f);
    }
}
