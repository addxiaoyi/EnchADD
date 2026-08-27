package net.enchadd.commands;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

final class BalanceService {

    private BalanceService() {
    }

    static List<String> run(JavaPlugin plugin, double threshold, int simulatedRounds, double comboThreshold, int comboRounds) {
        return BalanceReportComposer.compose(plugin, threshold, simulatedRounds, comboThreshold, comboRounds);
    }
}
