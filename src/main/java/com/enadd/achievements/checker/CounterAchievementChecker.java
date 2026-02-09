package com.enadd.achievements.checker;

import com.enadd.achievements.PlayerAchievementData;
import com.enadd.core.api.IAchievementChecker;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public final class CounterAchievementChecker implements IAchievementChecker {

    private final String achievementId;
    private final String counterKey;
    private final int targetCount;
    private final CounterCondition condition;

    public CounterAchievementChecker(String achievementId, String counterKey, int targetCount, CounterCondition condition) {
        this.achievementId = achievementId;
        this.counterKey = counterKey;
        this.targetCount = targetCount;
        this.condition = condition;
    }

    @Override
    public boolean check(Player player, PlayerAchievementData data) {
        if (data.hasAchievement(achievementId)) {
            return false;
        }

        if (condition != null && !condition.isMet(player, player.getInventory().getItemInMainHand(), null)) {
            return false;
        }

        int currentCount = data.getCounter(counterKey);
        return currentCount >= targetCount;
    }

    @Override
    public void onAward(Player player, String achievementId) {
        // Achievement awarded callback
    }

    @Override
    public int getPriority() {
        return 50;
    }

    public interface CounterCondition {
        boolean isMet(Player player, ItemStack tool, Entity target);
    }

    public static CounterAchievementChecker createTreeFellerChecker() {
        return new CounterAchievementChecker(
            "lumberjack",
            "trees_cut",
            1000,
            (player, tool, target) -> {
                if (tool == null) return false;
                boolean hasTreeFeller = tool.getEnchantments().keySet().stream()
                    .anyMatch(ench -> ench.getKey().toString().equals("enadd:arbor_master"));
                return hasTreeFeller;
            }
        );
    }

    public static CounterAchievementChecker createSniperChecker() {
        return new CounterAchievementChecker(
            "marksman",
            "sniper_shots",
            100,
            (player, tool, target) -> {
                if (tool == null) return false;
                boolean hasSniper = tool.getEnchantments().keySet().stream()
                    .anyMatch(ench -> ench.getKey().toString().equals("enadd:sniper"));
                return hasSniper;
            }
        );
    }
}
