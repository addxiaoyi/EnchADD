package net.enchadd.config;

import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EnchantConflictPolicy {

    public static final int CONFLICT_POLICY_VERSION = 3;
    public static final String CONFLICT_POLICY_VERSION_KEY = "conflictPolicyVersion";
    private static final Map<String, List<String>> DEFAULT_CONFLICTS = createDefaultConflicts();

    private EnchantConflictPolicy() {
    }

    @FunctionalInterface
    public interface DebugLogger {
        void log(String message, Object... args);
    }

    public static boolean applyDefaults(@NotNull FileConfiguration configuration,
                                        @NotNull ConfigurationSection conflictsSection) {
        int previousVersion = configuration.getInt(CONFLICT_POLICY_VERSION_KEY, 0);
        if (previousVersion >= CONFLICT_POLICY_VERSION) {
            return false;
        }
        mergeDefaultConflicts(conflictsSection);
        configuration.set(CONFLICT_POLICY_VERSION_KEY, CONFLICT_POLICY_VERSION);
        return true;
    }

    public static void loadFromConfig(@NotNull ConfigurationSection section,
                                      @NotNull Map<Key, Set<Key>> incompatible,
                                      @NotNull DebugLogger debugLogger) {
        incompatible.clear();
        if (section.getKeys(false).isEmpty()) {
            populateDefaultConflicts(section);
        }
        for (String keyStr : new ArrayList<>(section.getKeys(false))) {
            Key first = parseConflictKey(keyStr, "conflicts", debugLogger);
            if (first == null) {
                section.set(keyStr, null);
                continue;
            }

            List<String> incompatibleWith = section.getStringList(keyStr);
            Map<String, Key> sanitizedTargets = new LinkedHashMap<>();
            for (String secondStr : incompatibleWith) {
                Key second = parseConflictKey(secondStr, "conflicts." + keyStr, debugLogger);
                if (second == null) {
                    continue;
                }
                if (first.equals(second)) {
                    debugLogger.log("Skipping self-conflict entry %s -> %s", first.asString(), second.asString());
                    continue;
                }
                sanitizedTargets.putIfAbsent(second.asString(), second);
            }

            List<String> sanitizedTargetKeys = new ArrayList<>(sanitizedTargets.keySet());
            if (!incompatibleWith.equals(sanitizedTargetKeys)) {
                section.set(keyStr, sanitizedTargetKeys);
            }

            // 直接遍历 entrySet，避免先获取 values() 再遍历
            for (Map.Entry<String, Key> entry : sanitizedTargets.entrySet()) {
                registerConflict(incompatible, first, entry.getValue());
            }
        }
    }

    private static void populateDefaultConflicts(ConfigurationSection section) {
        mergeDefaultConflicts(section);
    }

    private static void mergeDefaultConflicts(ConfigurationSection section) {
        for (Map.Entry<String, List<String>> entry : DEFAULT_CONFLICTS.entrySet()) {
            List<String> configured = section.getStringList(entry.getKey());
            LinkedHashSet<String> merged = new LinkedHashSet<>(configured);
            merged.addAll(entry.getValue());
            List<String> mergedList = new ArrayList<>(merged);
            if (!configured.equals(mergedList)) {
                section.set(entry.getKey(), mergedList);
            }
        }
    }

    private static Key parseConflictKey(String rawKey, String path, DebugLogger debugLogger) {
        if (rawKey == null) {
            debugLogger.log("Skipping null conflict key at %s", path);
            return null;
        }
        String trimmed = rawKey.trim();
        if (trimmed.isEmpty()) {
            debugLogger.log("Skipping blank conflict key at %s", path);
            return null;
        }
        try {
            return Key.key(trimmed);
        } catch (RuntimeException ex) {
            debugLogger.log("Skipping invalid conflict key '%s' at %s", rawKey, path);
            return null;
        }
    }

    private static void registerConflict(Map<Key, Set<Key>> incompatible, Key first, Key second) {
        incompatible.computeIfAbsent(first, key -> new HashSet<>()).add(second);
        incompatible.computeIfAbsent(second, key -> new HashSet<>()).add(first);
    }

    private static Map<String, List<String>> createDefaultConflicts() {
        Map<String, List<String>> defaults = new LinkedHashMap<>();
        defaults.put("enchadd:hemorrhage", List.of("minecraft:sharpness", "minecraft:smite", "minecraft:bane_of_arthropods"));
        defaults.put("enchadd:frostbrand", List.of("minecraft:fire_aspect", "enchadd:immolate"));
        defaults.put("enchadd:immolate", List.of("minecraft:fire_aspect"));
        defaults.put("enchadd:executioner", List.of("enchadd:decapitate"));
        defaults.put("enchadd:initiative", List.of("enchadd:shadowstrike"));
        defaults.put("enchadd:beheading", List.of("minecraft:looting"));
        defaults.put("enchadd:undertow", List.of("minecraft:riptide"));
        defaults.put("enchadd:volley", List.of("enchadd:steady_aim", "enchadd:stillness", "enchadd:farshot", "enchadd:bind"));
        defaults.put("enchadd:farshot", List.of("minecraft:multishot"));
        defaults.put("enchadd:steady_aim", List.of("enchadd:stillness"));
        defaults.put("enchadd:bind", List.of("minecraft:punch"));
        defaults.put("enchadd:mortal_wound", List.of("minecraft:power"));
        defaults.put("enchadd:ward", List.of("enchadd:barrier", "enchadd:brace", "enchadd:bulwark", "enchadd:holdfast", "enchadd:parry", "enchadd:pivot", "enchadd:riposte"));
        defaults.put("enchadd:parry", List.of("enchadd:barrier", "enchadd:bulwark", "enchadd:pivot", "enchadd:riposte"));
        defaults.put("enchadd:barrier", List.of("enchadd:bulwark", "enchadd:riposte"));
        defaults.put("enchadd:bulwark", List.of("enchadd:pivot", "enchadd:riposte"));
        defaults.put("enchadd:pivot", List.of("enchadd:holdfast"));
        defaults.put("enchadd:last_stand", List.of("enchadd:fortitude"));
        defaults.put("enchadd:airbag", List.of("enchadd:afterglide", "enchadd:wingguard"));
        defaults.put("enchadd:fleetfoot", List.of("enchadd:steadfast"));
        defaults.put("enchadd:tide_runner", List.of("minecraft:depth_strider"));
        defaults.put("enchadd:evasion", List.of("minecraft:projectile_protection", "enchadd:sidestep"));
        defaults.put("enchadd:sidestep", List.of("enchadd:homeward"));
        return defaults;
    }
}
