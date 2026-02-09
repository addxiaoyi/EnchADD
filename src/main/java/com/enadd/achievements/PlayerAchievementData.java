package com.enadd.achievements;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Thread-safe player achievement data storage
 * Optimized for minimal memory usage and zero TPS impact
 */
public final class PlayerAchievementData {

    // Achieved achievements (thread-safe set)
    private final Set<String> achievements = ConcurrentHashMap.newKeySet();

    // Unique enchantments seen (for Enchantment Scholar)
    private final Set<String> seenEnchantments = ConcurrentHashMap.newKeySet();

    // Elemental effects used (for Elemental Master)
    private final Set<String> usedElements = ConcurrentHashMap.newKeySet();

    // Bosses killed (for God Slayer)
    private final Set<String> bossesKilled = ConcurrentHashMap.newKeySet();

    // Progress counters (thread-safe)
    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    private volatile long curseStartTime = 0;

    public PlayerAchievementData() {
        // Initialize common counters
        counters.put("trees_cut", new AtomicInteger(0));
        counters.put("sniper_shots", new AtomicInteger(0));
        counters.put("boss_kills", new AtomicInteger(0));
    }

    /**
     * Award an achievement to the player
     */
    public void awardAchievement(String achievementId) {
        if (achievementId != null && !achievementId.trim().isEmpty()) {
            achievements.add(achievementId.toLowerCase());
        }
    }

    /**
     * Check if player has a specific achievement
     */
    public boolean hasAchievement(String achievementId) {
        return achievementId != null && achievements.contains(achievementId.toLowerCase());
    }

    /**
     * Increment a counter and return new value
     */
    public int incrementCounter(String counterName) {
        if (counterName == null || counterName.trim().isEmpty()) {
            return 0;
        }

        return counters.computeIfAbsent(counterName, k -> new AtomicInteger(0))
                      .incrementAndGet();
    }

    /**
     * Get current counter value
     */
    public int getCounter(String counterName) {
        if (counterName == null || counterName.trim().isEmpty()) {
            return 0;
        }

        AtomicInteger counter = counters.get(counterName);
        return counter != null ? counter.get() : 0;
    }

    /**
     * Set counter value
     */
    public void setCounter(String counterName, int value) {
        if (counterName != null && !counterName.trim().isEmpty() && value >= 0) {
            counters.computeIfAbsent(counterName, k -> new AtomicInteger(0))
                   .set(value);
        }
    }

    /**
     * Track a seen enchantment
     */
    public void trackSeenEnchantment(String enchantmentId) {
        if (enchantmentId != null) {
            seenEnchantments.add(enchantmentId.toLowerCase());
        }
    }

    public int getSeenEnchantmentsCount() {
        return seenEnchantments.size();
    }

    /**
     * Track used element
     */
    public void trackUsedElement(String element) {
        if (element != null) {
            usedElements.add(element.toLowerCase());
        }
    }

    public int getUsedElementsCount() {
        return usedElements.size();
    }

    public boolean hasUsedElement(String element) {
        return usedElements.contains(element.toLowerCase());
    }

    /**
     * Track boss kill
     */
    public void trackBossKill(String bossType) {
        if (bossType != null) {
            bossesKilled.add(bossType.toLowerCase());
        }
    }

    public boolean hasKilledBoss(String bossType) {
        return bossesKilled.contains(bossType.toLowerCase());
    }

    public int getBossesKilledCount() {
        return bossesKilled.size();
    }

    /**
     * Curse time tracking
     */
    public long getCurseStartTime() {
        return curseStartTime;
    }

    public void setCurseStartTime(long time) {
        this.curseStartTime = time;
    }

    /**
     * Get all achievements (defensive copy)
     */
    public Set<String> getAchievements() {
        return Set.copyOf(achievements);
    }

    /**
     * Get achievement count
     */
    public int getAchievementCount() {
        return achievements.size();
    }

    /**
     * Reset all data (for testing purposes)
     */
    public void reset() {
        achievements.clear();
        counters.clear();
        // Reinitialize common counters
        counters.put("trees_cut", new AtomicInteger(0));
        counters.put("sniper_shots", new AtomicInteger(0));
        counters.put("boss_kills", new AtomicInteger(0));
    }

    @Override
    public String toString() {
        return "PlayerAchievementData{" +
                "achievements=" + achievements.size() +
                ", counters=" + counters.size() +
                '}';
    }
}
