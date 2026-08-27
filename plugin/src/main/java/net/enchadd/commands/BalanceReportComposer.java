package net.enchadd.commands;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

final class BalanceReportComposer {

    private BalanceReportComposer() {
    }

    static List<String> compose(JavaPlugin plugin, double threshold, int simulatedRounds, double comboThreshold, int comboRounds) {
        BalanceScenarioAnalyzer.Result burst = BalanceScenarioAnalyzer.analyzeBurst(threshold, simulatedRounds);
        BalanceScenarioAnalyzer.Result combo = BalanceScenarioAnalyzer.analyzeCombo(comboThreshold, comboRounds);

        String message = BalanceReportLineComposer.burst(burst, threshold, simulatedRounds);
        String comboMessage = BalanceReportLineComposer.combo(combo, comboThreshold, comboRounds);

        List<String> messages = new ArrayList<>();
        plugin.getLogger().info(message);
        plugin.getLogger().info(comboMessage);
        messages.add(message);
        messages.add(comboMessage);
        return messages;
    }
}
