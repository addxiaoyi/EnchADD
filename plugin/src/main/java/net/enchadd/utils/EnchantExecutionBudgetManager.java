package net.enchadd.utils;

import net.enchadd.EnchADDConfig;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Per-enchant execution budget and circuit-breaker guard.
 */
public final class EnchantExecutionBudgetManager {

    private static final EnchantExecutionBudgetState STATE = new EnchantExecutionBudgetState();
    static final AtomicLong tickCounter = STATE.tickCounter;
    private static final ThreadLocal<ExecutionToken> currentExecution = new ThreadLocal<>();

    private EnchantExecutionBudgetManager() {
    }

    public static void start(@NotNull JavaPlugin owningPlugin) {
        EnchantExecutionBudgetScheduler.start(STATE, owningPlugin);
    }

    public static void stop() {
        EnchantExecutionBudgetScheduler.stop(STATE, currentExecution);
    }

    @NotNull
    public static ExecutionToken enter(@NotNull String enchantKey) {
        return EnchantExecutionBudgetEngine.enter(STATE, currentExecution, enchantKey);
    }

    public static void exit(@Nullable ExecutionToken token, long elapsedNanos) {
        EnchantExecutionBudgetEngine.exit(STATE, currentExecution, token, elapsedNanos);
    }

    public static boolean isCurrentExecutionDegraded() {
        ExecutionToken token = currentExecution.get();
        return token != null && token.degraded();
    }

    public static boolean shouldSuppressParticlesForCurrentExecution() {
        return isCurrentExecutionDegraded() && EnchADDConfig.isEnchantBudgetSuppressParticles();
    }

    public static double getCurrentChanceMultiplier() {
        return EnchantExecutionBudgetQuery.getCurrentChanceMultiplier(STATE, currentExecution);
    }

    public static int getCurrentTickModuloMultiplier() {
        return EnchantExecutionBudgetQuery.getCurrentTickModuloMultiplier(STATE, currentExecution);
    }

    public static long getDegradedExecutionCount() {
        return EnchantExecutionBudgetQuery.getDegradedExecutionCount(STATE);
    }

    public static long getSkippedExecutionCount() {
        return EnchantExecutionBudgetQuery.getSkippedExecutionCount(STATE);
    }

    public static int getOpenBreakerCount() {
        return EnchantExecutionBudgetQuery.getOpenBreakerCount(STATE);
    }

    public static long getTrackedEnchantCount() {
        return EnchantExecutionBudgetQuery.getTrackedEnchantCount(STATE);
    }

    @NotNull
    public static Map<String, Long> snapshotOverBudgetNanos() {
        return EnchantExecutionBudgetQuery.snapshotOverBudgetNanos(STATE);
    }

    public static final class ExecutionToken {
        final @NotNull String enchantKey;
        final long tick;
        final long budgetNanos;
        final boolean degraded;
        final boolean skipExecution;
        final @NotNull EnchantExecutionBudgetState.BudgetState state;

        ExecutionToken(@NotNull String enchantKey,
                       long tick,
                       long budgetNanos,
                       boolean degraded,
                       boolean skipExecution,
                       @NotNull EnchantExecutionBudgetState.BudgetState state) {
            this.enchantKey = enchantKey;
            this.tick = tick;
            this.budgetNanos = budgetNanos;
            this.degraded = degraded;
            this.skipExecution = skipExecution;
            this.state = state;
        }

        public @NotNull String enchantKey() {
            return enchantKey;
        }

        public long tick() {
            return tick;
        }

        public long budgetNanos() {
            return budgetNanos;
        }

        public boolean degraded() {
            return degraded;
        }

        public boolean skipExecution() {
            return skipExecution;
        }

        @NotNull EnchantExecutionBudgetState.BudgetState state() {
            return state;
        }
    }

    static final class EnchantExecutionBudgetState {
        final java.util.concurrent.ConcurrentHashMap<String, BudgetState> states = new java.util.concurrent.ConcurrentHashMap<>();
        final AtomicLong tickCounter = new AtomicLong(0L);
        final AtomicLong degradedExecutions = new AtomicLong(0L);
        final AtomicLong skippedExecutions = new AtomicLong(0L);
        volatile JavaPlugin plugin;
        volatile BukkitTask tickTask;

        static final class BudgetState {
            long tick = -1L;
            long usedNanos = 0L;
            boolean tickExceeded = false;
            int consecutiveExceededTicks = 0;
            long breakerUntilTick = -1L;
            long totalExecutions = 0L;
            long totalNanos = 0L;
            long degradedCount = 0L;
            long skippedCount = 0L;
        }
    }
}
