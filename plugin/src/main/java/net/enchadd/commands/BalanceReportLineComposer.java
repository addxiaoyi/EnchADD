package net.enchadd.commands;

import java.util.Locale;

final class BalanceReportLineComposer {

    private BalanceReportLineComposer() {
    }

    static String burst(BalanceScenarioAnalyzer.Result result, double threshold, int simulatedRounds) {
        return String.format(
                Locale.ROOT,
                "[ENCHADD-BALANCE] status=%s pairs=%d blocked=%d scored=%d violations=%d threshold=%.2f highestScore=%.2f rounds=%d estimatedBurstDamage=%.2f review=%s",
                result.status(),
                result.pairCount(),
                result.blockedPairs(),
                result.scoredPairs(),
                result.violations(),
                threshold,
                result.highestScore(),
                simulatedRounds,
                result.estimatedDamage(),
                result.reviewLabel()
        );
    }

    static String combo(BalanceScenarioAnalyzer.Result result, double threshold, int rounds) {
        return String.format(
                Locale.ROOT,
                "[ENCHADD-COMBO-GATE] status=%s scenarios=%d blocked=%d scored=%d violations=%d threshold=%.2f rounds=%d highestTeamScore=%.2f estimatedTeamBurstDamage=%.2f review=%s",
                result.status(),
                result.scenarioCount(),
                result.blockedPairs(),
                result.scoredPairs(),
                result.violations(),
                threshold,
                rounds,
                result.highestScore(),
                result.estimatedDamage(),
                result.reviewLabel()
        );
    }
}
