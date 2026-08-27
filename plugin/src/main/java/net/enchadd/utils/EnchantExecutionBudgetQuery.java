package net.enchadd.utils;

import net.enchadd.EnchADDConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

final class EnchantExecutionBudgetQuery {

    private EnchantExecutionBudgetQuery() {
    }

    static double getCurrentChanceMultiplier(@NotNull EnchantExecutionBudgetManager.EnchantExecutionBudgetState state,
                                             @NotNull ThreadLocal<EnchantExecutionBudgetManager.ExecutionToken> currentExecution) {
        if (!isCurrentExecutionDegraded(currentExecution)) {
            return 1.0;
        }
        return clamp(EnchADDConfig.getEnchantBudgetDegradedChanceMultiplier(), 0.0, 1.0);
    }

    static int getCurrentTickModuloMultiplier(@NotNull EnchantExecutionBudgetManager.EnchantExecutionBudgetState state,
                                              @NotNull ThreadLocal<EnchantExecutionBudgetManager.ExecutionToken> currentExecution) {
        if (!isCurrentExecutionDegraded(currentExecution)) {
            return 1;
        }
        return Math.max(1, EnchADDConfig.getEnchantBudgetDegradedTickModuloMultiplier());
    }

    static long getDegradedExecutionCount(@NotNull EnchantExecutionBudgetManager.EnchantExecutionBudgetState state) {
        return state.degradedExecutions.get();
    }

    static long getSkippedExecutionCount(@NotNull EnchantExecutionBudgetManager.EnchantExecutionBudgetState state) {
        return state.skippedExecutions.get();
    }

    static int getOpenBreakerCount(@NotNull EnchantExecutionBudgetManager.EnchantExecutionBudgetState state) {
        long tick = state.tickCounter.get();
        int count = 0;
        for (EnchantExecutionBudgetManager.EnchantExecutionBudgetState.BudgetState budgetState : state.states.values()) {
            synchronized (budgetState) {
                if (tick < budgetState.breakerUntilTick) {
                    count++;
                }
            }
        }
        return count;
    }

    static long getTrackedEnchantCount(@NotNull EnchantExecutionBudgetManager.EnchantExecutionBudgetState state) {
        return state.states.size();
    }

    @NotNull
    static Map<String, Long> snapshotOverBudgetNanos(@NotNull EnchantExecutionBudgetManager.EnchantExecutionBudgetState state) {
        Map<String, Long> out = new java.util.HashMap<>();
        long budgetNanos = Math.max(1L, EnchADDConfig.getEnchantBudgetNanosPerTick());
        long tick = state.tickCounter.get();
        for (Map.Entry<String, EnchantExecutionBudgetManager.EnchantExecutionBudgetState.BudgetState> entry : state.states.entrySet()) {
            EnchantExecutionBudgetManager.EnchantExecutionBudgetState.BudgetState budgetState = entry.getValue();
            synchronized (budgetState) {
                if (budgetState.tick != tick) {
                    continue;
                }
                long over = budgetState.usedNanos - budgetNanos;
                if (over > 0L) {
                    out.put(entry.getKey(), over);
                }
            }
        }
        return out;
    }

    private static boolean isCurrentExecutionDegraded(@NotNull ThreadLocal<EnchantExecutionBudgetManager.ExecutionToken> currentExecution) {
        EnchantExecutionBudgetManager.ExecutionToken token = currentExecution.get();
        return token != null && token.degraded();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
