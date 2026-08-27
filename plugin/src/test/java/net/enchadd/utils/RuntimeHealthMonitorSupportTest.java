package net.enchadd.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuntimeHealthMonitorSupportTest {

    @Test
    void buildAlertReasonKeepsStableCauseOrder() {
        assertEquals("none", RuntimeHealthAlertPolicy.buildAlertReason(false, false, false, false, false));
        assertEquals("low_tps+error_rate+trigger_spike+particle_drop+stats_pending",
                RuntimeHealthAlertPolicy.buildAlertReason(true, true, true, true, true));
        assertEquals("error_rate+stats_pending",
                RuntimeHealthAlertPolicy.buildAlertReason(false, true, false, false, true));
    }

    @Test
    void sanitizeTpsClampsInvalidAndOutOfRangeValues() {
        assertEquals(20.0, RuntimeHealthSample.sanitizeTps(null, 0), 1.0e-9);
        assertEquals(20.0, RuntimeHealthSample.sanitizeTps(new double[] {Double.NaN}, 0), 1.0e-9);
        assertEquals(20.0, RuntimeHealthSample.sanitizeTps(new double[] {25.0}, 0), 1.0e-9);
        assertEquals(0.0, RuntimeHealthSample.sanitizeTps(new double[] {-3.0}, 0), 1.0e-9);
    }
}
