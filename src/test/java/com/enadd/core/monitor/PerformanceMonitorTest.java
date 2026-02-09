package com.enadd.core.monitor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PerformanceMonitorTest {

    @Test
    public void testGetAvailableProcessors() {
        com.enadd.core.monitor.PerformanceMonitor monitor = com.enadd.core.monitor.PerformanceMonitor.getInstance();
        assertTrue(monitor.getAvailableProcessors() > 0);
    }

    @Test
    public void testGetActiveThreadCount() {
        com.enadd.core.monitor.PerformanceMonitor monitor = com.enadd.core.monitor.PerformanceMonitor.getInstance();
        assertTrue(monitor.getActiveThreadCount() > 0);
    }

    @Test
    public void testRecordFrame() {
        com.enadd.core.monitor.PerformanceMonitor monitor = com.enadd.core.monitor.PerformanceMonitor.getInstance();
        monitor.recordFrame();
        com.enadd.core.monitor.PerformanceMonitor.PerformanceStats stats = monitor.getStats();
        assertTrue(stats.getFrameCount() > 0);
    }

    @Test
    public void testGetStats() {
        com.enadd.core.monitor.PerformanceMonitor monitor = com.enadd.core.monitor.PerformanceMonitor.getInstance();
        com.enadd.core.monitor.PerformanceMonitor.PerformanceStats stats = monitor.getStats();
        assertNotNull(stats);
        assertTrue(stats.getProcessors() > 0);
    }

    @Test
    public void testIsUnderLoad() {
        com.enadd.core.monitor.PerformanceMonitor monitor = com.enadd.core.monitor.PerformanceMonitor.getInstance();
        assertFalse(monitor.isCriticalLoad());
    }

    @Test
    public void testResetStats() {
        com.enadd.core.monitor.PerformanceMonitor monitor = com.enadd.core.monitor.PerformanceMonitor.getInstance();
        monitor.recordFrame();
        monitor.resetStats();
        com.enadd.core.monitor.PerformanceMonitor.PerformanceStats stats = monitor.getStats();
        assertEquals(0, stats.getFrameCount());
    }
}
