package net.enchadd;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.enchadd.config.EnchADDConfigOrchestrator;
import net.enchadd.config.EnchADDRuntimeSettingsFacade;
import net.enchadd.config.EnchADDConfigValueBridge;
import net.enchadd.config.EnchantConflictRuntime;
import net.enchadd.config.RuntimeConfigLoader;
import net.enchadd.config.RuntimeConfigState;
import net.enchadd.enchants.EnchADDEnchant;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

@SuppressWarnings("UnstableApiUsage")
public class EnchADDConfig {
    private EnchADDConfig() {}

    public static final Map<Key, EnchADDEnchant> ENCHANTS = new HashMap<>();
    private static final TagKey<Enchantment> CURSE_TAG = EnchantmentTagKeys.create(Key.key("curse"));
    private static final Map<Key, Set<Key>> INCOMPATIBLE = new HashMap<>();
    private static boolean initialized = false;
    public static boolean DEBUG = false; // Suggestion 15: Debug flag
    private static final RuntimeConfigState RUNTIME_STATE = new RuntimeConfigState();
    private static volatile RuntimeConfigLoader.RuntimeConfigSnapshot runtimeConfig = RuntimeConfigLoader.RuntimeConfigSnapshot.defaults();
    private static final EnchADDRuntimeSettingsFacade RUNTIME_FACADE = new EnchADDRuntimeSettingsFacade(
            RUNTIME_STATE,
            EnchADDConfig::runtimeConfig,
            EnchADDConfig::setRuntimeConfig,
            EnchADDConfig::setDebugFlag
    );
    private static final EnchADDConfigOrchestrator CONFIG_ORCHESTRATOR = EnchADDConfigOrchestrator.forEnchADD(
            RUNTIME_FACADE::apply,
            EnchADDConfig::loadConflictsFromConfig
    );


    protected static void init(Path filePath) throws IOException {
        if (initialized) {
            return;
        }
        initialized = true;
        CONFIG_ORCHESTRATOR.initialize(filePath);
    }

    public static String getLanguage() {
        return RUNTIME_FACADE.getLanguage();
    }

    public static boolean isMonitoringEnabled() {
        return RUNTIME_FACADE.isMonitoringEnabled();
    }

    public static int getMonitoringSampleIntervalSeconds() {
        return RUNTIME_FACADE.getMonitoringSampleIntervalSeconds();
    }

    public static double getMonitoringMinTps() {
        return RUNTIME_FACADE.getMonitoringMinTps();
    }

    public static double getMonitoringMaxErrorsPerMinute() {
        return RUNTIME_FACADE.getMonitoringMaxErrorsPerMinute();
    }

    public static long getMonitoringMaxTriggerRatePerMinute() {
        return RUNTIME_FACADE.getMonitoringMaxTriggerRatePerMinute();
    }

    public static double getMonitoringMaxParticleWindowDropRate() {
        return RUNTIME_FACADE.getMonitoringMaxParticleWindowDropRate();
    }

    public static int getMonitoringMaxStatsPending() {
        return RUNTIME_FACADE.getMonitoringMaxStatsPending();
    }

    public static boolean isSafetyModeEnabled() {
        return RUNTIME_FACADE.isSafetyModeEnabled();
    }

    public static boolean isSafetyModeAutoOnAlert() {
        return RUNTIME_FACADE.isSafetyModeAutoOnAlert();
    }

    public static int getSafetyModeAlertBurstThreshold() {
        return RUNTIME_FACADE.getSafetyModeAlertBurstThreshold();
    }

    public static int getSafetyModeAlertWindowSeconds() {
        return RUNTIME_FACADE.getSafetyModeAlertWindowSeconds();
    }

    public static int getSafetyModeAutoRecoverStableSamples() {
        return RUNTIME_FACADE.getSafetyModeAutoRecoverStableSamples();
    }

    public static double getSafetyModeChanceMultiplier() {
        return RUNTIME_FACADE.getSafetyModeChanceMultiplier();
    }

    public static int getSafetyModeTickModuloMultiplier() {
        return RUNTIME_FACADE.getSafetyModeTickModuloMultiplier();
    }

    public static boolean isSafetyModeSuppressParticles() {
        return RUNTIME_FACADE.isSafetyModeSuppressParticles();
    }

    public static long getEnchantBudgetNanosPerTick() {
        return RUNTIME_FACADE.getEnchantBudgetNanosPerTick();
    }

    public static double getEnchantBudgetDegradedChanceMultiplier() {
        return RUNTIME_FACADE.getEnchantBudgetDegradedChanceMultiplier();
    }

