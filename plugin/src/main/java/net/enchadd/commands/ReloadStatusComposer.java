package net.enchadd.commands;

import net.enchadd.EnchADDConfig;

import java.util.Locale;

final class ReloadStatusComposer {

    private ReloadStatusComposer() {
    }

    static String success() {
        return String.format(
                Locale.ROOT,
                "[ENCHADD-RELOAD] status=OK lang=%s safetyEnabled=%s budgetNanos=%d budgetChance=%.2f budgetTickModulo=%d",
                EnchADDConfig.getLanguage(),
                EnchADDConfig.isSafetyModeEnabled(),
                EnchADDConfig.getEnchantBudgetNanosPerTick(),
                EnchADDConfig.getEnchantBudgetDegradedChanceMultiplier(),
                EnchADDConfig.getEnchantBudgetDegradedTickModuloMultiplier()
        );
    }
}
