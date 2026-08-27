package net.enchadd;

import net.enchadd.config.RuntimeConfigLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchADDConfigRuntimeCompatibilityTest {

    private Field runtimeConfigField;
    private Object originalRuntimeConfig;
    private boolean originalDebug;

    @BeforeEach
    void setUp() throws Exception {
        runtimeConfigField = EnchADDConfig.class.getDeclaredField("runtimeConfig");
        runtimeConfigField.setAccessible(true);
        originalRuntimeConfig = runtimeConfigField.get(null);
        originalDebug = EnchADDConfig.DEBUG;
    }

    @AfterEach
    void tearDown() throws Exception {
        runtimeConfigField.set(null, originalRuntimeConfig);
        EnchADDConfig.DEBUG = originalDebug;
    }

    @Test
    void runtimeConfigFieldStaysInSyncWithReloadedSettings() throws Exception {
        Path tempDir = Files.createTempDirectory("enchadd-runtime-compat-");
        try {
            Files.writeString(tempDir.resolve("config.yml"), String.join("\n",
                    "language: zh",
                    "debug: true",
                    "enchant-budget:",
                    "  nanos-per-tick: 7654321",
                    "  degraded-chance-multiplier: 0.42",
                    "  degraded-tick-modulo-multiplier: 4",
                    "  suppress-particles: false",
                    "  breaker-consecutive-overruns: 5",
                    "  breaker-cooldown-ticks: 90",
                    "  breaker-skip-execution: true",
                    ""
            ), StandardCharsets.UTF_8);

            assertTrue(EnchADDConfig.reloadRuntimeSettings(tempDir));
            assertEquals(7654321L, EnchADDConfig.getEnchantBudgetNanosPerTick());
            assertTrue(EnchADDConfig.DEBUG);

            RuntimeConfigLoader.RuntimeConfigSnapshot snapshot =
                    (RuntimeConfigLoader.RuntimeConfigSnapshot) runtimeConfigField.get(null);
            assertEquals(7654321L, snapshot.enchantBudgetNanosPerTick());
            assertTrue(!snapshot.enchantBudgetSuppressParticles());
            assertEquals(5, snapshot.enchantBudgetBreakerConsecutiveOverruns());
            assertEquals(90, snapshot.enchantBudgetBreakerCooldownTicks());
            assertTrue(snapshot.enchantBudgetSkipExecutionOnBreaker());
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private static void deleteRecursively(Path root) throws Exception {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(root)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception ignored) {
                    // best-effort cleanup
                }
            });
        }
    }
}
