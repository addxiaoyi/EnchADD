package com.enadd.enchantments.conflict;

import org.bukkit.enchantments.Enchantment;
import net.minecraft.resources.ResourceLocation;
import com.enadd.core.conflict.EnchantmentConflictManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;










public final class EnchantmentWeightManager {

    private static final Map<ResourceLocation, Integer> ENCHANTMENT_WEIGHTS = new ConcurrentHashMap<>();
    private static final Map<String, List<ResourceLocation>> WEIGHT_CATEGORIES = new ConcurrentHashMap<>();
    private static final Random WEIGHT_RANDOM = new Random();
    private static volatile boolean initialized = false;

    private static final int MIN_WEIGHT = 1;
    private static final int MAX_WEIGHT = 100;
    private static final int DEFAULT_WEIGHT = 10;

    private EnchantmentWeightManager() {}

    private static ResourceLocation rl(String id) {
        try {
            return ResourceLocation.tryParse(id);
        } catch (Exception e) {
            return null;
        }
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        initializeVanillaWeights();
        initializeCustomWeights();
        initializeWeightCategories();

        initialized = true;
    }

    private static void initializeVanillaWeights() {
        registerWeight("protection", 10);
        registerWeight("fire_protection", 5);
        registerWeight("feather_falling", 5);
        registerWeight("blast_protection", 5);
        registerWeight("projectile_protection", 5);

        registerWeight("sharpness", 10);
        registerWeight("smite", 5);
        registerWeight("bane_of_arthropods", 5);
        registerWeight("knockback", 5);
        registerWeight("fire_aspect", 2);
        registerWeight("looting", 2);
        registerWeight("sweeping", 2);

        registerWeight("efficiency", 10);
        registerWeight("silk_touch", 1);
        registerWeight("unbreaking", 5);
        registerWeight("fortune", 2);

        registerWeight("power", 10);
        registerWeight("punch", 2);
        registerWeight("flame", 2);
        registerWeight("infinity", 1);

        registerWeight("mending", 2);
        registerWeight("vanishing_curse", 1);
        registerWeight("binding_curse", 1);

        registerWeight("lure", 2);
        registerWeight("luck_of_the_sea", 2);
        registerWeight("respiration", 2);
        registerWeight("depth_strider", 2);
        registerWeight("aqua_affinity", 2);
        registerWeight("frost_walker", 2);

        registerWeight("thorns", 1);
        registerWeight("loyalty", 3);
        registerWeight("rill", 2);
        registerWeight("channeling", 2);

        registerWeight("soul_speed", 2);
        registerWeight("swift_sneak", 2);
    }

