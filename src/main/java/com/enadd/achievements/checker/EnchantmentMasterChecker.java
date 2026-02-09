package com.enadd.achievements.checker;

import com.enadd.achievements.PlayerAchievementData;
import com.enadd.core.api.IAchievementChecker;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashSet;
import java.util.Set;


public final class EnchantmentMasterChecker implements IAchievementChecker {

    private static final int REQUIRED_ENCHANTMENTS = 229;

    @Override
    public boolean check(Player player, PlayerAchievementData data) {
        if (data.hasAchievement("enchantment_master")) {
            return false;
        }

        Set<String> uniqueEnchantments = new HashSet<>();

        ItemStack[] equipment = {
            player.getInventory().getHelmet(),
            player.getInventory().getChestplate(),
            player.getInventory().getLeggings(),
            player.getInventory().getBoots(),
            player.getInventory().getItemInMainHand(),
            player.getInventory().getItemInOffHand()
        };

        for (ItemStack item : equipment) {
            if (item != null) {
                for (Enchantment ench : item.getEnchantments().keySet()) {
                    String key = ench.getKey().toString();
                    if (key.startsWith("enadd:")) {
                        uniqueEnchantments.add(key);
                    }
                }
            }
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                for (Enchantment ench : item.getEnchantments().keySet()) {
                    String key = ench.getKey().toString();
                    if (key.startsWith("enadd:")) {
                        uniqueEnchantments.add(key);
                    }
                }
            }
        }

        return uniqueEnchantments.size() >= REQUIRED_ENCHANTMENTS;
    }

    @Override
    public int getPriority() {
        return 100;
    }
}
