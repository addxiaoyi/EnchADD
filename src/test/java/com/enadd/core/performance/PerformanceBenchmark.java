package com.enadd.core.performance;

import com.enadd.core.conflict.EnchantmentConflictManager;
import com.enadd.core.registry.EnchantmentRegistry;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PerformanceBenchmark {
    
    private static final int WARMUP_ITERATIONS = 100;
    private static final int BENCHMARK_ITERATIONS = 10000;
    private static final long MAX_ACCEPTABLE_TIME_MS = 1000;
    
    @BeforeAll
    static void setup() {
        EnchantmentConflictManager.getInstance().initialize();
    }
    
    @AfterAll
    static void teardown() {
        EnchantmentConflictManager.getInstance().shutdown();
    }
    
    @Test
    @DisplayName("附魔注册性能测试")
    void benchmarkEnchantmentRegistration() {
        System.out.println("\n=== 附魔注册性能测试 ===");
        
        long totalTime = 0;
        
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            
            EnchantmentRegistry.getAllEnchantments().get("critical_strike");
            
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }
        
        double avgTimeNs = (double) totalTime / BENCHMARK_ITERATIONS;
        double avgTimeMs = avgTimeNs / 1_000_000.0;
        
        System.out.println("Average query time: " + String.format("%.4f", avgTimeMs) + " ms");
        System.out.println("Total iterations: " + BENCHMARK_ITERATIONS);
        
        assertTrue(avgTimeMs < 1.0, 
            "Enchantment query should be fast (<1ms), actual: " + avgTimeMs + "ms");
    }
    
    @Test
    @DisplayName("Conflict detection performance test")
    void benchmarkConflictDetection() {
        System.out.println("\n=== Conflict detection performance test ===");
        
        String[] testPairs = {
            "critical_strike", "precision_strike",
            "vampirism", "life_drain",
            "bleeding", "hemorrhage",
            "weapon_flame_trail", "weapon_frost_trail",
            "stone_skin", "efficiency"
        };
        
        long totalTime = 0;
        
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            for (int j = 0; j < testPairs.length; j += 2) {
                long startTime = System.nanoTime();
                
                EnchantmentConflictManager.getInstance()
                    .areConflicting(testPairs[j], testPairs[j + 1]);
                
                long endTime = System.nanoTime();
                totalTime += (endTime - startTime);
            }
        }
        
        double avgTimeNs = (double) totalTime / (BENCHMARK_ITERATIONS * testPairs.length / 2);
        double avgTimeMs = avgTimeNs / 1_000_000.0;
        
        System.out.println("Average conflict detection time: " + String.format("%.4f", avgTimeMs) + " ms");
        System.out.println("Total detections: " + (BENCHMARK_ITERATIONS * testPairs.length / 2));
        
        assertTrue(avgTimeMs < 0.5, 
            "Conflict detection should be fast (<0.5ms), actual: " + avgTimeMs + "ms");
    }
    
    @Test
    @DisplayName("Bulk conflict query performance test")
    void benchmarkBulkConflictQueries() {
        System.out.println("\n=== Bulk conflict query performance test ===");
        
        String[] enchantments = {
            "critical_strike", "precision_strike", "execution",
            "vampirism", "life_drain", "leech",
            "bleeding", "hemorrhage", "wound",
            "weapon_flame_trail", "weapon_frost_trail", "weapon_lightning_trail"
        };
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            for (String enchant1 : enchantments) {
                for (String enchant2 : enchantments) {
                    if (!enchant1.equals(enchant2)) {
                        EnchantmentConflictManager.getInstance()
                            .areConflicting(enchant1, enchant2);
                    }
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("Bulk query total time: " + duration + " ms");
        int totalQueries = BENCHMARK_ITERATIONS * enchantments.length * (enchantments.length - 1);
        System.out.println("Total queries: " + totalQueries);
        
        assertTrue(duration < MAX_ACCEPTABLE_TIME_MS, 
            "Bulk query should complete within " + MAX_ACCEPTABLE_TIME_MS + "ms, actual: " + duration + "ms");
    }
    
    @Test
    @DisplayName("Cache performance test")
    void benchmarkCachePerformance() {
        System.out.println("\n=== Cache performance test ===");
        
        String enchant1 = "critical_strike";
        String enchant2 = "precision_strike";
        
        long warmTime = 0;
        
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            EnchantmentConflictManager.getInstance()
                .areConflicting(enchant1, enchant2);
        }
        
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            
            EnchantmentConflictManager.getInstance()
                .areConflicting(enchant1, enchant2);
            
            long endTime = System.nanoTime();
            warmTime += (endTime - startTime);
        }
        
        double avgWarmTimeNs = (double) warmTime / BENCHMARK_ITERATIONS;
        double avgWarmTimeMs = avgWarmTimeNs / 1_000_000.0;
        
        System.out.println("Cache hit average time: " + String.format("%.4f", avgWarmTimeMs) + " ms");
        System.out.println("Total queries: " + BENCHMARK_ITERATIONS);
        
        assertTrue(avgWarmTimeMs < 0.1, 
            "Cache query should be very fast (<0.1ms), actual: " + avgWarmTimeMs + "ms");
    }
    
    @Test
    @DisplayName("Memory usage test")
    void benchmarkMemoryUsage() {
        System.out.println("\n=== Memory usage test ===");
        
        Runtime runtime = Runtime.getRuntime();
        
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        Map<String, Set<String>> conflictRules = 
            EnchantmentConflictManager.getInstance().getConflictRules();
        
        runtime.gc();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        
        long memoryUsed = memoryAfter - memoryBefore;
        double memoryUsedMB = memoryUsed / (1024.0 * 1024.0);
        
        System.out.println("Conflict rules count: " + conflictRules.size());
        System.out.println("Memory usage: " + String.format("%.2f", memoryUsedMB) + " MB");
        
        assertTrue(memoryUsedMB < 10.0, 
            "Memory usage should be reasonable (<10MB), actual: " + memoryUsedMB + "MB");
    }
    
    @Test
    @DisplayName("Concurrent access performance test")
    void benchmarkConcurrentAccess() {
        System.out.println("\n=== Concurrent access performance test ===");
        
        int threadCount = 10;
        int operationsPerThread = 1000;
        
        long startTime = System.currentTimeMillis();
        
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    EnchantmentConflictManager.getInstance()
                        .areConflicting("critical_strike", "precision_strike");
                    EnchantmentRegistry.getAllEnchantments().get("vampirism");
                }
            });
            threads.add(thread);
            thread.start();
        }
        
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("Thread count: " + threadCount);
        System.out.println("Operations per thread: " + operationsPerThread);
        int totalOps = threadCount * operationsPerThread * 2;
        System.out.println("Total operations: " + totalOps);
        System.out.println("Total time: " + duration + " ms");
        System.out.println("Average time per operation: " + 
            String.format("%.4f", (double) duration / totalOps) + " ms");
        
        assertTrue(duration < MAX_ACCEPTABLE_TIME_MS, 
            "Concurrent operations should complete within " + MAX_ACCEPTABLE_TIME_MS + "ms, actual: " + duration + "ms");
    }
    
    @Test
    @DisplayName("Large scale data test")
    void benchmarkLargeScaleData() {
        System.out.println("\n=== Large scale data test ===");
        
        int dataSize = 1000;
        String[] testData = new String[dataSize];
        
        for (int i = 0; i < dataSize; i++) {
            testData[i] = "test_enchantment_" + i;
        }
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < dataSize; i++) {
            for (int j = i + 1; j < dataSize; j++) {
                EnchantmentConflictManager.getInstance()
                    .areConflicting(testData[i], testData[j]);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("Data size: " + dataSize);
        int totalQueries = dataSize * (dataSize - 1) / 2;
        System.out.println("Total queries: " + totalQueries);
        System.out.println("Total time: " + duration + " ms");
        
        assertTrue(duration < MAX_ACCEPTABLE_TIME_MS * 10, 
            "Large scale data query should complete in reasonable time, actual: " + duration + "ms");
    }
    
    @Test
    @DisplayName("Performance regression test")
    void benchmarkPerformanceRegression() {
        System.out.println("\n=== Performance regression test ===");
        
        Map<String, Long> baselineMetrics = new HashMap<>();
        baselineMetrics.put("conflict_check", 100L);
        baselineMetrics.put("enchantment_query", 50L);
        baselineMetrics.put("bulk_query", 500L);
        
        Map<String, Long> currentMetrics = new HashMap<>();
        
        long conflictCheckTime = measureConflictCheck();
        currentMetrics.put("conflict_check", conflictCheckTime);
        
        long enchantmentQueryTime = measureEnchantmentQuery();
        currentMetrics.put("enchantment_query", enchantmentQueryTime);
        
        long bulkQueryTime = measureBulkQuery();
        currentMetrics.put("bulk_query", bulkQueryTime);
        
        System.out.println("\nPerformance comparison:");
        for (Map.Entry<String, Long> entry : baselineMetrics.entrySet()) {
            String metric = entry.getKey();
            long baseline = entry.getValue();
            long current = currentMetrics.get(metric);
            double ratio = (double) current / baseline;
            
            System.out.println(metric + ":");
            System.out.println("  Baseline: " + baseline + " ms");
            System.out.println("  Current: " + current + " ms");
            System.out.println("  Ratio: " + String.format("%.2f", ratio));
            
            assertTrue(ratio < 2.0, 
                metric + " performance should not degrade significantly (ratio < 2.0), actual: " + ratio);
        }
    }
    
    private long measureConflictCheck() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            EnchantmentConflictManager.getInstance()
                .areConflicting("critical_strike", "precision_strike");
        }
        
        return System.currentTimeMillis() - startTime;
    }
    
    private long measureEnchantmentQuery() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            EnchantmentRegistry.getAllEnchantments().get("critical_strike");
        }
        
        return System.currentTimeMillis() - startTime;
    }
    
    private long measureBulkQuery() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            EnchantmentConflictManager.getInstance()
                .getConflicts("critical_strike");
        }
        
        return System.currentTimeMillis() - startTime;
    }
}