    private static void initializeCustomWeights() {
        registerWeight("cleave", 4);
        registerWeight("bleeding", 5);
        registerWeight("armor_pierce", 4);
        registerWeight("execution", 2);
        registerWeight("momentum", 5);
        registerWeight("disarm", 3);
        registerWeight("crippling", 4);
        registerWeight("reprisal", 4);
        registerWeight("hemorrhage", 5);
        registerWeight("backstab", 4);
        registerWeight("stagger", 4);
        registerWeight("rend", 4);
        registerWeight("savage", 3);
        registerWeight("duelist", 5);
        registerWeight("hunter", 4);
        registerWeight("juggernaut", 3);
        registerWeight("finisher", 3);
        registerWeight("vampirism", 2);

        registerWeight("chain_lightning", 3);
        registerWeight("frost_nova", 4);
        registerWeight("fire_storm", 3);
        registerWeight("shadow_strike", 4);
        registerWeight("berserker_rage", 3);
        registerWeight("life_stal", 3);
        registerWeight("mana_steal", 3);
        registerWeight("critical_strike", 5);
        registerWeight("poison_cloud", 4);
        registerWeight("thunder_strike", 3);
        registerWeight("void_slash", 2);
        registerWeight("dragon_breath", 3);
        registerWeight("soul_burn", 3);
        registerWeight("bloodlust", 2);
        registerWeight("death_mark", 3);

        registerWeight("stone_skin", 4);
        registerWeight("dodge", 5);
        registerWeight("reinforced_thorns", 3);
        registerWeight("barrier", 3);
        registerWeight("adrenaline", 4);
        registerWeight("willpower", 4);
        registerWeight("grounding", 5);
        registerWeight("thermostatic", 4);
        registerWeight("iron_will", 4);
        registerWeight("recoil", 3);
        registerWeight("endurance", 5);
        registerWeight("last_stand", 2);

        registerWeight("miner", 10);
        registerWeight("chain_mining", 4);
        registerWeight("prospecting", 5);
        registerWeight("auto_smelt", 8);
        registerWeight("magnetic", 5);
        registerWeight("tree_feller", 4);
        registerWeight("precision", 8);
        registerWeight("strong_draw", 6);
        registerWeight("catapult", 4);
        registerWeight("enhanced_piercing", 6);
        registerWeight("sniper", 5);
        registerWeight("frost_arrow", 4);
        registerWeight("signal_arrow", 5);
        registerWeight("silence", 4);
        registerWeight("harvest", 8);
        registerWeight("navigation", 6);

        registerWeight("titan_strength", 3);
        registerWeight("lightning_speed", 4);
        registerWeight("explosive_mining", 3);
        registerWeight("combo_breaker", 4);
        registerWeight("arbor_master", 4);
        registerWeight("fortunes_grace", 3);
        registerWeight("smelting_touch", 5);
        registerWeight("collector", 4);
        registerWeight("speed_surge", 5);
        registerWeight("ethereal_step", 4);
        registerWeight("heavy_hand", 5);
        registerWeight("shadow_veil", 3);
        registerWeight("climber", 6);
        registerWeight("intimidation", 3);
        registerWeight("torch_light", 5);
        registerWeight("ore_sight", 4);
        registerWeight("traveler", 5);
        registerWeight("vacuum", 4);
        registerWeight("void_mining", 2);
        registerWeight("transmutation", 3);
        registerWeight("multitool", 2);
        registerWeight("instant_mining", 2);
        registerWeight("infinite", 1);
        registerWeight("duplication", 1);
        registerWeight("builder", 4);
        registerWeight("area_mining", 3);
        registerWeight("auto_sort", 5);
        registerWeight("magnet", 4);

        registerWeight("auto_repair", 3);
        registerWeight("soul_bound", 2);
        registerWeight("teleport", 3);
        registerWeight("time_accel", 1);
        registerWeight("time_stop", 1);
        registerWeight("gravity", 3);
        registerWeight("anti_gravity", 3);
        registerWeight("cloud_step", 4);
        registerWeight("phase", 2);
        registerWeight("water_walk", 4);
        registerWeight("wall_walk", 3);
        registerWeight("dimension_shift", 1);
        registerWeight("weather_control", 1);
        registerWeight("xray", 2);
        registerWeight("light_source", 5);
        registerWeight("night_vision", 5);
        registerWeight("invisibility", 3);
        registerWeight("mind_control", 2);

        registerWeight("curse_fragile", 1);
        registerWeight("curse_sluggish", 1);
        registerWeight("curse_noise", 1);
        registerWeight("curse_binding_plus", 1);
        registerWeight("curse_drain", 1);
        registerWeight("curse_weakness", 1);
        registerWeight("curse_confusion", 1);

        registerWeight("meteor_strike", 1);
        registerWeight("homecoming", 2);

        registerWeight("binder", 3);
    }

