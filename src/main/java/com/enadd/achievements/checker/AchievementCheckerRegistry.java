package com.enadd.achievements.checker;

import com.enadd.achievements.PlayerAchievementData;
import com.enadd.core.api.IAchievementChecker;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public final class AchievementCheckerRegistry {

    private static final ConcurrentHashMap<String, IAchievementChecker> checkers = new ConcurrentHashMap<>();

    private AchievementCheckerRegistry() {}

    public static void register(IAchievementChecker checker) {
        // Fallback registration based on class name if ID not provided
        String id = checker.getClass().getSimpleName().toLowerCase()
            .replace("checker", "");
        register(id, checker);
    }

    public static void register(String achievementId, IAchievementChecker checker) {
        checkers.put(achievementId, checker);
    }

    public static IAchievementChecker getChecker(String achievementId) {
        return checkers.get(achievementId);
    }

    public static List<IAchievementChecker> getAllCheckers() {
        List<IAchievementChecker> list = new ArrayList<>(checkers.values());
        Collections.sort(list, Comparator.comparingInt(IAchievementChecker::getPriority).reversed());
        return list;
    }

    public static void initializeDefaultCheckers() {
        register("enchantment_master", new EnchantmentMasterChecker());
        register("cursed_warrior", new CursedWarriorChecker());
        register("lumberjack", CounterAchievementChecker.createTreeFellerChecker());
        register("marksman", CounterAchievementChecker.createSniperChecker());
    }

    public static boolean checkAchievement(Player player, PlayerAchievementData data, String achievementId) {
        IAchievementChecker checker = getChecker(achievementId);
        if (checker != null) {
            return checker.check(player, data);
        }
        return false;
    }

    public static void clear() {
        checkers.clear();
    }
}
