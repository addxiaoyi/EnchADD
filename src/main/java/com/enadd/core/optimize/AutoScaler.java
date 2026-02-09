package com.enadd.core.optimize;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.enadd.core.optimize.ServerAnalyzer.ServerProfile;
import com.enadd.core.optimize.ServerAnalyzer.HardwareInfo;
import com.enadd.core.optimize.ServerAnalyzer.ServerRole;
import com.enadd.core.optimize.PerformanceMonitor.PerformanceSummary;


public final class AutoScaler {
    private static final Logger LOGGER = Logger.getLogger(AutoScaler.class.getName());

    private static final AtomicBoolean ENABLED = new AtomicBoolean(false);
    private static final AtomicReference<ServerProfile> PROFILE = new AtomicReference<>();
    private static final AtomicReference<PerformanceSummary> LAST_SUMMARY = new AtomicReference<>();

    private static ScheduledExecutorService scheduler;
    private static ExecutorService asyncExecutor;
    private static volatile int targetThreadCount;
    private static volatile int targetTickInterval;
    private static volatile long targetMemoryThreshold;

    private static final int MIN_THREADS = 2;
    private static final int MAX_THREADS = 32;
    private static final int MIN_TICK_INTERVAL = 20;
    private static final int MAX_TICK_INTERVAL = 50;
    private static final long MIN_MEMORY_THRESHOLD_MB = 256L;
    private static final long MAX_MEMORY_THRESHOLD_MB = 4096L;

    private static final long SCALING_INTERVAL_MS = 30000L;

    private AutoScaler() {}

    public static synchronized void initialize() {
        if (ENABLED.get()) {
            return;
        }

        ServerProfile profile = ServerAnalyzer.analyzeServer();
        PROFILE.set(profile);

        initializeResources(profile);

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AutoScaler-Scheduler");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(AutoScaler::scalingDecision,
                                     SCALING_INTERVAL_MS,
                                     SCALING_INTERVAL_MS,
                                     TimeUnit.MILLISECONDS);

