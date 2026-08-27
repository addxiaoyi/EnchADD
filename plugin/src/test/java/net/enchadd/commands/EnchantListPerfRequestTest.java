package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnchantListPerfRequestTest {

    @Test
    void fromUsesDefaultsWhenCountsAreMissingOrInvalid() {
        EnchantListPerfRequest missing = EnchantListPerfRequest.from(new String[] {"perf"});
        EnchantListPerfRequest invalid = EnchantListPerfRequest.from(new String[] {"perf", "bad", "nope"});

        assertEquals(1200, missing.triggerCount());
        assertEquals(600, missing.particleCount());
        assertEquals(1200, invalid.triggerCount());
        assertEquals(600, invalid.particleCount());
    }

    @Test
    void fromClampsConfiguredCounts() {
        EnchantListPerfRequest low = EnchantListPerfRequest.from(new String[] {"perf", "-5", "-1"});
        EnchantListPerfRequest high = EnchantListPerfRequest.from(new String[] {"perf", "999999", "999999"});

        assertEquals(1, low.triggerCount());
        assertEquals(0, low.particleCount());
        assertEquals(200_000, high.triggerCount());
        assertEquals(100_000, high.particleCount());
    }
}
