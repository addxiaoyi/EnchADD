package net.enchadd.utils;

import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.tag.DamageTypeTags;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class PerformanceUtils {

    private PerformanceUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ThreadLocalRandom getRandom() {
        return PerformanceRandomSupport.getRandom();
    }

    public static boolean rollChance(double chance) {
        return PerformanceRandomSupport.rollChance(chance);
    }

    public static boolean rollChanceWithLevel(double baseChance, int level, double maxChance) {
        return PerformanceRandomSupport.rollChanceWithLevel(baseChance, level, maxChance);
    }

    public static int getEnchantLevel(@Nullable ItemStack item, @NotNull Enchantment enchantment) {
        return PerformanceEnchantSupport.getEnchantLevel(item, enchantment);
    }

    public static int getHighestEnchantLevel(@Nullable EntityEquipment equipment, @NotNull Enchantment enchantment) {
        return PerformanceEnchantSupport.getHighestEnchantLevel(equipment, enchantment);
    }

    public static int getSumOfEnchantLevels(@Nullable EntityEquipment equipment, @NotNull Enchantment enchantment) {
        return PerformanceEnchantSupport.getSumOfEnchantLevels(equipment, enchantment);
    }

    public static boolean isOnCooldown(@NotNull PersistentDataContainer pdc,
                                       @NotNull NamespacedKey key,
                                       long cooldownTicks) {
        return PerformanceCooldownSupport.isOnCooldown(pdc, key, cooldownTicks);
    }

    public static void setCooldown(@NotNull PersistentDataContainer pdc,
                                   @NotNull NamespacedKey key) {
        PerformanceCooldownSupport.setCooldown(pdc, key);
    }

    public static long getRemainingCooldown(@NotNull PersistentDataContainer pdc,
                                             @NotNull NamespacedKey key,
                                             long cooldownTicks) {
        return PerformanceCooldownSupport.getRemainingCooldown(pdc, key, cooldownTicks);
    }

    @Nullable
    public static EntityEquipment getEquipmentSafe(@Nullable LivingEntity entity) {
        return PerformanceValidationSupport.getEquipmentSafe(entity);
    }

    @Nullable
    public static PersistentDataContainer getPDCSafe(@Nullable org.bukkit.entity.Entity entity) {
        return PerformanceValidationSupport.getPDCSafe(entity);
    }

    public static NamespacedKey namespacedKey(Key key) {
        return PerformanceKeySupport.namespacedKey(key);
    }

    public static NamespacedKey namespacedKeyWithSuffix(Key key, String suffix) {
        return PerformanceKeySupport.namespacedKeyWithSuffix(key, suffix);
    }

    public static NamespacedKey enchaddKey(String value) {
        return PerformanceKeySupport.enchaddKey(value);
    }

    public static void setWindowUntilTicks(@NotNull PersistentDataContainer pdc,
                                           @NotNull NamespacedKey key,
                                           int ticks) {
        PerformanceCooldownSupport.setWindowUntilTicks(pdc, key, ticks);
    }

    public static boolean extendWindowUntilTicks(@NotNull PersistentDataContainer pdc,
                                                 @NotNull NamespacedKey key, int ticks) {
        return PerformanceCooldownSupport.extendWindowUntilTicks(pdc, key, ticks);
    }

    public static void setWindowUntilSeconds(@NotNull PersistentDataContainer pdc,
                                             @NotNull NamespacedKey key,
                                             int seconds) {
        PerformanceCooldownSupport.setWindowUntilSeconds(pdc, key, seconds);
    }

    public static boolean isWindowActive(@NotNull PersistentDataContainer pdc,
                                         @NotNull NamespacedKey key) {
        return PerformanceCooldownSupport.isWindowActive(pdc, key);
    }

    public static boolean isPlayerValid(@Nullable Player player) {
        return PerformanceValidationSupport.isPlayerValid(player);
    }

    public static boolean isEntityValid(@Nullable LivingEntity entity) {
        return PerformanceValidationSupport.isEntityValid(entity);
    }

    public static boolean isSuccessfulShieldBlock(@Nullable Player player,
                                                   @Nullable EntityDamageByEntityEvent event) {
        return PerformanceShieldSupport.isSuccessfulShieldBlock(player, event);
    }

    public static int getActiveOffhandShieldLevel(@Nullable Player player, @NotNull Enchantment enchant) {
        return PerformanceShieldSupport.getActiveOffhandShieldLevel(player, enchant);
    }

    public static boolean isLikelyShieldFacingBlock(@Nullable Player player,
                                                    @Nullable Vector knockback) {
        return PerformanceShieldSupport.isLikelyShieldFacingBlock(player, knockback);
    }

    public static boolean shouldTick(@Nullable Entity entity, int modulo) {
        return PerformanceValidationSupport.shouldTick(entity, modulo);
    }

    public static double safeDivide(double numerator, double denominator, double defaultValue) {
        return PerformanceMathSupport.safeDivide(numerator, denominator, defaultValue);
    }

    public static double clamp(double value, double min, double max) {
        return PerformanceMathSupport.clamp(value, min, max);
    }

    public static int clamp(int value, int min, int max) {
        return PerformanceMathSupport.clamp(value, min, max);
    }

    public static int calculateDurationTicks(int seconds, int level) {
        return PerformanceMathSupport.calculateDurationTicks(seconds, level);
    }

    public static int calculateDurationTicksPerLevel(int secondsPerLevel, int level) {
        return PerformanceMathSupport.calculateDurationTicksPerLevel(secondsPerLevel, level);
    }

    public static <T> ArrayList<T> newArrayListWithCapacity(int capacity) {
        return PerformanceCollectionSupport.newArrayListWithCapacity(capacity);
    }

    public static <K, V> HashMap<K, V> newHashMapWithCapacity(int capacity) {
        return PerformanceCollectionSupport.newHashMapWithCapacity(capacity);
    }
}
