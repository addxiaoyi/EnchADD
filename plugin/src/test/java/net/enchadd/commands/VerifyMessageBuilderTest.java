package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VerifyMessageBuilderTest {

    @Test
    void buildReportsOkWhenAllChecksPass() {
        VerifySupport.VerifyTally tally = new VerifySupport.VerifyTally(new ArrayList<>());
        tally.conflictChecks = 2;
        tally.cooldownChecks = 1;
        tally.damageChecks = 3;
        tally.boundaryChecks = 4;
        tally.itemChecks = 0;
        tally.passed = 10;

        String message = VerifyMessageBuilder.build(tally);

        assertEquals("[ENCHADD-VERIFY] status=OK total=10 passed=10 failed=0 conflictChecks=2 cooldownChecks=1 damageChecks=3 boundaryChecks=4 itemChecks=0 firstFailure=none", message);
    }

    @Test
    void buildReportsFailureAndSanitizesFirstFailure() {
        VerifySupport.VerifyTally tally = new VerifySupport.VerifyTally(new ArrayList<>());
        tally.conflictChecks = 1;
        tally.cooldownChecks = 1;
        tally.damageChecks = 1;
        tally.boundaryChecks = 1;
        tally.itemChecks = 0;
        tally.passed = 2;
        tally.failures.add("bad value = with spaces");

        String message = VerifyMessageBuilder.build(tally);

        assertEquals("[ENCHADD-VERIFY] status=FAIL total=4 passed=2 failed=2 conflictChecks=1 cooldownChecks=1 damageChecks=1 boundaryChecks=1 itemChecks=0 firstFailure=bad_value_-_with_spaces", message);
    }
}
