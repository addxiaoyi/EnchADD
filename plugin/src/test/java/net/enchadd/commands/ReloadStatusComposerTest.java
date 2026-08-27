package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReloadStatusComposerTest {

    private static final Pattern SUCCESS_PATTERN = Pattern.compile(
            "^\\[ENCHADD-RELOAD] status=OK lang=\\S+ safetyEnabled=(true|false) budgetNanos=\\d+ budgetChance=\\d+\\.\\d{2} budgetTickModulo=\\d+$"
    );

    @Test
    void successKeepsStableMachineReadableFormat() {
        String status = ReloadStatusComposer.success();

        assertTrue(SUCCESS_PATTERN.matcher(status).matches(), status);
    }
}
