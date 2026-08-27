package net.enchadd.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SafetyModePolicySupportTest {

    @Test
    void sanitizeReasonAndStateLabelStayStable() {
        assertEquals("unknown", SafetyModePolicySupport.sanitizeReason(null));
        assertEquals("alpha_beta", SafetyModePolicySupport.sanitizeReason("alpha beta"));
        assertEquals("DISABLED", SafetyModePolicySupport.buildStateLabel(false, true, true));
        assertEquals("MANUAL+AUTO", SafetyModePolicySupport.buildStateLabel(true, true, true));
    }

    @Test
    void clampBoundsValues() {
        assertEquals(0.0, SafetyModePolicySupport.clamp(-1.0, 0.0, 1.0), 1.0e-9);
        assertEquals(1.0, SafetyModePolicySupport.clamp(2.0, 0.0, 1.0), 1.0e-9);
        assertEquals(0.5, SafetyModePolicySupport.clamp(0.5, 0.0, 1.0), 1.0e-9);
    }
}
