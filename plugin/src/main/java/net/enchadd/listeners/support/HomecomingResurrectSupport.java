package net.enchadd.listeners.support;

import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HomecomingResurrectSupport {

    public boolean hasHomecomingItem(@NotNull Player player,
                                     @NotNull EquipmentSlot equipmentSlot,
                                     @NotNull Enchantment homecoming) {
        EntityEquipment entityEquipment = PerformanceUtils.getEquipmentSafe(player);
        if (entityEquipment == null) {
            return false;
        }

        ItemStack item = entityEquipment.getItem(equipmentSlot);
        if (item == null) {
            return false;
        }
        return PerformanceUtils.getEnchantLevel(item, homecoming) > 0;
    }

    public @Nullable Location resolveDestination(@NotNull Player player) {
        Location location = player.getRespawnLocation();
        if (location != null) {
            return location;
        }
        if (player.getWorld() == null) {
            return null;
        }
        return player.getWorld().getSpawnLocation();
    }
}
