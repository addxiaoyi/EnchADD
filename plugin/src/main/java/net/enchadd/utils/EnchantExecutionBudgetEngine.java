package net.enchadd.utils;

import net.enchadd.EnchADDConfig;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class EnchantExecutionBudgetEngine {

    private EnchantExecutionBudgetEngine() {
    }

    @NotNull
    static EnchantExecutionBudgetManager.ExecutionToken enter(@NotNull EnchantExecutionBudgetManager.EnchantExecutionBudgetState state,
                                                              @NotNull ThreadLocal<EnchantExecutionBudgetManager.ExecutionToken> currentExecution,
                                                              @NotNull String enchantKey) {
        String normalizedKey = normalizeKey(enchantKey);
        EnchantExecutionBudgetManager.EnchantExecutionBudgetState.BudgetState budgetState =
                state.states.computeIfAbsent(normalizedKey, k -> new EnchantExecutionBudgetManager.EnchantExecutionBudgetState.BudgetState());
        long tick = state.tickCounter.get();
        long budgetNanos = Math.max(1L, EnchADDConfig.getEnchantBudgetNanosPerTick());

        boolean degraded;
        boolean skipExecution;
        synchronized (budgetState) {
            rollToTick(budgetState, tick);
            boolean breakerOpen = tick < budgetState.breakerUntilTick;
            boolean overBudget = budgetState.usedNanos >= budgetNanos;
            degraded = breakerOpen || overBudget;
            skipExecution = breakerOpen && EnchADDConfig.isEnchantBudgetSkipExecutionOnBreaker();
            if (degraded) {
                budgetState.degradedCount++;
                state.degradedExecutions.incrementAndGet();
            }
            if (skipExecution) {
                budgetState.skippedCount++;
                state.skippedExecutions.incrementAndGet();
            }
        }

        EnchantExecutionBudgetManager.ExecutionToken token = new EnchantExecutionBudgetManager.ExecutionToken(
                normalizedKey,
                tick,
                budgetNanos,
                degraded,
                skipExecution,
                budgetState
        );
        currentExecution.set(token);
        return token;
    }

    static void exit(@NotNull EnchantExecutionBudgetManager.EnchantExecutionBudgetState state,
                     @NotNull ThreadLocal<EnchantExecutionBudgetManager.ExecutionToken> currentExecution,
                     @Nullable EnchantExecutionBudgetManager.ExecutionToken token,
                     long elapsedNanos) {
        if (token == null) {
            currentExecution.remove();
            return;
        }
        long safeElapsed = Math.max(0L, elapsedNanos);
        EnchantExecutionBudgetManager.EnchantExecutionBudgetState.BudgetState budgetState = token.state();
        long tick = state.tickCounter.get();
        long budgetNanos = Math.max(1L, EnchADDConfig.getEnchantBudgetNanosPerTick());
        int consecutiveThreshold = Math.max(1, EnchADDConfig.getEnchantBudgetBreakerConsecutiveOverruns());
        int cooldownTicks = Math.max(1, EnchADDConfig.getEnchantBudgetBreakerCooldownTicks());

        synchronized (budgetState) {
            rollToTick(budgetState, tick);
            budgetState.totalExecutions++;
            budgetState.totalNanos += safeElapsed;
            budgetState.usedNanos += safeElapsed;

            if (!budgetState.tickExceeded && budgetState.usedNanos > budgetNanos) {
                budgetState.tickExceeded = true;
                budgetState.consecutiveExceededTicks++;
                if (budgetState.consecutiveExceededTicks >= consecutiveThreshold) {
                    budgetState.breakerUntilTick = Math.max(budgetState.breakerUntilTick, tick + cooldownTicks);
                    budgetState.consecutiveExceededTicks = 0;
                    logWarning(state, String.format(
                            "[ENCHADD-BUDGET] breaker-open key=%s usedNanos=%d budgetNanos=%d cooldownTicks=%d",
                            token.enchantKey(),
                            budgetState.usedNanos,
                            budgetNanos,
                            cooldownTicks
                    ));
                }
            }
        }

        currentExecution.remove();
    }

    private static void rollToTick(@NotNull EnchantExecutionBudgetManager.EnchantExecutionBudgetState.BudgetState state, long tick) {
        if (state.tick == tick) {
            return;
        }
        if (!state.tickExceeded) {
            state.consecutiveExceededTicks = 0;
        }
        state.tick = tick;
        state.usedNanos = 0L;
        state.tickExceeded = false;
    }

    @NotNull
    private static String normalizeKey(@NotNull String key) {
        String value = key.trim().toLowerCase(java.util.Locale.ROOT);
        return value.isEmpty() ? "unknown" : value;
    }

    private static void logWarning(@NotNull EnchantExecutionBudgetManager.EnchantExecutionBudgetState state,
                                   String message) {
        JavaPlugin currentPlugin = state.plugin;
        if (currentPlugin != null) {
            currentPlugin.getLogger().warning(message);
        }
    }
}
