package com.enadd.core.safe;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 安全的性能监控器
 * 
 * 特点：
 * - 低频采样（1%）- 最小性能影响
 * - 聚合统计 - 不保存原始数据
 * - 异步处理 - 不阻塞主线程
 * - 自动清理 - 防止内存泄露
 * 
 * 风险等级: LOW
 */
public final class SafePerformanceMonitor {
    
    private static final Logger LOGGER = Logger.getLogger(SafePerformanceMonitor.class.getName());
    
    private static SafePerformanceMonitor instance;
    
    // 安全配置
    private static final double SAMPLING_RATE = 0.01; // 1%采样
    private static final int AGGREGATION_INTERVAL_SECONDS = 60;
    private static final int MAX_HISTORY_MINUTES = 60;
    
    private final MemoryMXBean memoryBean;
    private final ScheduledExecutorService scheduler;
    
    // 聚合数据（不保存原始数据）
    private final AtomicLong totalSamples = new AtomicLong(0);
    private final AtomicLong tpsSum = new AtomicLong(0);
    private final AtomicLong memorySum = new AtomicLong(0);
    
    // 历史数据（只保留聚合结果）
    private final ConcurrentLinkedQueue<AggregatedData> history = new ConcurrentLinkedQueue<>();
    
    @SuppressWarnings("unused")
    private volatile long lastAggregationTime = System.currentTimeMillis();
    
    private SafePerformanceMonitor() {
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SafePerformanceMonitor");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY); // 最低优先级
            return t;
        });
        
        // 每秒采样一次（但只有1%的概率真正采样）
        scheduler.scheduleAtFixedRate(this::sample, 1, 1, TimeUnit.SECONDS);
        
        // 每分钟聚合一次
        scheduler.scheduleAtFixedRate(this::aggregate, 
            AGGREGATION_INTERVAL_SECONDS, 
            AGGREGATION_INTERVAL_SECONDS, 
            TimeUnit.SECONDS);
        
        LOGGER.info("SafePerformanceMonitor initialized with " + (SAMPLING_RATE * 100) + "% sampling rate");
    }
    
    public static synchronized void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new SafePerformanceMonitor();
        }
    }
    
    public static SafePerformanceMonitor getInstance() {
        return instance;
    }
    
    /**
     * 采样（低频）
     */
    private void sample() {
        // 只有1%的概率采样
        if (Math.random() > SAMPLING_RATE) {
            return;
        }
        
        try {
            // 异步采样，不阻塞
            CompletableFuture.runAsync(() -> {
                double tps = Bukkit.getTPS()[0]; // 1分钟平均TPS
                long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
                
                totalSamples.incrementAndGet();
                tpsSum.addAndGet((long) (tps * 100)); // 保留2位小数
                memorySum.addAndGet(usedMemory);
            });
        } catch (Exception e) {
            // 静默失败，不影响服务器
        }
    }
    
    /**
     * 聚合数据
     */
    private void aggregate() {
        long samples = totalSamples.getAndSet(0);
        if (samples == 0) {
            return; // 没有数据
        }
        
        long tpsTotal = tpsSum.getAndSet(0);
        long memoryTotal = memorySum.getAndSet(0);
        
        double avgTps = (tpsTotal / 100.0) / samples;
        long avgMemory = memoryTotal / samples;
        
        AggregatedData data = new AggregatedData(
            System.currentTimeMillis(),
            avgTps,
            avgMemory,
            samples
        );
        
        history.offer(data);
        
        // 清理旧数据
        long cutoffTime = System.currentTimeMillis() - (MAX_HISTORY_MINUTES * 60 * 1000L);
        while (!history.isEmpty() && history.peek().timestamp < cutoffTime) {
            history.poll();
        }
        
        lastAggregationTime = System.currentTimeMillis();
    }
    
    /**
     * 获取摘要
     */
    public Summary getSummary() {
        if (history.isEmpty()) {
            return new Summary(0, 0, 0, 0);
        }
        
        double totalTps = 0;
        long totalMemory = 0;
        long totalSamples = 0;
        
        for (AggregatedData data : history) {
            totalTps += data.avgTps;
            totalMemory += data.avgMemory;
            totalSamples += data.sampleCount;
        }
        
        int dataPoints = history.size();
        return new Summary(
            totalTps / dataPoints,
            totalMemory / dataPoints,
            totalSamples,
            dataPoints
        );
    }
    
    /**
     * 关闭
     */
    public void shutdown() {
        LOGGER.info("Shutting down SafePerformanceMonitor...");
        
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        history.clear();
        LOGGER.info("SafePerformanceMonitor shutdown complete");
    }
    
    /**
     * 聚合数据
     */
    private static class AggregatedData {
        final long timestamp;
        final double avgTps;
        final long avgMemory;
        final long sampleCount;
        
        AggregatedData(long timestamp, double avgTps, long avgMemory, long sampleCount) {
            this.timestamp = timestamp;
            this.avgTps = avgTps;
            this.avgMemory = avgMemory;
            this.sampleCount = sampleCount;
        }
    }
    
    /**
     * 摘要信息
     */
    public static class Summary {
        public final double avgTps;
        public final long avgMemoryBytes;
        public final long totalSamples;
        public final int dataPoints;
        
        Summary(double avgTps, long avgMemoryBytes, long totalSamples, int dataPoints) {
            this.avgTps = avgTps;
            this.avgMemoryBytes = avgMemoryBytes;
            this.totalSamples = totalSamples;
            this.dataPoints = dataPoints;
        }
        
        public double getAvgMemoryMB() {
            return avgMemoryBytes / (1024.0 * 1024.0);
        }
        
        @Override
        public String toString() {
            return String.format("Summary{TPS=%.2f, Memory=%.2fMB, Samples=%d, DataPoints=%d}",
                avgTps, getAvgMemoryMB(), totalSamples, dataPoints);
        }
    }
}
