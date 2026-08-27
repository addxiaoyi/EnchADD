package net.enchadd;

import net.kyori.adventure.key.Key;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcquisitionBalancePolicyTest {

    private static final List<String> TABLE_COMMON_TAGS = List.of(
            "#non_treasure",
            "#in_enchanting_table",
            "#on_random_loot",
            "#tradeable",
            "#trades/desert_common",
            "#trades/jungle_common",
            "#trades/plains_common",
            "#trades/savanna_common",
            "#trades/snow_common",
            "#trades/swamp_common",
            "#trades/taiga_common"
    );
    private static final List<String> TREASURE_SPECIAL_TAGS = List.of(
            "#treasure",
            "#on_random_loot",
            "#tradeable",
            "#double_trade_price",
            "#trades/desert_special",
            "#trades/jungle_special",
            "#trades/plains_special",
            "#trades/savanna_special",
            "#trades/snow_special",
            "#trades/swamp_special",
            "#trades/taiga_special"
    );
    private static final List<String> TREASURE_ONLY_TAGS = List.of(
            "#treasure",
            "#on_random_loot"
    );
    private static final List<String> CURSE_TREASURE_TAGS = List.of(
            "#curse",
            "#treasure",
            "#in_enchanting_table",
            "#on_random_loot"
    );
    private static final List<String> COMMON_TIERS_ADDED_IN_V3 = List.of(
            "arborist",
            "freshcatch",
            "furrow",
            "irrigation",
            "stonewake",
            "tenderstep",
            "trailblazer"
    );
    private static final List<String> WARD_DEFAULT_CONFLICTS = List.of(
            "enchadd:barrier",
            "enchadd:brace",
            "enchadd:bulwark",
            "enchadd:holdfast",
            "enchadd:parry",
            "enchadd:pivot",
            "enchadd:riposte"
    );

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        resetEnchantConfigState();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetEnchantConfigState();
    }

    @Test
    void initAppliesTieredSourcesAndScaledWeights() throws Exception {
        EnchADDConfig.init(tempDir);

        YamlConfiguration yaml = loadConfig();

        assertEquals(4, yaml.getInt("acquisitionPolicyVersion"));

        assertEquals(1, yaml.getInt("enchants.telepathy.weight"));
        assertEquals(TABLE_COMMON_TAGS, yaml.getStringList("enchants.telepathy.enchantmentTags"));
        assertTrue(yaml.getBoolean("enchants.telepathy.canGetFromEnchantingTable"));
        for (String enchantKey : COMMON_TIERS_ADDED_IN_V3) {
            assertEquals(
                    TABLE_COMMON_TAGS,
                    yaml.getStringList("enchants." + enchantKey + ".enchantmentTags"),
                    () -> enchantKey + " should follow TABLE_COMMON tags"
            );
            assertTrue(
                    yaml.getBoolean("enchants." + enchantKey + ".canGetFromEnchantingTable"),
                    () -> enchantKey + " should be enchant-table eligible"
            );
        }

        assertEquals(1, yaml.getInt("enchants.ricochet.weight"));
        assertEquals(TREASURE_SPECIAL_TAGS, yaml.getStringList("enchants.ricochet.enchantmentTags"));
        assertFalse(yaml.getBoolean("enchants.ricochet.canGetFromEnchantingTable"));

        assertEquals(1, yaml.getInt("enchants.soulbound.weight"));
        assertEquals(TREASURE_ONLY_TAGS, yaml.getStringList("enchants.soulbound.enchantmentTags"));
        assertFalse(yaml.getBoolean("enchants.soulbound.canGetFromEnchantingTable"));

        assertEquals(1, yaml.getInt("curses.gluttony.weight"));
        assertEquals(CURSE_TREASURE_TAGS, yaml.getStringList("curses.gluttony.enchantmentTags"));
        assertTrue(yaml.getBoolean("curses.gluttony.canGetFromEnchantingTable"));

        assertFalse(yaml.contains("enchants.cloaking"));
        assertFalse(yaml.contains("enchants.sonar"));
        assertFalse(yaml.contains("enchants.arrow_refund"));
    }

    @Test
    void initDoesNotRescaleWeightsAgainAfterPolicyVersionIsStored() throws Exception {
        EnchADDConfig.init(tempDir);
        int firstWeight = loadConfig().getInt("enchants.telepathy.weight");

        resetEnchantConfigState();
        EnchADDConfig.init(tempDir);
        int secondWeight = loadConfig().getInt("enchants.telepathy.weight");

        assertEquals(firstWeight, secondWeight);
        assertEquals(1, secondWeight);
    }

    @Test
    void initUpgradesVersionOnePolicyWithoutLeavingDeletedEnchantSectionsBehind() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("acquisitionPolicyVersion", 1);
        yaml.set("enchants.telepathy.weight", 3);
        yaml.set("enchants.telepathy.enchantmentTags", TABLE_COMMON_TAGS);
        yaml.set("enchants.soulbound.weight", 2);
        yaml.set("enchants.soulbound.enchantmentTags", TREASURE_ONLY_TAGS);
        yaml.set("curses.gluttony.weight", 1);
        yaml.set("curses.gluttony.enchantmentTags", List.of("#curse", "#treasure", "#on_random_loot"));
        yaml.set("enchants.cloaking.weight", 2);
        yaml.set("enchants.sonar.weight", 3);
        yaml.set("enchants.arrow_refund.weight", 2);
        yaml.save(tempDir.resolve("config.yml").toFile());

        EnchADDConfig.init(tempDir);

        YamlConfiguration upgraded = loadConfig();
        assertEquals(4, upgraded.getInt("acquisitionPolicyVersion"));
        assertEquals(1, upgraded.getInt("enchants.telepathy.weight"));
        assertEquals(1, upgraded.getInt("enchants.soulbound.weight"));
        assertEquals(CURSE_TREASURE_TAGS, upgraded.getStringList("curses.gluttony.enchantmentTags"));
        assertTrue(upgraded.getBoolean("curses.gluttony.canGetFromEnchantingTable"));
        assertFalse(upgraded.contains("enchants.cloaking"));
        assertFalse(upgraded.contains("enchants.sonar"));
        assertFalse(upgraded.contains("enchants.arrow_refund"));
    }

    @Test
    void initUpgradesVersionTwoPolicyAndRescalesExistingWeights() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("acquisitionPolicyVersion", 2);
        yaml.set("enchants.telepathy.weight", 2);
        yaml.set("enchants.irrigation.weight", 2);
        yaml.set("enchants.freshcatch.weight", 3);
        yaml.save(tempDir.resolve("config.yml").toFile());

        EnchADDConfig.init(tempDir);

        YamlConfiguration upgraded = loadConfig();
        assertEquals(4, upgraded.getInt("acquisitionPolicyVersion"));
        assertEquals(1, upgraded.getInt("enchants.telepathy.weight"));
        assertEquals(1, upgraded.getInt("enchants.irrigation.weight"));
        assertEquals(1, upgraded.getInt("enchants.freshcatch.weight"));
        for (String enchantKey : COMMON_TIERS_ADDED_IN_V3) {
            assertEquals(TABLE_COMMON_TAGS, upgraded.getStringList("enchants." + enchantKey + ".enchantmentTags"));
        }
    }

    @Test
    void initUpgradeIsIdempotentAcrossRepeatedVersionTwoDowngrades() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("acquisitionPolicyVersion", 2);
        yaml.set("enchants.telepathy.weight", 2);
        yaml.set("enchants.irrigation.weight", 2);
        yaml.set("enchants.freshcatch.weight", 3);
        yaml.save(tempDir.resolve("config.yml").toFile());

        for (int i = 0; i < 3; i++) {
            resetEnchantConfigState();
            EnchADDConfig.init(tempDir);
            YamlConfiguration upgraded = loadConfig();

            assertEquals(4, upgraded.getInt("acquisitionPolicyVersion"));
            assertEquals(1, upgraded.getInt("enchants.telepathy.weight"));
            assertEquals(1, upgraded.getInt("enchants.irrigation.weight"));
            assertEquals(1, upgraded.getInt("enchants.freshcatch.weight"));

            upgraded.set("acquisitionPolicyVersion", 2);
            upgraded.save(tempDir.resolve("config.yml").toFile());
        }
    }

    @Test
    void initMergesSmartDefaultConflictsIntoLegacyConfigWithoutDroppingCustomPairs() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("acquisitionPolicyVersion", 2);
        yaml.set("conflictPolicyVersion", 1);
        yaml.set("conflicts.enchadd:ward", List.of("enchadd:barrier"));
        yaml.set("conflicts.enchadd:custom", List.of("minecraft:unbreaking"));
        yaml.save(tempDir.resolve("config.yml").toFile());

        EnchADDConfig.init(tempDir);

        YamlConfiguration upgraded = loadConfig();
        assertEquals(4, upgraded.getInt("acquisitionPolicyVersion"));
        assertEquals(3, upgraded.getInt("conflictPolicyVersion"));
        assertEquals(
                WARD_DEFAULT_CONFLICTS,
                upgraded.getStringList("conflicts.enchadd:ward")
        );
        assertEquals(List.of("minecraft:fire_aspect", "enchadd:immolate"), upgraded.getStringList("conflicts.enchadd:frostbrand"));
        assertEquals(List.of("minecraft:unbreaking"), upgraded.getStringList("conflicts.enchadd:custom"));
        assertEquals(List.of("minecraft:projectile_protection", "enchadd:sidestep"), upgraded.getStringList("conflicts.enchadd:evasion"));

        assertTrue(EnchADDConfig.areIncompatible(Key.key("enchadd:ward"), Key.key("enchadd:brace")));
        assertTrue(EnchADDConfig.areIncompatible(Key.key("enchadd:ward"), Key.key("enchadd:bulwark")));
        assertTrue(EnchADDConfig.areIncompatible(Key.key("enchadd:ward"), Key.key("enchadd:parry")));
        assertTrue(EnchADDConfig.areIncompatible(Key.key("enchadd:frostbrand"), Key.key("minecraft:fire_aspect")));
        assertTrue(EnchADDConfig.areIncompatible(Key.key("enchadd:evasion"), Key.key("minecraft:projectile_protection")));
        assertTrue(EnchADDConfig.areIncompatible(Key.key("enchadd:evasion"), Key.key("enchadd:sidestep")));
        assertTrue(EnchADDConfig.areIncompatible(Key.key("enchadd:custom"), Key.key("minecraft:unbreaking")));
    }

    @Test
    void initUpgradeRemainsIdempotentAcrossRepeatedAcquisitionAndConflictRollbacks() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("acquisitionPolicyVersion", 2);
        yaml.set("conflictPolicyVersion", 1);
        yaml.set("enchants.telepathy.weight", 2);
        yaml.set("conflicts.enchadd:ward", List.of("enchadd:barrier"));
        yaml.set("conflicts.enchadd:custom", List.of("minecraft:unbreaking"));
        yaml.save(tempDir.resolve("config.yml").toFile());

        for (int i = 0; i < 3; i++) {
            resetEnchantConfigState();
            EnchADDConfig.init(tempDir);
            YamlConfiguration upgraded = loadConfig();

            assertEquals(4, upgraded.getInt("acquisitionPolicyVersion"));
            assertEquals(3, upgraded.getInt("conflictPolicyVersion"));
            assertEquals(1, upgraded.getInt("enchants.telepathy.weight"));
            assertEquals(WARD_DEFAULT_CONFLICTS, upgraded.getStringList("conflicts.enchadd:ward"));
            assertEquals(List.of("minecraft:fire_aspect", "enchadd:immolate"), upgraded.getStringList("conflicts.enchadd:frostbrand"));
            assertEquals(List.of("minecraft:unbreaking"), upgraded.getStringList("conflicts.enchadd:custom"));

            upgraded.set("acquisitionPolicyVersion", 2);
            upgraded.set("conflictPolicyVersion", 1);
            upgraded.set("conflicts.enchadd:ward", List.of("enchadd:barrier"));
            upgraded.set("conflicts.enchadd:frostbrand", List.of("minecraft:fire_aspect"));
            upgraded.save(tempDir.resolve("config.yml").toFile());
        }
    }

    private YamlConfiguration loadConfig() {
        File configFile = tempDir.resolve("config.yml").toFile();
        return YamlConfiguration.loadConfiguration(configFile);
    }

    private static void resetEnchantConfigState() throws Exception {
        EnchADDConfig.ENCHANTS.clear();
        getIncompatibleMap().clear();
        setInitialized(false);
    }

    @SuppressWarnings("unchecked")
    private static Map<Key, Set<Key>> getIncompatibleMap() throws Exception {
        Field field = EnchADDConfig.class.getDeclaredField("INCOMPATIBLE");
        field.setAccessible(true);
        return (Map<Key, Set<Key>>) field.get(null);
    }

    private static void setInitialized(boolean value) throws Exception {
        Field field = EnchADDConfig.class.getDeclaredField("initialized");
        field.setAccessible(true);
        field.setBoolean(null, value);
    }
}
