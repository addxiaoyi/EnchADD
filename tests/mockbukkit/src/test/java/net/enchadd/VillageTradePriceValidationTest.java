package net.enchadd;

import net.enchadd.config.EnchantAcquisitionPolicy;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Village trade price validation tests.
 * Tests that enchantments with #double_trade_price tag are correctly configured
 * and that tradeable enchants have proper biome-specific village trade tags.
 */
class VillageTradePriceValidationTest {

    /**
     * Tags for enchants that have double trade price in villages.
     */
    private static final List<String> TREASURE_SPECIAL_TAGS_WITH_DOUBLE_PRICE = List.of(
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

    /**
     * Enchant keys that should have double trade price (treasure special tier).
     */
    private static final Set<String> DOUBLE_TRADE_PRICE_ENCHANTS = Set.of(
            "riposte", "updraft", "tremor", "meteor", "overwhelm", "ricochet", "undertow", "cinder",
            "frostbrand", "immolate", "hemorrhage", "mortal_wound", "measured", "initiative", "lodestar",
            "resonance", "poise", "last_stand", "dispel", "starwish", "waysong", "delvesense", "tideshell"
    );

    /**
     * Enchant keys that should be tradeable but NOT have double price (table special tier).
     */
    private static final Set<String> TRADEABLE_NO_DOUBLE_PRICE_ENCHANTS = Set.of(
            "purify", "clairvoyance", "sidestep", "shroud", "homeward", "bulwark", "holdfast", "parry",
            "pivot", "bind", "farshot", "flare", "hunters_mark", "obscure", "steady_aim", "stillness",
            "highground", "pursuit", "tracer", "debilitate", "quell", "rally", "breakguard", "sunder",
            "underdog", "cadence", "wingclip"
    );

    /**
     * Enchant keys that are tradeable at common tier (plains villages).
     */
    private static final Set<String> TRADEABLE_COMMON_ENCHANTS = Set.of(
            "telepathy", "insight", "smelting", "replanting", "irrigation", "arborist", "trailblazer",
            "furrow", "rebound", "refine", "stonewake", "afterglide", "skim", "trawler", "freshcatch",
            "tide_runner", "fleetfoot", "tenderstep", "evasion", "firebreak", "lucidity", "brace",
            "barrier", "beheading", "steadfast"
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
    void treasureSpecialEnchantsHaveDoubleTradePrice() throws Exception {
        EnchADDConfig.init(tempDir);

        YamlConfiguration yaml = loadConfig();

        for (String enchantKey : DOUBLE_TRADE_PRICE_ENCHANTS) {
            List<String> tags = yaml.getStringList("enchants." + enchantKey + ".enchantmentTags");
            assertTrue(
                    tags.contains("#tradeable"),
                    () -> enchantKey + " should be tradeable"
            );
            assertTrue(
                    tags.contains("#double_trade_price"),
                    () -> enchantKey + " should have double_trade_price tag"
            );
            // Verify all biome-specific trade tags are present
            for (String biome : List.of("desert", "jungle", "plains", "savanna", "snow", "swamp", "taiga")) {
                assertTrue(
                        tags.contains("#trades/" + biome + "_special"),
                        () -> enchantKey + " should have #trades/" + biome + "_special tag"
                );
            }
        }
    }

    @Test
    void tableSpecialEnchantsAreTradeableWithoutDoublePrice() throws Exception {
        EnchADDConfig.init(tempDir);

        YamlConfiguration yaml = loadConfig();

        for (String enchantKey : TRADEABLE_NO_DOUBLE_PRICE_ENCHANTS) {
            List<String> tags = yaml.getStringList("enchants." + enchantKey + ".enchantmentTags");
            assertTrue(
                    tags.contains("#tradeable"),
                    () -> enchantKey + " should be tradeable"
            );
            assertFalse(
                    tags.contains("#double_trade_price"),
                    () -> enchantKey + " should NOT have double_trade_price tag"
            );
            // Should have biome-specific trade tags for special tier
            for (String biome : List.of("desert", "jungle", "plains", "savanna", "snow", "swamp", "taiga")) {
                assertTrue(
                        tags.contains("#trades/" + biome + "_special"),
                        () -> enchantKey + " should have #trades/" + biome + "_special tag"
                );
            }
        }
    }

    @Test
    void tableCommonEnchantsAreTradeableInPlainsVillages() throws Exception {
        EnchADDConfig.init(tempDir);

        YamlConfiguration yaml = loadConfig();

        for (String enchantKey : TRADEABLE_COMMON_ENCHANTS) {
            List<String> tags = yaml.getStringList("enchants." + enchantKey + ".enchantmentTags");
            assertTrue(
                    tags.contains("#tradeable"),
                    () -> enchantKey + " should be tradeable"
            );
            assertFalse(
                    tags.contains("#double_trade_price"),
                    () -> enchantKey + " should NOT have double_trade_price tag"
            );
            // Common enchants should only be available in plains villages
            assertTrue(
                    tags.contains("#trades/plains_common"),
                    () -> enchantKey + " should have #trades/plains_common tag"
            );
        }
    }

    @Test
    void treasureOnlyEnchantsAreNotTradeable() throws Exception {
        EnchADDConfig.init(tempDir);

        YamlConfiguration yaml = loadConfig();

        Set<String> treasureOnlyEnchants = Set.of(
                "soulbound", "executioner", "decapitate", "momentum", "fortitude", "nourish", "volley",
                "shadowstrike", "airbag", "homecoming", "ward", "wingguard"
        );

        for (String enchantKey : treasureOnlyEnchants) {
            List<String> tags = yaml.getStringList("enchants." + enchantKey + ".enchantmentTags");
            assertFalse(
                    tags.contains("#tradeable"),
                    () -> enchantKey + " should NOT be tradeable (treasure only)"
            );
            assertFalse(
                    tags.contains("#double_trade_price"),
                    () -> enchantKey + " should NOT have double_trade_price tag"
            );
        }
    }

    @Test
    void curseEnchantsAreNotTradeable() throws Exception {
        EnchADDConfig.init(tempDir);

        YamlConfiguration yaml = loadConfig();

        Set<String> curseEnchants = Set.of(
                "panic", "gluttony", "vampirism", "insomnia", "greed", "rashness", "retching", "thirst",
                "fragility", "misfortune", "lethargy", "gravitation", "brittle", "backfire", "shatter"
        );

        for (String enchantKey : curseEnchants) {
            List<String> tags = yaml.getStringList("curses." + enchantKey + ".enchantmentTags");
            assertFalse(
                    tags.contains("#tradeable"),
                    () -> "curse " + enchantKey + " should NOT be tradeable"
            );
            assertFalse(
                    tags.contains("#double_trade_price"),
                    () -> "curse " + enchantKey + " should NOT have double_trade_price tag"
            );
        }
    }

    @Test
    void doubleTradePriceEnchantsHaveCorrectWeight() throws Exception {
        EnchADDConfig.init(tempDir);

        YamlConfiguration yaml = loadConfig();

        // All treasure special enchants should have weight 1 after policy v4 migration
        for (String enchantKey : DOUBLE_TRADE_PRICE_ENCHANTS) {
            int weight = yaml.getInt("enchants." + enchantKey + ".weight");
            assertEquals(
                    1,
                    weight,
                    () -> enchantKey + " should have weight 1"
            );
        }
    }

    @Test
    void doubleTradePriceTagIsCorrectlyAppliedToTreasureSpecialTier() throws Exception {
        // Verify the AcquisitionTier.TREASURE_SPECIAL includes #double_trade_price
        List<String> treasureSpecialTags = EnchantAcquisitionPolicy.AcquisitionTier.TREASURE_SPECIAL.tags();

        assertTrue(
                treasureSpecialTags.contains("#double_trade_price"),
                "TREASURE_SPECIAL tier should include #double_trade_price tag"
        );
        assertTrue(
                treasureSpecialTags.contains("#tradeable"),
                "TREASURE_SPECIAL tier should include #tradeable tag"
        );
        assertTrue(
                treasureSpecialTags.contains("#treasure"),
                "TREASURE_SPECIAL tier should include #treasure tag"
        );
    }

    @Test
    void tableSpecialTierDoesNotHaveDoubleTradePrice() throws Exception {
        List<String> tableSpecialTags = EnchantAcquisitionPolicy.AcquisitionTier.TABLE_SPECIAL.tags();

        assertTrue(
                tableSpecialTags.contains("#tradeable"),
                "TABLE_SPECIAL tier should include #tradeable tag"
        );
        assertFalse(
                tableSpecialTags.contains("#double_trade_price"),
                "TABLE_SPECIAL tier should NOT include #double_trade_price tag"
        );
    }

    @Test
    void tableCommonTierDoesNotHaveDoubleTradePrice() throws Exception {
        List<String> tableCommonTags = EnchantAcquisitionPolicy.AcquisitionTier.TABLE_COMMON.tags();

        assertTrue(
                tableCommonTags.contains("#tradeable"),
                "TABLE_COMMON tier should include #tradeable tag"
        );
        assertFalse(
                tableCommonTags.contains("#double_trade_price"),
                "TABLE_COMMON tier should NOT include #double_trade_price tag"
        );
    }

    private YamlConfiguration loadConfig() {
        java.io.File configFile = tempDir.resolve("config.yml").toFile();
        return YamlConfiguration.loadConfiguration(configFile);
    }

    private static void resetEnchantConfigState() throws Exception {
        EnchADDConfig.ENCHANTS.clear();
        getIncompatibleMap().clear();
        setInitialized(false);
    }

    @SuppressWarnings("unchecked")
    private static Map<net.kyori.adventure.key.Key, Set<net.kyori.adventure.key.Key>> getIncompatibleMap() throws Exception {
        Field field = EnchADDConfig.class.getDeclaredField("INCOMPATIBLE");
        field.setAccessible(true);
        return (Map<net.kyori.adventure.key.Key, Set<net.kyori.adventure.key.Key>>) field.get(null);
    }

    private static void setInitialized(boolean value) throws Exception {
        Field field = EnchADDConfig.class.getDeclaredField("initialized");
        field.setAccessible(true);
        field.setBoolean(null, value);
    }
}
