package net.enchadd;

import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigConflictAndDefaultsTest {

    private Map<Key, Set<Key>> incompatibleMap;
    private Method loadConflictsMethod;

    @BeforeEach
    void setUp() throws Exception {
        incompatibleMap = getIncompatibleMap();
        incompatibleMap.clear();

        loadConflictsMethod = EnchADDConfig.class.getDeclaredMethod("loadConflictsFromConfig", ConfigurationSection.class);
        loadConflictsMethod.setAccessible(true);
    }

    @AfterEach
    void tearDown() {
        incompatibleMap.clear();
    }

    @Test
    void customConflictSectionLoadsBidirectionally() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection conflicts = yaml.createSection("conflicts");
        conflicts.set("enchadd:test_a", List.of("minecraft:sharpness", "minecraft:smite"));

        loadConflictsMethod.invoke(null, conflicts);

        Key a = Key.key("enchadd:test_a");
        Key sharpness = Key.key("minecraft:sharpness");
        Key smite = Key.key("minecraft:smite");
        Key unbreaking = Key.key("minecraft:unbreaking");

        assertTrue(EnchADDConfig.areIncompatible(a, sharpness), "custom conflict should be registered");
        assertTrue(EnchADDConfig.areIncompatible(sharpness, a), "conflict should be symmetric");
        assertTrue(EnchADDConfig.areIncompatible(a, smite), "all configured conflicts should be registered");
        assertFalse(EnchADDConfig.areIncompatible(a, a), "same key should never conflict with itself");
        assertFalse(EnchADDConfig.areIncompatible(a, unbreaking), "unconfigured pair should remain compatible");
    }

    @Test
    void emptyConflictSectionFallsBackToExpandedDefaultConflictPolicy() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection conflicts = yaml.createSection("conflicts");

        loadConflictsMethod.invoke(null, conflicts);

        List<String[]> expectedPairs = List.of(
                pair("enchadd:hemorrhage", "minecraft:sharpness"),
                pair("enchadd:hemorrhage", "minecraft:smite"),
                pair("enchadd:hemorrhage", "minecraft:bane_of_arthropods"),
                pair("enchadd:frostbrand", "minecraft:fire_aspect"),
                pair("enchadd:immolate", "minecraft:fire_aspect"),
                pair("enchadd:frostbrand", "enchadd:immolate"),
                pair("enchadd:executioner", "enchadd:decapitate"),
                pair("enchadd:initiative", "enchadd:shadowstrike"),
                pair("enchadd:beheading", "minecraft:looting"),
                pair("enchadd:undertow", "minecraft:riptide"),
                pair("enchadd:volley", "enchadd:steady_aim"),
                pair("enchadd:volley", "enchadd:stillness"),
                pair("enchadd:volley", "enchadd:farshot"),
                pair("enchadd:volley", "enchadd:bind"),
                pair("enchadd:farshot", "minecraft:multishot"),
                pair("enchadd:steady_aim", "enchadd:stillness"),
                pair("enchadd:bind", "minecraft:punch"),
                pair("enchadd:mortal_wound", "minecraft:power"),
                pair("enchadd:ward", "enchadd:barrier"),
                pair("enchadd:ward", "enchadd:brace"),
                pair("enchadd:ward", "enchadd:bulwark"),
                pair("enchadd:ward", "enchadd:holdfast"),
                pair("enchadd:ward", "enchadd:parry"),
                pair("enchadd:ward", "enchadd:pivot"),
                pair("enchadd:ward", "enchadd:riposte"),
                pair("enchadd:parry", "enchadd:barrier"),
                pair("enchadd:parry", "enchadd:bulwark"),
                pair("enchadd:parry", "enchadd:pivot"),
                pair("enchadd:parry", "enchadd:riposte"),
                pair("enchadd:barrier", "enchadd:bulwark"),
                pair("enchadd:barrier", "enchadd:riposte"),
                pair("enchadd:bulwark", "enchadd:pivot"),
                pair("enchadd:bulwark", "enchadd:riposte"),
                pair("enchadd:pivot", "enchadd:holdfast"),
                pair("enchadd:last_stand", "enchadd:fortitude"),
                pair("enchadd:airbag", "enchadd:afterglide"),
                pair("enchadd:airbag", "enchadd:wingguard"),
                pair("enchadd:fleetfoot", "enchadd:steadfast"),
                pair("enchadd:tide_runner", "minecraft:depth_strider"),
                pair("enchadd:evasion", "minecraft:projectile_protection"),
                pair("enchadd:evasion", "enchadd:sidestep"),
                pair("enchadd:sidestep", "enchadd:homeward")
        );
        assertEquals(42, expectedPairs.size(), "default conflict policy should contain 42 curated pairs");
        for (String[] expectedPair : expectedPairs) {
            assertConflictPair(expectedPair[0], expectedPair[1]);
        }
        assertFalse(EnchADDConfig.areIncompatible(Key.key("enchadd:ward"), Key.key("enchadd:steady_aim")));
        assertFalse(EnchADDConfig.areIncompatible(Key.key("enchadd:airbag"), Key.key("enchadd:fleetfoot")));
    }

    @Test
    void invalidConflictKeysSelfReferencesAndDuplicatesAreSanitized() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection conflicts = yaml.createSection("conflicts");
        conflicts.set("enchadd:test_a", List.of("enchadd:test_a", "minecraft:sharpness", " minecraft:sharpness ", "bad key"));
        conflicts.set("bad key", List.of("minecraft:smite"));
        conflicts.set("enchadd:test_b", List.of("minecraft:smite", "minecraft:smite"));

        loadConflictsMethod.invoke(null, conflicts);

        assertEquals(List.of("minecraft:sharpness"), conflicts.getStringList("enchadd:test_a"));
        assertEquals(List.of("minecraft:smite"), conflicts.getStringList("enchadd:test_b"));
        assertFalse(conflicts.contains("bad key"));
        assertConflictPair("enchadd:test_a", "minecraft:sharpness");
        assertConflictPair("enchadd:test_b", "minecraft:smite");
        assertFalse(EnchADDConfig.areIncompatible(Key.key("enchadd:test_a"), Key.key("enchadd:test_a")));
        assertFalse(EnchADDConfig.areIncompatible(Key.key("enchadd:test_a"), Key.key("minecraft:smite")));
    }

    @Test
    void configAccessorsPersistDefaultsAndRespectExistingValues() {
        YamlConfiguration yaml = new YamlConfiguration();

        assertEquals("fallback-name", EnchADDConfig.getString(yaml, "name", "fallback-name"));
        assertEquals("fallback-name", yaml.getString("name"));

        assertEquals(9, EnchADDConfig.getInt(yaml, "weight", 9));
        assertEquals(9, yaml.getInt("weight"));

        assertEquals(0.35, EnchADDConfig.getDouble(yaml, "chance", 0.35), 1e-9);
        assertEquals(0.35, yaml.getDouble("chance"), 1e-9);

        assertEquals(1_234_567_890_123L, EnchADDConfig.getLong(yaml, "long-value", 1_234_567_890_123L));
        assertEquals(1_234_567_890_123L, yaml.getLong("long-value"));

        assertTrue(EnchADDConfig.getBoolean(yaml, "enabled", true));
        assertTrue(yaml.getBoolean("enabled"));

        List<String> defaults = List.of("A", "B");
        assertEquals(defaults, EnchADDConfig.getStringList(yaml, "tags", defaults));
        assertEquals(defaults, yaml.getStringList("tags"));

        yaml.set("name", "existing-name");
        yaml.set("weight", 3);
        yaml.set("chance", 0.8);
        yaml.set("long-value", 99L);
        yaml.set("enabled", false);
        yaml.set("tags", List.of("X"));

        assertEquals("existing-name", EnchADDConfig.getString(yaml, "name", "fallback-name"));
        assertEquals(3, EnchADDConfig.getInt(yaml, "weight", 9));
        assertEquals(0.8, EnchADDConfig.getDouble(yaml, "chance", 0.35), 1e-9);
        assertEquals(99L, EnchADDConfig.getLong(yaml, "long-value", 1_234_567_890_123L));
        assertFalse(EnchADDConfig.getBoolean(yaml, "enabled", true));
        assertEquals(List.of("X"), EnchADDConfig.getStringList(yaml, "tags", defaults));
    }

    @Test
    void migrateEnchantTagsUsesEachEnchantSectionFlag() {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection enchants = yaml.createSection("enchants");

        ConfigurationSection disabledFromTable = enchants.createSection("disabled");
        disabledFromTable.set("canGetFromEnchantingTable", false);

        ConfigurationSection enabledFromTable = enchants.createSection("enabled");
        enabledFromTable.set("canGetFromEnchantingTable", true);

        ConfigurationSection alreadyMigrated = enchants.createSection("alreadyMigrated");
        alreadyMigrated.set("canGetFromEnchantingTable", false);
        alreadyMigrated.set("enchantmentTags", List.of("#custom_tag"));

        EnchADDConfig.migrateEnchantTags(enchants);

        assertEquals(List.of(), disabledFromTable.getStringList("enchantmentTags"));
        assertEquals(List.of("#in_enchanting_table"), enabledFromTable.getStringList("enchantmentTags"));
        assertEquals(List.of("#custom_tag"), alreadyMigrated.getStringList("enchantmentTags"));
    }

    @Test
    void migrateEnchantTagsSkipsNonLegacyEntriesWithoutStopping() {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection enchants = yaml.createSection("enchants");

        enchants.createSection("withoutLegacyFlag");

        ConfigurationSection legacy = enchants.createSection("legacy");
        legacy.set("canGetFromEnchantingTable", true);

        EnchADDConfig.migrateEnchantTags(enchants);

        assertEquals(List.of("#in_enchanting_table"), legacy.getStringList("enchantmentTags"));
    }

    @SuppressWarnings("unchecked")
    private static Map<Key, Set<Key>> getIncompatibleMap() throws Exception {
        Field field = EnchADDConfig.class.getDeclaredField("INCOMPATIBLE");
        field.setAccessible(true);
        return (Map<Key, Set<Key>>) field.get(null);
    }

    private static void assertConflictPair(String first, String second) {
        Key a = Key.key(first);
        Key b = Key.key(second);
        assertTrue(EnchADDConfig.areIncompatible(a, b), () -> first + " should conflict with " + second);
        assertTrue(EnchADDConfig.areIncompatible(b, a), () -> second + " should conflict with " + first);
    }

    private static String[] pair(String first, String second) {
        return new String[] {first, second};
    }
}
