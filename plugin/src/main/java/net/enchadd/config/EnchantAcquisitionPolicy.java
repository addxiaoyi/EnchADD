package net.enchadd.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class EnchantAcquisitionPolicy {

    public static final int ACQUISITION_POLICY_VERSION = 4;
    public static final String ACQUISITION_POLICY_VERSION_KEY = "acquisitionPolicyVersion";
    // Keep custom enchant weights at roughly one-third of the previous policy.
    private static final double WEIGHT_REDUCTION_FACTOR = 0.34;
    private static final double LEGACY_TABLE_COMMON_WEIGHT_MULTIPLIER_V1 = 0.5;
    private static final double LEGACY_TABLE_COMMON_WEIGHT_MULTIPLIER_V2_PLUS = 0.3;
    private static final double LEGACY_TABLE_SPECIAL_WEIGHT_MULTIPLIER = 0.4;
    private static final double LEGACY_TREASURE_SPECIAL_WEIGHT_MULTIPLIER = 0.3;
    private static final double LEGACY_TREASURE_ONLY_WEIGHT_MULTIPLIER = 0.2;
    private static final double LEGACY_CURSE_WEIGHT_MULTIPLIER = 0.25;

    public static final Set<String> TABLE_COMMON_ENCHANTS = Set.of(
            "telepathy", "insight", "smelting", "replanting", "irrigation", "arborist", "trailblazer",
            "furrow", "rebound", "refine", "stonewake", "afterglide", "skim", "trawler", "freshcatch",
            "tide_runner", "fleetfoot", "tenderstep", "evasion", "firebreak", "lucidity", "brace",
            "barrier", "beheading", "steadfast"
    );
    public static final Set<String> TABLE_SPECIAL_ENCHANTS = Set.of(
            "purify", "clairvoyance", "sidestep", "shroud", "homeward", "bulwark", "holdfast", "parry",
            "pivot", "bind", "farshot", "flare", "hunters_mark", "obscure", "steady_aim", "stillness",
            "highground", "pursuit", "tracer", "debilitate", "quell", "rally", "breakguard", "sunder",
            "underdog", "cadence", "wingclip"
    );
    public static final Set<String> TREASURE_SPECIAL_ENCHANTS = Set.of(
            "riposte", "updraft", "tremor", "meteor", "overwhelm", "ricochet", "undertow", "cinder",
            "frostbrand", "immolate", "hemorrhage", "mortal_wound", "measured", "initiative", "lodestar",
            "resonance", "poise", "last_stand", "dispel", "starwish", "waysong", "delvesense", "tideshell"
    );
    public static final Set<String> TREASURE_ONLY_ENCHANTS = Set.of(
            "soulbound", "executioner", "decapitate", "momentum", "fortitude", "nourish", "volley",
            "shadowstrike", "airbag", "homecoming", "ward", "wingguard"
    );
    public static final Set<String> CURSE_ENCHANTS = Set.of(
            "panic", "gluttony", "vampirism", "insomnia", "greed", "rashness", "retching", "thirst",
            "fragility", "misfortune", "lethargy", "gravitation", "brittle", "backfire", "shatter"
    );

    public enum AcquisitionTier {
        TABLE_COMMON(0.3 * WEIGHT_REDUCTION_FACTOR, List.of(
                "#non_treasure", "#in_enchanting_table", "#on_random_loot", "#tradeable",
                "#trades/desert_common", "#trades/jungle_common", "#trades/plains_common",
                "#trades/savanna_common", "#trades/snow_common", "#trades/swamp_common", "#trades/taiga_common"
        )),
        TABLE_SPECIAL(0.4 * WEIGHT_REDUCTION_FACTOR, List.of(
                "#non_treasure", "#in_enchanting_table", "#on_random_loot", "#tradeable",
                "#trades/desert_special", "#trades/jungle_special", "#trades/plains_special",
                "#trades/savanna_special", "#trades/snow_special", "#trades/swamp_special", "#trades/taiga_special"
        )),
        TREASURE_SPECIAL(0.3 * WEIGHT_REDUCTION_FACTOR, List.of(
                "#treasure", "#on_random_loot", "#tradeable", "#double_trade_price",
                "#trades/desert_special", "#trades/jungle_special", "#trades/plains_special",
                "#trades/savanna_special", "#trades/snow_special", "#trades/swamp_special", "#trades/taiga_special"
        )),
        TREASURE_ONLY(0.2 * WEIGHT_REDUCTION_FACTOR, List.of("#treasure", "#on_random_loot")),
        CURSE_TREASURE(0.25 * WEIGHT_REDUCTION_FACTOR, List.of("#curse", "#treasure", "#in_enchanting_table", "#on_random_loot"));

        private final double weightMultiplier;
        private final List<String> tags;

        AcquisitionTier(double weightMultiplier, List<String> tags) {
            this.weightMultiplier = weightMultiplier;
            this.tags = tags;
        }

        public double weightMultiplier() {
            return weightMultiplier;
        }

        public List<String> tags() {
            return tags;
        }

        public boolean allowsEnchantingTable() {
            return tags.contains("#in_enchanting_table");
        }
    }

    private EnchantAcquisitionPolicy() {
    }

    public static void migrateEnchantTags(@NotNull ConfigurationSection section) {
        for (String sectionKey : section.getKeys(false)) {
            ConfigurationSection enchantSection = section.getConfigurationSection(sectionKey);
            if (enchantSection == null) continue;
            if (!enchantSection.isSet("canGetFromEnchantingTable") || enchantSection.isSet("enchantmentTags")) continue;
            boolean canGetFromEnchantingTable = enchantSection.getBoolean("canGetFromEnchantingTable", true);
            enchantSection.set("enchantmentTags", canGetFromEnchantingTable ? List.of("#in_enchanting_table") : List.of());
        }
    }

    public static boolean applyAcquisitionPolicyDefaults(@NotNull FileConfiguration configuration,
                                                         @NotNull ConfigurationSection enchantsSection,
                                                         @NotNull ConfigurationSection cursesSection) {
        int previousVersion = configuration.getInt(ACQUISITION_POLICY_VERSION_KEY, 0);
        if (previousVersion >= ACQUISITION_POLICY_VERSION) {
            return false;
        }
        double commonPreviousMultiplier = previousVersion >= 2 ? LEGACY_TABLE_COMMON_WEIGHT_MULTIPLIER_V2_PLUS : LEGACY_TABLE_COMMON_WEIGHT_MULTIPLIER_V1;
        applyTierToSections(enchantsSection, TABLE_COMMON_ENCHANTS, AcquisitionTier.TABLE_COMMON, previousVersion, commonPreviousMultiplier);
        applyTierToSections(enchantsSection, TABLE_SPECIAL_ENCHANTS, AcquisitionTier.TABLE_SPECIAL, previousVersion, LEGACY_TABLE_SPECIAL_WEIGHT_MULTIPLIER);
        applyTierToSections(enchantsSection, TREASURE_SPECIAL_ENCHANTS, AcquisitionTier.TREASURE_SPECIAL, previousVersion, LEGACY_TREASURE_SPECIAL_WEIGHT_MULTIPLIER);
        applyTierToSections(enchantsSection, TREASURE_ONLY_ENCHANTS, AcquisitionTier.TREASURE_ONLY, previousVersion, LEGACY_TREASURE_ONLY_WEIGHT_MULTIPLIER);
        applyTierToSections(cursesSection, CURSE_ENCHANTS, AcquisitionTier.CURSE_TREASURE, previousVersion, LEGACY_CURSE_WEIGHT_MULTIPLIER);
        applySoulboundRarityOverride(enchantsSection);
        configuration.set(ACQUISITION_POLICY_VERSION_KEY, ACQUISITION_POLICY_VERSION);
        return true;
    }

    private static void applyTierToSections(@NotNull ConfigurationSection parentSection,
                                            @NotNull Set<String> sectionKeys,
                                            @NotNull AcquisitionTier tier,
                                            int previousVersion,
                                            double previousMultiplier) {
        for (String sectionKey : sectionKeys) {
            ConfigurationSection enchantSection = ConfigSupport.getConfigSection(parentSection, sectionKey);
            enchantSection.set("weight", scaleWeight(enchantSection.getInt("weight", 0), tier.weightMultiplier(), previousVersion, previousMultiplier));
            enchantSection.set("enchantmentTags", new ArrayList<>(tier.tags()));
            enchantSection.set("canGetFromEnchantingTable", tier.allowsEnchantingTable());
        }
    }

    private static int scaleWeight(int currentWeight, double multiplier, int previousVersion, double previousMultiplier) {
        if (currentWeight <= 0) {
            return currentWeight;
        }
        if (previousVersion >= 1) {
            if (Math.abs(previousMultiplier - multiplier) < 1.0E-9) {
                return currentWeight;
            }
            return Math.max(1, (int) Math.round(currentWeight * (multiplier / previousMultiplier)));
        }
        return Math.max(1, (int) Math.round(currentWeight * multiplier));
    }

    private static void applySoulboundRarityOverride(@NotNull ConfigurationSection enchantsSection) {
        ConfigurationSection soulboundSection = ConfigSupport.getConfigSection(enchantsSection, "soulbound");
        soulboundSection.set("weight", 1);
    }
}