    private static void initializeWeightCategories() {
        WEIGHT_CATEGORIES.put("weapon", Arrays.asList(
            rl("sharpness"),
            rl("smite"),
            rl("bane_of_arthropods"),
            rl("knockback"),
            rl("fire_aspect"),
            rl("looting"),
            rl("sweeping"),
            rl("cleave"),
            rl("bleeding"),
            rl("execution"),
            rl("momentum"),
            rl("vampirism"),
            rl("critical_strike"),
            rl("armor_pierce"),
            rl("disarm"),
            rl("crippling"),
            rl("hemorrhage"),
            rl("backstab"),
            rl("stagger"),
            rl("rend"),
            rl("savage"),
            rl("duelist"),
            rl("hunter"),
            rl("juggernaut"),
            rl("finisher")
        ));

        WEIGHT_CATEGORIES.put("armor", Arrays.asList(
            rl("protection"),
            rl("fire_protection"),
            rl("feather_falling"),
            rl("blast_protection"),
            rl("projectile_protection"),
            rl("thorns"),
            rl("stone_skin"),
            rl("dodge"),
            rl("reinforced_thorns"),
            rl("barrier"),
            rl("adrenaline"),
            rl("willpower"),
            rl("grounding"),
            rl("thermostatic"),
            rl("iron_will"),
            rl("recoil"),
            rl("endurance"),
            rl("last_stand")
        ));

        WEIGHT_CATEGORIES.put("tool", Arrays.asList(
            rl("efficiency"),
            rl("silk_touch"),
            rl("fortune"),
            rl("unbreaking"),
            rl("miner"),
            rl("chain_mining"),
            rl("prospecting"),
            rl("auto_smelt"),
            rl("magnetic"),
            rl("tree_feller"),
            rl("precision"),
            rl("strong_draw"),
            rl("catapult"),
            rl("enhanced_piercing"),
            rl("sniper"),
            rl("frost_arrow"),
            rl("signal_arrow"),
            rl("silence"),
            rl("harvest"),
            rl("navigation"),
            rl("titan_strength"),
            rl("lightning_speed"),
            rl("explosive_mining"),
            rl("smelting_touch"),
            rl("speed_surge"),
            rl("collector"),
            rl("heavy_hand"),
            rl("climber"),
            rl("torch_light"),
            rl("ore_sight"),
            rl("traveler")
        ));

        WEIGHT_CATEGORIES.put("bow", Arrays.asList(
            rl("power"),
            rl("punch"),
            rl("flame"),
            rl("infinity"),
            rl("sniper"),
            rl("hunter"),
            rl("frost_arrow"),
            rl("signal_arrow")
        ));

        WEIGHT_CATEGORIES.put("fishing_rod", Arrays.asList(
            rl("luck_of_the_sea"),
            rl("lure"),
            rl("unbreaking")
        ));

        WEIGHT_CATEGORIES.put("trident", Arrays.asList(
            rl("loyalty"),
            rl("rill"),
            rl("channeling"),
            rl("impaling"),
            rl("riptide")
        ));

        WEIGHT_CATEGORIES.put("crossbow", Arrays.asList(
            rl("multishot"),
            rl("piercing"),
            rl("quick_charge")
        ));

        WEIGHT_CATEGORIES.put("utility", Arrays.asList(
            rl("mending"),
            rl("unbreaking"),
            rl("soul_speed"),
            rl("swift_sneak"),
            rl("depth_strider"),
            rl("aqua_affinity"),
            rl("respiration"),
            rl("auto_repair"),
            rl("soul_bound"),
            rl("teleport"),
            rl("cloud_step"),
            rl("water_walk"),
            rl("wall_walk"),
            rl("light_source"),
            rl("night_vision"),
            rl("invisibility")
        ));

        WEIGHT_CATEGORIES.put("special", Arrays.asList(
            rl("meteor_strike"),
            rl("homecoming"),
            rl("binder"),
            rl("time_accel"),
            rl("time_stop"),
            rl("gravity"),
            rl("anti_gravity"),
            rl("phase"),
            rl("dimension_shift"),
            rl("weather_control"),
            rl("xray"),
            rl("mind_control"),
            rl("transmutation"),
            rl("multitool"),
            rl("instant_mining"),
            rl("infinite"),
            rl("duplication")
        ));

        WEIGHT_CATEGORIES.put("curse", Arrays.asList(
            rl("vanishing_curse"),
            rl("binding_curse"),
            rl("curse_fragile"),
            rl("curse_sluggish"),
            rl("curse_noise"),
            rl("curse_binding_plus"),
            rl("curse_drain"),
            rl("curse_weakness"),
            rl("curse_confusion")
        ));
    }

    public static void registerWeight(ResourceLocation enchantment, int weight) {
        int normalizedWeight = normalizeWeight(weight);
        ENCHANTMENT_WEIGHTS.put(enchantment, normalizedWeight);
    }

    public static void registerWeight(String enchantmentId, int weight) {
        registerWeight(rl(enchantmentId), weight);
    }

    public static int getWeight(Enchantment enchantment) {
        if (enchantment == null) {
            return DEFAULT_WEIGHT;
        }

        ResourceLocation loc = EnchantmentConflictManager.getEnchantmentLocation(enchantment);
        if (loc == null) {
            return DEFAULT_WEIGHT;
        }

        return getWeight(loc);
    }

    public static int getWeight(ResourceLocation enchantment) {
        return ENCHANTMENT_WEIGHTS.getOrDefault(enchantment, DEFAULT_WEIGHT);
    }

    public static void setWeight(ResourceLocation enchantment, int weight) {
        ENCHANTMENT_WEIGHTS.put(enchantment, normalizeWeight(weight));
    }

    public static void setWeight(String enchantmentId, int weight) {
        setWeight(rl(enchantmentId), weight);
    }

    private static int normalizeWeight(int weight) {
        return Math.max(MIN_WEIGHT, Math.min(weight, MAX_WEIGHT));
    }

    public static List<ResourceLocation> getAvailableEnchantments(Collection<ResourceLocation> candidates, int availableSlots) {
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        List<ResourceLocation> result = new ArrayList<>();
        List<ResourceLocation> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled, WEIGHT_RANDOM);

        int totalWeight = 0;
        for (ResourceLocation ench : shuffled) {
            totalWeight += getWeight(ench);
        }

        int remainingWeight = totalWeight;
        int slotsFilled = 0;

        for (ResourceLocation ench : shuffled) {
            if (slotsFilled >= availableSlots) {
                break;
            }

            int enchWeight = getWeight(ench);
            double probability = (double) enchWeight / remainingWeight;

            if (WEIGHT_RANDOM.nextDouble() < probability) {
                result.add(ench);
                slotsFilled++;
                remainingWeight -= enchWeight;
            }
        }