        ENABLED.set(true);
        LOGGER.info("AutoScaler initialized with profile: " + profile.hardware.cpuCores + " cores, " +
                    profile.hardware.totalMemoryMB + "MB RAM");
    }

    private static void initializeResources(ServerProfile profile) {
        HardwareInfo hardware = profile.hardware;
        int optimalThreads = calculateOptimalThreadCount(hardware);
        targetThreadCount = optimalThreads;

        int optimalTickInterval = calculateOptimalTickInterval(profile);
        targetTickInterval = optimalTickInterval;

        long optimalMemoryThreshold = calculateOptimalMemoryThreshold(hardware);
        targetMemoryThreshold = optimalMemoryThreshold;

        asyncExecutor = new ThreadPoolExecutor(
            optimalThreads,
            optimalThreads,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "AutoScaler-Worker-" + counter++);
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    private static int calculateOptimalThreadCount(HardwareInfo hardware) {
        int cores = (int) hardware.cpuCores;
        int threads = Math.max(MIN_THREADS, Math.min(MAX_THREADS, cores * 2));

        if (hardware.cpuUsagePercent > 70) {
            threads = Math.max(MIN_THREADS, threads - 2);
        } else if (hardware.cpuUsagePercent < 30) {
            threads = Math.min(MAX_THREADS, threads + 2);
        }

        return threads;
    }

    private static int calculateOptimalTickInterval(ServerProfile profile) {
        int interval;

        switch (profile.role) {
            case STANDALONE:
                interval = 30;
                break;
            case PROXY:
                interval = 25;
                break;
            case NODE:
                interval = 35;
                break;
            case MASTER:
                interval = 20;
                break;
            case DISTRIBUTED:
                interval = 28;
                break;
            default:
                interval = 30;
        }

        HardwareInfo hardware = profile.hardware;
        if (hardware.cpuUsagePercent > 80) {
            interval = Math.min(MAX_TICK_INTERVAL, interval + 5);
        } else if (hardware.cpuUsagePercent < 40) {
            interval = Math.max(MIN_TICK_INTERVAL, interval - 2);
        }

        return interval;
    }

    private static long calculateOptimalMemoryThreshold(HardwareInfo hardware) {
        long totalMemory = hardware.totalMemoryMB;
        long threshold = totalMemory / 4;
        threshold = Math.max(MIN_MEMORY_THRESHOLD_MB, Math.min(MAX_MEMORY_THRESHOLD_MB, threshold));
        return threshold;
    }

    private static void scalingDecision() {
        if (!ENABLED.get()) return;

        PerformanceMonitor.recordCustomMetric("autoscaler_evaluation", 1.0);

        PerformanceSummary current = PerformanceMonitor.getSummary();
        LAST_SUMMARY.set(current);
        ServerProfile profile = PROFILE.get();

        if (profile == null) {
            profile = ServerAnalyzer.analyzeServer();
            PROFILE.set(profile);
        }

        boolean needsRescaling = evaluateScalingNeeds(current, profile);

        if (needsRescaling) {
            applyScaling(profile);
        }
    }

    private static boolean evaluateScalingNeeds(PerformanceSummary summary, ServerProfile profile) {
        if (summary == null) return false;

        double cpuUsage = summary.getAverageCpuUsage();
        double memoryUsage = summary.getAverageMemoryUsage();

        int currentCpuThreshold = profile.role == ServerRole.STANDALONE ? 70 : 80;
        int currentMemoryThreshold = profile.role == ServerRole.STANDALONE ? 75 : 85;

        return cpuUsage > currentCpuThreshold || memoryUsage > currentMemoryThreshold;
    }

    private static void applyScaling(@SuppressWarnings("unused") ServerProfile profile) {
        PerformanceSummary summary = LAST_SUMMARY.get();
        if (summary == null) return;

        int newThreadCount = targetThreadCount;
        int newTickInterval = targetTickInterval;

        if (summary.getAverageCpuUsage() > 75) {
            newThreadCount = Math.max(MIN_THREADS, targetThreadCount - 2);
            newTickInterval = Math.min(MAX_TICK_INTERVAL, targetTickInterval + 3);
        } else if (summary.getAverageCpuUsage() < 40) {
            newThreadCount = Math.min(MAX_THREADS, targetThreadCount + 1);
            newTickInterval = Math.max(MIN_TICK_INTERVAL, targetTickInterval - 1);
        }

        if (newThreadCount != targetThreadCount) {
            adjustThreadPool(newThreadCount);
            LOGGER.log(Level.INFO, "Thread pool scaled from {0} to {1}", new Object[]{targetThreadCount, newThreadCount});
        }

        if (newTickInterval != targetTickInterval) {
            int oldInterval = targetTickInterval;
            targetTickInterval = newTickInterval;
            LOGGER.log(Level.INFO, "Tick interval adjusted from {0}ms to {1}ms", new Object[]{oldInterval, newTickInterval});
        }
    }

    private static void adjustThreadPool(int newSize) {
        targetThreadCount = newSize;

        if (asyncExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) asyncExecutor;

            executor.setCorePoolSize(newSize);
            executor.setMaximumPoolSize(newSize);

            PerformanceMonitor.recordCustomMetric("thread_pool_resized", newSize);
        }
    }

    public static void submitTask(Runnable task) {
        if (!ENABLED.get()) {
            task.run();
            return;
        }

        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.submit(task);
        } else {
            task.run();
        }
    }

    public static <T> Future<T> submitTaskReturningFuture(Callable<T> task) {
        if (!ENABLED.get()) {
            try {
                CompletableFuture<T> future = new CompletableFuture<>();
                future.complete(task.call());
                return future;
            } catch (Exception e) {
                CompletableFuture<T> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
        }

        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            return asyncExecutor.submit(task);
        }

        try {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.complete(task.call());
            return future;
        } catch (Exception e) {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    public static void setTargetThreadCount(int count) {
        targetThreadCount = Math.max(MIN_THREADS, Math.min(MAX_THREADS, count));
        if (ENABLED.get()) {
            adjustThreadPool(targetThreadCount);
        }
    }

    public static void setTargetTickInterval(int interval) {
        targetTickInterval = Math.max(MIN_TICK_INTERVAL, Math.min(MAX_TICK_INTERVAL, interval));
    }

    public static void setTargetMemoryThreshold(long thresholdMB) {
        targetMemoryThreshold = Math.max(MIN_MEMORY_THRESHOLD_MB, Math.min(MAX_MEMORY_THRESHOLD_MB, thresholdMB));
    }

    public static ScalingReport getScalingReport() {
        ServerProfile profile = PROFILE.get();
        PerformanceSummary summary = LAST_SUMMARY.get();

        int activeThreads = 0;
        int queuedTasks = 0;
        if (asyncExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) asyncExecutor;
            activeThreads = executor.getActiveCount();
            queuedTasks = executor.getQueue().size();
        }

        return new ScalingReport(
            profile,
            summary,
            targetThreadCount,
            targetTickInterval,
            targetMemoryThreshold,
            activeThreads,
            queuedTasks,
            ENABLED.get()
        );
    }

    public static int getCurrentThreadCount() {
        return targetThreadCount;
    }

    public static int getCurrentTickInterval() {
        return targetTickInterval;
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    public static synchronized void shutdown() {
        if (!ENABLED.get()) return;

        ENABLED.set(false);

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        LOGGER.info("AutoScaler shutdown complete");
    }

    public static final class ScalingReport {
        private final ServerProfile profile;
        private final PerformanceSummary summary;
        private final int targetThreadCount;
        private final int targetTickInterval;
        private final long targetMemoryThreshold;
        private final int activeThreads;
        private final int queuedTasks;
        private final boolean enabled;

        public ScalingReport(ServerProfile profile, PerformanceSummary summary,
                           int targetThreadCount, int targetTickInterval,
                           long targetMemoryThreshold, int activeThreads,
                           int queuedTasks, boolean enabled) {
            this.profile = profile;
            this.summary = summary;
            this.targetThreadCount = targetThreadCount;
            this.targetTickInterval = targetTickInterval;
            this.targetMemoryThreshold = targetMemoryThreshold;
            this.activeThreads = activeThreads;
            this.queuedTasks = queuedTasks;
            this.enabled = enabled;
        }

        public ServerProfile getProfile() { return profile; }
        public PerformanceSummary getSummary() { return summary; }
        public int getTargetThreadCount() { return targetThreadCount; }
        public int getTargetTickInterval() { return targetTickInterval; }
        public long getTargetMemoryThreshold() { return targetMemoryThreshold; }
        public int getActiveThreads() { return activeThreads; }
        public int getQueuedTasks() { return queuedTasks; }
        public boolean isEnabled() { return enabled; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== AutoScaler Report ===\n");
            sb.append("Enabled: ").append(enabled).append("\n");
            sb.append("Target Threads: ").append(targetThreadCount).append("\n");
            sb.append("Active Threads: ").append(activeThreads).append("\n");
            sb.append("Queued Tasks: ").append(queuedTasks).append("\n");
            sb.append("Tick Interval: ").append(targetTickInterval).append("ms\n");
            sb.append("Memory Threshold: ").append(targetMemoryThreshold).append("MB\n");

            if (summary != null) {
                sb.append("Current CPU: ").append(String.format("%.1f%%", summary.getAverageCpuUsage())).append("\n");
                sb.append("Current Memory: ").append(String.format("%.1f%%", summary.getAverageMemoryUsage())).append("\n");
            }

            if (profile != null) {
                sb.append("Server Role: ").append(profile.role).append("\n");
                sb.append("Estimated Capacity: ").append(profile.playerCapacity).append(" players\n");
            }

            return sb.toString();
        }
    }
}
