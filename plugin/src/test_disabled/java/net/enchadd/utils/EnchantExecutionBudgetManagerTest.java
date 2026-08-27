package net.enchadd.utils;

import net.enchadd.EnchADDConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantExecutionBudgetManagerTest {

    private Field runtimeConfigField;
    private Object originalRuntimeConfigSnapshot;
    private AtomicLong tickCounter;

    @BeforeEach
    void setUp() throws Exception {
        this.runtimeConfigField = EnchADDConfig.class.getDeclaredField("runtimeConfig");
        this.runtimeConfigField.setAccessible(true);
        this.originalRuntimeConfigSnapshot = this.runtimeConfigField.get(null);

        Field tickCounterField = EnchantExecutionBudgetManager.class.getDeclaredField("tickCounter");
        tickCounterField.setAccessible(true);
        this.tickCounter = (AtomicLong) tickCounterField.get(null);

        EnchantExecutionBudgetManager.stop();
    }

    @AfterEach
    void tearDown() throws Exception {
        EnchantExecutionBudgetManager.stop();
        this.runtimeConfigField.set(null, this.originalRuntimeConfigSnapshot);
    }

    @Test
    void overBudgetInSameTickEntersDegradedModeAndAppliesMultipliers() throws IOException {
        applyBudgetConfig(
                100_000,
                0.50,
                3,
                true,
                8,
                20,
                false
        );

        this.tickCounter.set(120L);
        EnchantExecutionBudgetManager.ExecutionToken first = EnchantExecutionBudgetManager.enter("enchadd:test");
        assertFalse(first.degraded());
        EnchantExecutionBudgetManager.exit(first, 150_000L);

        EnchantExecutionBudgetManager.ExecutionToken second = EnchantExecutionBudgetManager.enter("enchadd:test");
        assertTrue(second.degraded(), "same tick should degrade after budget exhaustion");
        assertFalse(second.skipExecution(), "breaker is not open yet, should not skip");
        assertEquals(0.50, EnchantExecutionBudgetManager.getCurrentChanceMultiplier(), 1.0e-9);
        assertEquals(3, EnchantExecutionBudgetManager.getCurrentTickModuloMultiplier());
        assertTrue(EnchantExecutionBudgetManager.shouldSuppressParticlesForCurrentExecution());
        EnchantExecutionBudgetManager.exit(second, 0L);

        assertEquals(1.0, EnchantExecutionBudgetManager.getCurrentChanceMultiplier(), 1.0e-9);
    }

    @Test
    void consecutiveOverrunsOpenBreakerAndBreakerCanSkipExecution() throws IOException {
        applyBudgetConfig(
                100_000,
                0.60,
                2,
                true,
                2,
                4,
                true
        );

        this.tickCounter.set(200L);
        EnchantExecutionBudgetManager.ExecutionToken firstTick = EnchantExecutionBudgetManager.enter("enchadd:combo");
        EnchantExecutionBudgetManager.exit(firstTick, 140_000L);

        this.tickCounter.set(201L);
        EnchantExecutionBudgetManager.ExecutionToken secondTick = EnchantExecutionBudgetManager.enter("enchadd:combo");
        EnchantExecutionBudgetManager.exit(secondTick, 140_000L);

        this.tickCounter.set(202L);
        EnchantExecutionBudgetManager.ExecutionToken breakerTick = EnchantExecutionBudgetManager.enter("enchadd:combo");
        assertTrue(breakerTick.degraded(), "breaker-open tick should be degraded");
        assertTrue(breakerTick.skipExecution(), "breaker skip-execution should be active");
        EnchantExecutionBudgetManager.exit(breakerTick, 0L);

        assertTrue(EnchantExecutionBudgetManager.getOpenBreakerCount() >= 1);
        assertTrue(EnchantExecutionBudgetManager.getSkippedExecutionCount() >= 1L);

        this.tickCounter.set(206L);
        EnchantExecutionBudgetManager.ExecutionToken recovered = EnchantExecutionBudgetManager.enter("enchadd:combo");
        assertFalse(recovered.skipExecution(), "after cooldown ticks, breaker should close");
        EnchantExecutionBudgetManager.exit(recovered, 0L);
    }

    @Test
    void snapshotOverBudgetNanosTracksCurrentTickOnly() throws IOException {
        applyBudgetConfig(
                100_000,
                0.65,
                2,
                true,
                5,
                40,
                false
        );

        this.tickCounter.set(300L);
        EnchantExecutionBudgetManager.ExecutionToken token = EnchantExecutionBudgetManager.enter("enchadd:sample");
        EnchantExecutionBudgetManager.exit(token, 170_000L);

        Map<String, Long> currentTickSnapshot = EnchantExecutionBudgetManager.snapshotOverBudgetNanos();
        assertTrue(currentTickSnapshot.containsKey("enchadd:sample"));
        assertTrue(currentTickSnapshot.get("enchadd:sample") >= 70_000L);
        assertTrue(EnchantExecutionBudgetManager.getTrackedEnchantCount() >= 1L);

        this.tickCounter.set(301L);
        Map<String, Long> nextTickSnapshot = EnchantExecutionBudgetManager.snapshotOverBudgetNanos();
        assertTrue(nextTickSnapshot.isEmpty(), "snapshot should only report the current tick");
    }

    private static void applyBudgetConfig(long nanosPerTick,
                                          double degradedChanceMultiplier,
                                          int degradedTickModuloMultiplier,
                                          boolean suppressParticles,
                                          int breakerConsecutiveOverruns,
                                          int breakerCooldownTicks,
                                          boolean breakerSkipExecution) throws IOException {
        Path tempDir = Files.createTempDirectory("enchadd-budget-config-");
        try {
            String yaml = String.join("\n",
                    "language: zh",
                    "debug: false",
                    "enchant-budget:",
                    "  nanos-per-tick: " + nanosPerTick,
                    "  degraded-chance-multiplier: " + degradedChanceMultiplier,
                    "  degraded-tick-modulo-multiplier: " + degradedTickModuloMultiplier,
                    "  suppress-particles: " + suppressParticles,
                    "  breaker-consecutive-overruns: " + breakerConsecutiveOverruns,
                    "  breaker-cooldown-ticks: " + breakerCooldownTicks,
                    "  breaker-skip-execution: " + breakerSkipExecution,
                    ""
            );
            Files.writeString(tempDir.resolve("config.yml"), yaml, StandardCharsets.UTF_8);
            assertTrue(EnchADDConfig.reloadRuntimeSettings(tempDir), "runtime config reload should succeed");
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(root)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // Best-effort cleanup for temporary test directories.
                }
            });
        }
    }
}
