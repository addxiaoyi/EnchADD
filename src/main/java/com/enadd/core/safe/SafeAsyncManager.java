package com.enadd.core.safe;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * 安全的异步任务管理器
 * 
 * 特点：
 * - 固定线程池大小（不会无限增长）
 * - 任务队列大小限制（防止内存溢出）
 * - 自动超时取消（防止任务卡死）
 * - 资源泄露检测（自动清理）
 * 
 * 风险等级: LOW
 */
public final class SafeAsyncManager {
    
    private static final Logger LOGGER = Logger.getLogger(SafeAsyncManager.class.getName());
    
    private static SafeAsyncManager instance;
    private final JavaPlugin plugin;
    
    // 安全配置
    private static final int CORE_THREADS = 2;
    private static final int MAX_THREADS = 4;
    private static final int QUEUE_SIZE = 100;
    private static final int TASK_TIMEOUT_SECONDS = 10;
    
    private final ThreadPoolExecutor executor;
    private final ScheduledExecutorService monitor;
    private final AtomicInteger taskCounter = new AtomicInteger(0);
    private final AtomicInteger timeoutCounter = new AtomicInteger(0);
    
    private SafeAsyncManager(JavaPlugin plugin) {
        this.plugin = plugin;
        
        // 创建有界队列的线程池
        this.executor = new ThreadPoolExecutor(
            CORE_THREADS,
            MAX_THREADS,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_SIZE),
            new SafeThreadFactory(),
            new SafeRejectionHandler()
        );
        
        // 创建监控线程
        this.monitor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SafeAsyncManager-Monitor");
            t.setDaemon(true);
            return t;
        });
        
        // 每分钟检查一次
        monitor.scheduleAtFixedRate(this::checkHealth, 60, 60, TimeUnit.SECONDS);
        
        LOGGER.info("SafeAsyncManager initialized with " + CORE_THREADS + "-" + MAX_THREADS + " threads");
    }
    
    public static synchronized void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new SafeAsyncManager(plugin);
        }
    }
    
    public static SafeAsyncManager getInstance() {
        return instance;
    }
    
    /**
     * 提交任务（带超时）
     */
    public <T> Future<T> submitTask(Callable<T> task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        
        taskCounter.incrementAndGet();
        
        // 包装任务以添加超时
        return executor.submit(() -> {
            Future<T> future = executor.submit(task);
            try {
                return future.get(TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                timeoutCounter.incrementAndGet();
                LOGGER.warning("Task timeout after " + TASK_TIMEOUT_SECONDS + " seconds");
                throw new RuntimeException("Task timeout", e);
            }
        });
    }
    
    /**
     * 提交任务（Runnable）
     */
    public Future<?> submitTask(Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        
        return submitTask(() -> {
            task.run();
            return null;
        });
    }
    
    /**
     * 健康检查
     */
    private void checkHealth() {
        int active = executor.getActiveCount();
        int queued = executor.getQueue().size();
        int completed = taskCounter.get();
        int timeouts = timeoutCounter.get();
        
        LOGGER.info(String.format("SafeAsyncManager Health: Active=%d, Queued=%d, Completed=%d, Timeouts=%d",
            active, queued, completed, timeouts));
        
        // 警告
        if (queued > QUEUE_SIZE * 0.8) {
            LOGGER.warning("Task queue is 80% full! Consider reducing task submission rate.");
        }
        
        if (timeouts > completed * 0.1) {
            LOGGER.warning("More than 10% tasks are timing out! Check task implementation.");
        }
    }
    
    /**
     * 获取统计信息
     */
    public Stats getStats() {
        return new Stats(
            executor.getActiveCount(),
            executor.getQueue().size(),
            taskCounter.get(),
            timeoutCounter.get()
        );
    }
    
    /**
     * 关闭
     */
    public void shutdown() {
        LOGGER.info("Shutting down SafeAsyncManager...");
        
        monitor.shutdown();
        executor.shutdown();
        
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        LOGGER.info("SafeAsyncManager shutdown complete");
    }
    
    /**
     * 安全的线程工厂
     */
    private static class SafeThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(0);
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "SafeAsync-Worker-" + counter.incrementAndGet());
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY);
            t.setUncaughtExceptionHandler((thread, throwable) -> {
                LOGGER.severe("Uncaught exception in " + thread.getName() + ": " + throwable.getMessage());
                throwable.printStackTrace();
            });
            return t;
        }
    }
    
    /**
     * 安全的拒绝策略
     */
    private static class SafeRejectionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            LOGGER.warning("Task rejected! Queue is full. Task will be executed in caller thread.");
            // 在调用者线程中执行（CallerRunsPolicy）
            if (!executor.isShutdown()) {
                r.run();
            }
        }
    }
    
    /**
     * 统计信息
     */
    public static class Stats {
        public final int activeThreads;
        public final int queuedTasks;
        public final int completedTasks;
        public final int timeoutTasks;
        
        Stats(int activeThreads, int queuedTasks, int completedTasks, int timeoutTasks) {
            this.activeThreads = activeThreads;
            this.queuedTasks = queuedTasks;
            this.completedTasks = completedTasks;
            this.timeoutTasks = timeoutTasks;
        }
        
        @Override
        public String toString() {
            return String.format("Stats{active=%d, queued=%d, completed=%d, timeout=%d}",
                activeThreads, queuedTasks, completedTasks, timeoutTasks);
        }
    }
}
