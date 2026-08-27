package net.enchadd.commands;

import java.util.List;

final class VerifySupport {

    private VerifySupport() {
    }

    static void passOrFail(boolean passed, String failureMessage, VerifyTally tally) {
        if (passed) {
            tally.passed++;
        } else {
            tally.failures.add(failureMessage);
        }
    }

    static final class VerifyTally {
        int conflictChecks;
        int cooldownChecks;
        int damageChecks;
        int boundaryChecks;
        int itemChecks;
        int passed;
        final List<String> failures;

        VerifyTally(List<String> failures) {
            this.failures = failures;
        }
    }
}
