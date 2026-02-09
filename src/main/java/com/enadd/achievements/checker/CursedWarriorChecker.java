package com.enadd.achievements.checker;

import com.enadd.achievements.PlayerAchievementData;
import com.enadd.core.api.IAchievementChecker;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public final class CursedWarriorChecker implements IAchievementChecker {

    private static final int REQUIRED_CURSES = 5;

    @Override
    public boolean check(Player player, PlayerAchievementData data) {
        if (data.hasAchievement("cursed_warrior")) {
            return false;
        }

        ItemStack[] equipment = {
            player.getInventory().getHelmet(),
            player.getInventory().getChestplate(),
            player.getInventory().getLeggings(),
            player.getInventory().getBoots()
        };

        int cursedCount = 0;
        for (ItemStack item : equipment) {
            if (item != null) {
                for (Enchantment ench : item.getEnchantments().keySet()) {
                    if (ench.getKey().getNamespace().equals("enadd") &&
                        ench.getKey().getKey().startsWith("curse_")) {
                        cursedCount++;
                        if (cursedCount >= REQUIRED_CURSES) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public int getPriority() {
        return 90;
    }
}
