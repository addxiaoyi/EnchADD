package net.enchadd.listeners.support;

import net.enchadd.enchants.TenderstepEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;

public final class TenderstepProtectionSupport {

    public boolean isProtectedBlock(@NotNull Material type, @NotNull TenderstepEnchant config) {
        return type == Material.FARMLAND || (config.isProtectTurtleEggs() && type == Material.TURTLE_EGG);
    }

    public boolean hasTenderstepBoots(@NotNull Player player, @NotNull Enchantment enchant) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return false;
        }
        return PerformanceUtils.getEnchantLevel(equipment.getBoots(), enchant) > 0;
    }
}
