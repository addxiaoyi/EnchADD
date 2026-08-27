package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BalanceReportLineComposerTest {

    @Test
    void burstLineUsesStableMachineReadableFormat() {
        BalanceScenarioAnalyzer.Result result = new BalanceScenarioAnalyzer.Result(4, 1, 3, 2, 12.345, 246.8, "FAIL", 0, "Alpha Guard + Beta Strike");

        String line = BalanceReportLineComposer.burst(result, 10.0, 20);

        assertEquals("[ENCHADD-BALANCE] status=FAIL pairs=4 blocked=1 scored=3 violations=2 threshold=10.00 highestScore=12.35 rounds=20 estimatedBurstDamage=246.80 review=Alpha Guard + Beta Strike", line);
    }

    @Test
    void comboLineUsesStableMachineReadableFormat() {
        BalanceScenarioAnalyzer.Result result = new BalanceScenarioAnalyzer.Result(0, 2, 5, 1, 8.123, 32.5, "OK", 7, "Alpha Guard + Beta Strike vs Gamma Ward + Delta");

        String line = BalanceReportLineComposer.combo(result, 9.5, 4);

        assertEquals("[ENCHADD-COMBO-GATE] status=OK scenarios=7 blocked=2 scored=5 violations=1 threshold=9.50 rounds=4 highestTeamScore=8.12 estimatedTeamBurstDamage=32.50 review=Alpha Guard + Beta Strike vs Gamma Ward + Delta", line);
    }
}
