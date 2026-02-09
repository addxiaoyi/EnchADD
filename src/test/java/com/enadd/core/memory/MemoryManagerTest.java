package com.enadd.core.memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MemoryManagerTest {

    @Test
    public void testGetUsedMemory() {
        MemoryManager manager = MemoryManager.getInstance();
        assertTrue(manager.getUsedMemory() >= 0);
    }

    @Test
    public void testGetMaxMemory() {
        MemoryManager manager = MemoryManager.getInstance();
        assertTrue(manager.getMaxMemory() > 0);
    }

    @Test
    public void testGetMemoryUsagePercent() {
        MemoryManager manager = MemoryManager.getInstance();
        float usage = manager.getMemoryUsagePercent();
        assertTrue(usage >= 0 && usage <= 100);
    }

    @Test
    public void testTrackAllocation() {
        MemoryManager manager = MemoryManager.getInstance();
        manager.trackAllocation("TEST", 1024);
        MemoryManager.MemoryStats stats = manager.getStats();
        assertTrue(stats.getTotalAllocated() >= 1024);
    }

    @Test
    public void testGetStats() {
        MemoryManager manager = MemoryManager.getInstance();
        MemoryManager.MemoryStats stats = manager.getStats();
        assertNotNull(stats);
        assertTrue(stats.getUsedMemory() >= 0);
        assertTrue(stats.getMaxMemory() > 0);
    }
}
