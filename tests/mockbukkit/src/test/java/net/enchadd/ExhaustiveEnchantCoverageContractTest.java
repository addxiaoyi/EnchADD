package net.enchadd;

import net.enchadd.enchants.EnchADDEnchant;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExhaustiveEnchantCoverageContractTest {

    private static final Path ENCHANTS_DIR = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
            Path.of("net", "enchadd", "enchants")
    );
    private static final Path LISTENERS_DIR = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
            Path.of("net", "enchadd", "listeners")
    );
    private static final Path CONFIG_SOURCE = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
            Path.of("net", "enchadd", "EnchADDConfig.java")
    );
    private static final Path ACQUISITION_POLICY_SOURCE = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
            Path.of("net", "enchadd", "config", "EnchantAcquisitionPolicy.java")
    );
    private static final Path REGISTRATION_ASSEMBLER_SOURCE = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
            Path.of("net", "enchadd", "config", "EnchantRegistrationAssembler.java")
    );
    private static final Path LISTENER_REGISTRATION_ASSEMBLER_SOURCE = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
            Path.of("net", "enchadd", "listeners", "ListenerRegistrationAssembler.java")
    );
    private static final Path LISTENER_REGISTRATION_PLAN_SOURCE = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
            Path.of("net", "enchadd", "listeners", "ListenerRegistrationPlan.java")
    );
    private static final Path REGISTRATION_PLAN_SOURCE = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
            Path.of("net", "enchadd", "config", "EnchantRegistrationPlan.java")
    );
    private static final Path LIFECYCLE_SOURCE = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
            Path.of("net", "enchadd", "EnchADDLifecycleService.java")
    );
    private static final Set<String> EXCLUDED_ENCHANT_TYPES = Set.of(
            "AbstractEnchADDEnchant",
            "EnchADDEnchant",
            "ChanceEnchant",
            "CooldownEnchant"
    );
    private static final Set<String> EXCLUDED_LISTENER_TYPES = Set.of(
            "CurseConflictListener",
            "EnchantListenerRegistrar",
            "LifecycleListener",
            "LegacyEnchantSanitizerListener",
            "ServerExceptionMonitorListener"
    );
    private static final Pattern KEY_PATTERN = Pattern.compile(
            "public\\s+static\\s+final\\s+Key\\s+KEY\\s*=\\s*Key\\.key\\(\"([^\"]+)\"\\)"
    );
    private static final Pattern ENCHANT_SECTION_KEY_PATTERN = Pattern.compile(
            "getConfigSection\\(enchantsSection,\\s*\"([^\"]+)\"\\)"
    );
    private static final Pattern CURSE_SECTION_KEY_PATTERN = Pattern.compile(
            "getConfigSection\\(cursesSection,\\s*\"([^\"]+)\"\\)"
    );

    @BeforeEach
    void setUp() throws Exception {
        resetEnchantConfigState();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetEnchantConfigState();
    }

    @Test
    void everyConcreteEnchantIsWiredThroughConfigAndLifecycle() throws IOException {
        Set<String> enchantTypes = concreteEnchantTypes();
        Set<String> listenerTypes = enchantListenerTypes();
        Map<String, String> expectedKeysByType = extractExpectedKeys(enchantTypes);
        String registrationPlanSource = Files.readString(REGISTRATION_PLAN_SOURCE, StandardCharsets.UTF_8);
        String lifecycleSource = Files.readString(LIFECYCLE_SOURCE, StandardCharsets.UTF_8);
        String listenerRegistrarSource = Files.readString(LISTENER_REGISTRATION_ASSEMBLER_SOURCE, StandardCharsets.UTF_8);
        String listenerPlanSource = Files.readString(LISTENER_REGISTRATION_PLAN_SOURCE, StandardCharsets.UTF_8);

        assertTrue(
                lifecycleSource.contains("EnchantListenerRegistrar.registerAll(plugin);"),
                "Lifecycle should delegate listener registration to EnchantListenerRegistrar"
        );
        assertTrue(
                listenerRegistrarSource.contains("ListenerRegistry.registerIfEnabled(entry.key(), () -> entry.factory().create(plugin));"),
                "EnchantListenerRegistrar should delegate to ListenerRegistrationAssembler"
        );

        assertEquals(
                enchantTypes.size(),
                listenerTypes.size(),
                "Every concrete enchant should have exactly one dedicated listener type"
        );

        for (String enchantType : enchantTypes) {
            String listenerType = enchantType.replace("Enchant", "Listener");
            assertTrue(
                    listenerTypes.contains(listenerType),
                    () -> "Missing listener class for enchant: " + enchantType
            );
            assertTrue(
                    registrationPlanSource.contains(enchantType + "::create"),
                    () -> "EnchantRegistrationPlan should include " + enchantType + "::create"
            );
            assertTrue(
                    isRegisteredInListenerPlan(listenerPlanSource, enchantTypeToKey(enchantType), listenerType),
                    () -> "ListenerRegistrationPlan should register " + listenerType + " for " + enchantType
            );
        }
    }

    @Test
    void defaultConfigRegistersAllDeclaredEnchantKeysAndRespectsBasicBounds() throws Exception {
        Set<String> enchantTypes = concreteEnchantTypes();
        Map<String, String> expectedKeysByType = extractExpectedKeys(enchantTypes);

        Path tempDataDir = Files.createTempDirectory("enchadd-config-contract-");
        try {
            EnchADDConfig.init(tempDataDir);

            Set<String> expectedKeys = new HashSet<>(expectedKeysByType.values());
            Set<String> actualKeys = EnchADDConfig.ENCHANTS.keySet().stream()
                    .map(Key::asString)
                    .collect(Collectors.toSet());

            assertEquals(expectedKeys, actualKeys, "Default config should register all declared enchant keys");
            assertEquals(expectedKeys.size(), EnchADDConfig.ENCHANTS.size(), "Unexpected number of registered enchants");

            for (Map.Entry<Key, EnchADDEnchant> entry : EnchADDConfig.ENCHANTS.entrySet()) {
                EnchADDEnchant enchant = entry.getValue();
                assertNotNull(enchant.getMinimumCost(), () -> "Minimum cost is required for " + entry.getKey());
                assertNotNull(enchant.getMaximumCost(), () -> "Maximum cost is required for " + entry.getKey());
                assertTrue(enchant.getMaxLevel() >= 1, () -> "maxLevel should be >= 1 for " + entry.getKey());
                assertTrue(enchant.getWeight() >= 0, () -> "weight should be >= 0 for " + entry.getKey());
                assertTrue(enchant.getAnvilCost() >= 0, () -> "anvilCost should be >= 0 for " + entry.getKey());
                assertTrue(enchant.getActiveSlots().iterator().hasNext(), () -> "activeSlots should not be empty for " + entry.getKey());
                assertFalse(enchant.getSupportedItems().isEmpty(), () -> "supportedItems should not be empty for " + entry.getKey());
                assertFalse(enchant.getEnchantTagKeys().isEmpty(), () -> "enchantmentTags should not be empty for " + entry.getKey());
            }
        } finally {
            deleteRecursively(tempDataDir);
        }
    }

    @Test
    void everyConfiguredSectionKeyHasAnAcquisitionTier() throws IOException {
        String registrationPlanSource = Files.readString(REGISTRATION_PLAN_SOURCE, StandardCharsets.UTF_8);
        String acquisitionPolicySource = Files.readString(ACQUISITION_POLICY_SOURCE, StandardCharsets.UTF_8);

        String normalSection = registrationPlanSource.substring(
                registrationPlanSource.indexOf("static List<Entry> normalEntries()"),
                registrationPlanSource.indexOf("static List<Entry> curseEntries()")
        );
        String curseSection = registrationPlanSource.substring(registrationPlanSource.indexOf("static List<Entry> curseEntries()"));

        Set<String> enchantSectionKeys = extractMatches(normalSection, Pattern.compile("new Entry\\(\"([^\"]+)\""));
        Set<String> curseSectionKeys = extractMatches(curseSection, Pattern.compile("new Entry\\(\"([^\"]+)\""));

        Set<String> enchantTierKeys = new HashSet<>();
        enchantTierKeys.addAll(extractTierSet(acquisitionPolicySource, "TABLE_COMMON_ENCHANTS"));
        enchantTierKeys.addAll(extractTierSet(acquisitionPolicySource, "TABLE_SPECIAL_ENCHANTS"));
        enchantTierKeys.addAll(extractTierSet(acquisitionPolicySource, "TREASURE_SPECIAL_ENCHANTS"));
        enchantTierKeys.addAll(extractTierSet(acquisitionPolicySource, "TREASURE_ONLY_ENCHANTS"));

        Set<String> curseTierKeys = extractTierSet(acquisitionPolicySource, "CURSE_ENCHANTS");

        assertEquals(enchantSectionKeys, enchantTierKeys, "Every configured normal enchant should be classified into acquisition tiers");
        assertEquals(curseSectionKeys, curseTierKeys, "Every configured curse should be classified into curse acquisition tier");
    }

    private static Set<String> concreteEnchantTypes() throws IOException {
        try (var stream = Files.list(ENCHANTS_DIR)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith("Enchant.java"))
                    .map(name -> name.substring(0, name.length() - ".java".length()))
                    .filter(name -> !EXCLUDED_ENCHANT_TYPES.contains(name))
                    .collect(Collectors.toSet());
        }
    }

    private static Set<String> enchantListenerTypes() throws IOException {
        try (var stream = Files.list(LISTENERS_DIR)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith("Listener.java"))
                    .map(name -> name.substring(0, name.length() - ".java".length()))
                    .filter(name -> !EXCLUDED_LISTENER_TYPES.contains(name))
                    .collect(Collectors.toSet());
        }
    }

    private static Map<String, String> extractExpectedKeys(Set<String> enchantTypes) throws IOException {
        Map<String, String> keys = new HashMap<>();
        for (String enchantType : enchantTypes) {
            Path source = ENCHANTS_DIR.resolve(enchantType + ".java");
            String text = Files.readString(source, StandardCharsets.UTF_8);
            Matcher matcher = KEY_PATTERN.matcher(text);
            assertTrue(matcher.find(), () -> "Missing KEY declaration in " + enchantType);
            keys.put(enchantType, matcher.group(1));
        }
        return keys;
    }

    private static Set<String> extractTierSet(String source, String setName) {
        Pattern setPattern = Pattern.compile(setName + "\\s*=\\s*Set\\.of\\((.*?)\\);", Pattern.DOTALL);
        Matcher matcher = setPattern.matcher(source);
        assertTrue(matcher.find(), () -> "Missing tier set declaration: " + setName);
        return extractMatches(matcher.group(1), Pattern.compile("\"([^\"]+)\""));
    }

    private static Set<String> extractMatches(String source, Pattern pattern) {
        Set<String> matches = new HashSet<>();
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        return matches;
    }

    private static boolean isRegisteredInListenerPlan(String planSource, String key, String listenerType) {
        String directCtor = "entry(\"" + key + "\", " + listenerType + "::new)";
        String pluginCtor = "entry(\"" + key + "\", plugin -> new " + listenerType + "(plugin))";
        return planSource.contains(directCtor) || planSource.contains(pluginCtor);
    }

    private static String enchantTypeToKey(String enchantType) {
        return enchantType
                .replace("Enchant", "")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
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

    private static void deleteRecursively(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // test temp cleanup best-effort
                }
            });
        }
    }
}
