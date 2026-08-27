package net.enchadd.commands;

import java.util.Locale;

final class VerifyMessageBuilder {

    private VerifyMessageBuilder() {
    }

    static String build(VerifySupport.VerifyTally tally) {
        int totalChecks = tally.conflictChecks + tally.cooldownChecks + tally.damageChecks + tally.boundaryChecks + tally.itemChecks;
        int failed = totalChecks - tally.passed;
        String status = failed == 0 ? "OK" : "FAIL";
        String firstFailure = tally.failures.isEmpty() ? "none" : tally.failures.get(0).replaceAll("\\s+", "_").replace('=', '-');
        return String.format(
                Locale.ROOT,
                "[ENCHADD-VERIFY] status=%s total=%d passed=%d failed=%d conflictChecks=%d cooldownChecks=%d damageChecks=%d boundaryChecks=%d itemChecks=%d firstFailure=%s",
                status,
                totalChecks,
                tally.passed,
                failed,
                tally.conflictChecks,
                tally.cooldownChecks,
                tally.damageChecks,
                tally.boundaryChecks,
                tally.itemChecks,
                firstFailure
        );
    }
}
