package net.enchadd.utils;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PerformanceShieldSupportTest {

    @Test
    void shieldBlockChecksRejectNullInputs() {
        assertFalse(PerformanceShieldSupport.isSuccessfulShieldBlock(null, null));
    }

    @Test
    void likelyFacingBlockRejectsNullInputs() {
        assertFalse(PerformanceShieldSupport.isLikelyShieldFacingBlock(null, new Vector(1, 0, 0)));
        assertFalse(PerformanceShieldSupport.isLikelyShieldFacingBlock(null, null));
    }
}
