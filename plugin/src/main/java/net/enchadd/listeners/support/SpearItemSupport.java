package net.enchadd.listeners.support;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class SpearItemSupport {

    private SpearItemSupport() {
    }

    public static boolean isSpear(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        String name = item.getType().name();
        return name.endsWith("_SPEAR") || "TRIDENT".equals(name);
    }
}