        return result;
    }

    public static List<Enchantment> getAvailableEnchantmentsByWeight(Collection<Enchantment> candidates, int availableSlots) {
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        List<ResourceLocation> candidateLocations = new ArrayList<>();
        for (Enchantment ench : candidates) {
            ResourceLocation loc = EnchantmentConflictManager.getEnchantmentLocation(ench);
            if (loc != null) {
                candidateLocations.add(loc);
            }
        }

        List<ResourceLocation> selected = getAvailableEnchantments(candidateLocations, availableSlots);

        List<Enchantment> result = new ArrayList<>();
        for (Enchantment ench : candidates) {
            ResourceLocation loc = EnchantmentConflictManager.getEnchantmentLocation(ench);
            if (loc != null && selected.contains(loc)) {
                result.add(ench);
            }
        }

        return result;
    }

    public static Enchantment selectEnchantmentByWeight(Collection<Enchantment> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        List<Enchantment> candidateList = new ArrayList<>(candidates);
        List<ResourceLocation> candidateLocations = new ArrayList<>();

        for (Enchantment ench : candidateList) {
            ResourceLocation loc = EnchantmentConflictManager.getEnchantmentLocation(ench);
            if (loc != null) {
                candidateLocations.add(loc);
            }
        }

        int totalWeight = 0;
        for (ResourceLocation loc : candidateLocations) {
            totalWeight += getWeight(loc);
        }

        if (totalWeight <= 0) {
            return candidateList.get(WEIGHT_RANDOM.nextInt(candidateList.size()));
        }

        int randomValue = WEIGHT_RANDOM.nextInt(totalWeight);
        int currentWeight = 0;

        for (int i = 0; i < candidateLocations.size(); i++) {
            currentWeight += getWeight(candidateLocations.get(i));
            if (currentWeight > randomValue) {
                return candidateList.get(i);
            }
        }

        return candidateList.get(candidateList.size() - 1);
    }

    public static int calculateTotalWeightForLocations(Collection<ResourceLocation> enchantments) {
        int total = 0;
        for (ResourceLocation ench : enchantments) {
            total += getWeight(ench);
        }
        return total;
    }

    public static int calculateTotalWeight(Collection<Enchantment> enchantments) {
        int total = 0;
        for (Enchantment ench : enchantments) {
            total += getWeight(ench);
        }
        return total;
    }

    public static List<ResourceLocation> getEnchantmentsByWeight(String category) {
        List<ResourceLocation> categoryEnchantments = WEIGHT_CATEGORIES.get(category);
        if (categoryEnchantments == null || categoryEnchantments.isEmpty()) {
            return new ArrayList<>();
        }

        List<ResourceLocation> sorted = new ArrayList<>(categoryEnchantments);
        sorted.sort((e1, e2) -> Integer.compare(getWeight(e2), getWeight(e1)));

        return sorted;
    }

    public static Map<ResourceLocation, Integer> getAllWeights() {
        return new HashMap<>(ENCHANTMENT_WEIGHTS);
    }

    public static List<String> getWeightCategories() {
        return new ArrayList<>(WEIGHT_CATEGORIES.keySet());
    }

    public static List<ResourceLocation> getEnchantmentsInCategory(String category) {
        return WEIGHT_CATEGORIES.getOrDefault(category, new ArrayList<>());
    }

    public static boolean isValidWeightCategory(String category) {
        return WEIGHT_CATEGORIES.containsKey(category);
    }

    public static int getMinWeight() {
        return MIN_WEIGHT;
    }

    public static int getMaxWeight() {
        return MAX_WEIGHT;
    }

    public static int getDefaultWeight() {
        return DEFAULT_WEIGHT;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void reset() {
        ENCHANTMENT_WEIGHTS.clear();
        WEIGHT_CATEGORIES.clear();
        initialized = false;
    }

    public static double getRarityFactor(Enchantment enchantment) {
        int weight = getWeight(enchantment);

        if (weight >= 80) {
            return 0.01;
        } else if (weight >= 60) {
            return 0.05;
        } else if (weight >= 40) {
            return 0.15;
        } else if (weight >= 20) {
            return 0.35;
        } else if (weight >= 10) {
            return 0.60;
        } else {
            return 0.85;
        }
    }

    public static String getRarityName(Enchantment enchantment) {
        int weight = getWeight(enchantment);

        if (weight >= 80) {
            return "legendary";
        } else if (weight >= 60) {
            return "epic";
        } else if (weight >= 40) {
            return "rare";
        } else if (weight >= 20) {
            return "uncommon";
        } else if (weight >= 10) {
            return "common";
        } else {
            return "cursed";
        }
    }

    public static void shutdown() {
        ENCHANTMENT_WEIGHTS.clear();
        WEIGHT_CATEGORIES.clear();
        initialized = false;
    }
}




