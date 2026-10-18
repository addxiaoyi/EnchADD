package net.enchadd.mod.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExperienceRewardRulesTest {
    @Test
    void greedOnlyRewardsDirectPlayerKills() {
        assertEquals(19, ExperienceRewardRules.adjust(10, 3, 0, true, true));
        assertEquals(10, ExperienceRewardRules.adjust(10, 3, 0, true, false));
    }

    @Test
    void misfortuneAppliesToIndirectPlayerKills() {
        assertEquals(75, ExperienceRewardRules.adjust(100, 0, 1, true, false));
    }

    @Test
    void neitherCurseAppliesWhenTheDeathWasNotCausedByThePlayer() {
        assertEquals(100, ExperienceRewardRules.adjust(100, 3, 3, false, false));
    }

    @Test
    void combinesGreedAndMisfortuneWithOneFinalRoundingStep() {
        assertEquals(48, ExperienceRewardRules.adjust(100, 3, 3, true, true));
    }

    @Test
    void clampsLevelsAndLargeExperienceRewards() {
        assertEquals(19, ExperienceRewardRules.adjust(10, 30, 0, true, true));
        assertEquals(Integer.MAX_VALUE,
                ExperienceRewardRules.adjust(Integer.MAX_VALUE, 3, 0, true, true));
    }

    @Test
    void leavesNonPositiveExperienceUnchanged() {
        assertEquals(0, ExperienceRewardRules.adjust(0, 3, 0, true, true));
        assertEquals(-2, ExperienceRewardRules.adjust(-2, 3, 0, true, true));
    }

    @Test
    void greedVulnerabilityIsBoundedByLevel() {
        assertEquals(1.0, ExperienceRewardRules.vulnerabilityMultiplier(0));
        assertEquals(1.75, ExperienceRewardRules.vulnerabilityMultiplier(3));
        assertEquals(360L, ExperienceRewardRules.vulnerabilityDurationTicks(30));
    }
}