    public static int getEnchantBudgetDegradedTickModuloMultiplier() {
        return RUNTIME_FACADE.getEnchantBudgetDegradedTickModuloMultiplier();
    }

    public static boolean isEnchantBudgetSuppressParticles() {
        return RUNTIME_FACADE.isEnchantBudgetSuppressParticles();
    }

    public static int getEnchantBudgetBreakerConsecutiveOverruns() {
        return RUNTIME_FACADE.getEnchantBudgetBreakerConsecutiveOverruns();
    }

    public static int getEnchantBudgetBreakerCooldownTicks() {
        return RUNTIME_FACADE.getEnchantBudgetBreakerCooldownTicks();
    }

    public static boolean isEnchantBudgetSkipExecutionOnBreaker() {
        return RUNTIME_FACADE.isEnchantBudgetSkipExecutionOnBreaker();
    }

    public static synchronized boolean reloadRuntimeSettings(@NotNull Path filePath) {
        return RUNTIME_FACADE.reload(filePath);
    }

    public static boolean isCurse(Key enchantKey) {
        EnchADDEnchant enchant = ENCHANTS.get(enchantKey);
        if (enchant == null) {
            return false;
        }
        for (TagKey<Enchantment> tagKey : enchant.getEnchantTagKeys()) {
            if (CURSE_TAG.equals(tagKey)) {
                return true;
            }
        }
        return false;
    }

    public static void debug(String message, Object... args) {
        if (RUNTIME_STATE.isDebug()) {
            org.bukkit.Bukkit.getLogger().info("[EnchADD-Debug] " + String.format(message, args));
        }
    }

    private static void loadConflictsFromConfig(ConfigurationSection section) {
        org.bukkit.configuration.Configuration root = section.getRoot();
        if (!(root instanceof FileConfiguration configuration)) {
            return;
        }
        EnchantConflictRuntime.applyDefaultsAndLoad(configuration, section, INCOMPATIBLE, EnchADDConfig::debug);
    }

    public static boolean areIncompatible(Key first, Key second) {
        return EnchantConflictRuntime.areIncompatible(INCOMPATIBLE, first, second);
    }

    public static List<String> getStringList(ConfigurationSection section, String key, List<String> defaultValue) {
        return EnchADDConfigValueBridge.getStringList(section, key, defaultValue);
    }

    public static String getString(ConfigurationSection section, String key, String defaultValue) {
        return EnchADDConfigValueBridge.getString(section, key, defaultValue);
    }

    public static int getInt(ConfigurationSection section, String key, int defaultValue) {
        return EnchADDConfigValueBridge.getInt(section, key, defaultValue);
    }

    public static long getLong(ConfigurationSection section, String key, long defaultValue) {
        return EnchADDConfigValueBridge.getLong(section, key, defaultValue);
    }

    public static double getDouble(ConfigurationSection section, String key, double defaultValue) {
        return EnchADDConfigValueBridge.getDouble(section, key, defaultValue);
    }

    public static boolean getBoolean(ConfigurationSection section, String key, boolean defaultValue) {
        return EnchADDConfigValueBridge.getBoolean(section, key, defaultValue);
    }

    private static RuntimeConfigLoader.RuntimeConfigSnapshot runtimeConfig() {
        return runtimeConfig;
    }

    private static void setRuntimeConfig(@NotNull RuntimeConfigLoader.RuntimeConfigSnapshot snapshot) {
        runtimeConfig = snapshot;
    }

    private static void setDebugFlag(boolean debugEnabled) {
        DEBUG = debugEnabled;
    }

    public static void migrateEnchantTags(@NotNull ConfigurationSection section) {
        EnchADDConfigValueBridge.migrateEnchantTags(section);
    }


    public static Set<EquipmentSlotGroup> getEquipmentSlotGroups(@NotNull List<String> slots) {
        return EnchADDConfigValueBridge.getEquipmentSlotGroups(slots);
    }

    public static Set<TagEntry<ItemType>> getItemTagEntriesFromList(@NotNull List<String> tags) {
        return EnchADDConfigValueBridge.getItemTagEntriesFromList(tags);
    }

    public static Set<TagKey<Enchantment>> getEnchantmentTagKeysFromList(@NotNull List<String> tags) {
        return EnchADDConfigValueBridge.getEnchantmentTagKeysFromList(tags);
    }

    public static ConfigurationSection getConfigSection(ConfigurationSection section, String key) {
        return EnchADDConfigValueBridge.getConfigSection(section, key);
    }

    public static String normalizeLanguage(String language) {
        return EnchADDConfigValueBridge.normalizeLanguage(language);
    }

    public static double clamp(double value, double min, double max) {
        return EnchADDConfigValueBridge.clamp(value, min, max);
    }

}
