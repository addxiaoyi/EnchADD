package com.enadd.core.api;

import org.bukkit.entity.Player;
import com.enadd.achievements.PlayerAchievementData;


/**
 * Strategy interface for checking achievement conditions.
 * Implements the Strategy pattern for flexible achievement validation.
 */
public interface IAchievementChecker {

    /**
     * Check if the player has met the achievement conditions.
     *
     * @param player the player to check
     * @param data the player's achievement data
     * @return true if conditions are met
     */
    boolean check(Player player, PlayerAchievementData data);

    /**
     * Called when an achievement is awarded.
     * Allows for custom reward and notification logic.
     *
     * @param player the player receiving the achievement
     * @param achievementId the ID of the achievement
     */
    default void onAward(Player player, String achievementId) {
        // Default implementation does nothing
    }

    /**
     * Get the priority of this checker.
     * Higher priority checkers are evaluated first.
     *
     * @return the checker priority
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Check if this checker should run asynchronously.
     *
     * @return true if async execution is safe
     */
    default boolean isAsyncSafe() {
        return true;
    }
}
