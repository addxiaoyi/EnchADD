package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CiStatusFormatterTest {

    @Test
    void formatIncludesStablePrefixCoreFieldsAndSanitizedMetricValues() {
        String line = CiStatusFormatter.format(snapshot());

        assertTrue(line.startsWith("[ENCHADD-CI] "));
        assertTrue(line.contains("status=OK"));
        assertTrue(line.contains("enchants=42"));
        assertTrue(line.contains("hasAirbag=true"));
        assertTrue(line.contains("version=1.2.3"));
        assertTrue(line.contains("activeLang=zh_cn"));
        assertTrue(line.contains("airbagName=Air_Bag"));
        assertTrue(line.contains("braceName=Brace-Name"));
        assertTrue(line.contains("safetyModeState=manual_on"));
        assertTrue(line.contains("safetyModeReason=command_on"));
        assertTrue(line.contains("legacySanitizedKeys=legacy_one,legacy_two"));
        assertTrue(line.contains("budgetChanceMultiplier=0.50"));
        assertTrue(line.contains("particleDropRate=0.2500"));
        assertTrue(line.contains("uptimeSeconds=123"));
    }

    private static CiStatusSnapshot snapshot() {
        return new CiStatusSnapshot(
                "OK",
                42,
                true,
                "1.2.3",
                "zh cn",
                true,
                "Air Bag",
                "Brace=Name",
                "41",
                "42",
                1000,
                60,
                5,
                20,
                2,
                0.25,
                7,
                30,
                4,
                0.125,
                0.3333,
                0.5,
                3,
                9,
                200,
                1,
                2,
                3,
                4,
                5,
                1.5,
                20.0,
                19.9,
                19.8,
                "manual on",
                true,
                true,
                false,
                6,
                7,
                0.75,
                2,
                true,
                123456789L,
                "command on",
                8,
                9,
                10,
                11,
                120000L,
                0.5,
                3,
                true,
                12,
                "legacy one,legacy two",
                64,
                128,
                256,
                123
        );
    }
}
