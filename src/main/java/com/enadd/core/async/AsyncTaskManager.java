package com.enadd.core.async;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;



/**
 * 异步任务管理器 - 处理非关键操作
 * 使用线程池优化性能，避免阻塞主线程
 */
public final class AsyncTaskManager {

    private static final class Holder {
        private static final AsyncTaskManager INSTANCE = new AsyncTaskManager();
    }

    private final ExecutorService asyncExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    private final AtomicLong taskIdGenerator;
    private final AtomicInteger activeTaskCount;
    private final ConcurrentHashMap<Long, Future<?>> activeTasks;

    private volatile Plugin plugin;
    private volatile boolean shutdown = false;

    private AsyncTaskManager() {
        // 创建异步执行器 - 核心线程数=CPU核心数，最大线程数=CPU核心数*2
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maxPoolSize = corePoolSize * 2;

        this.asyncExecutor = new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new NamedThreadFactory("EnchAdd-Async"),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

        // 创建定时执行器
        this.scheduledExecutor = Executors.newScheduledThreadPool(
            2,
            new NamedThreadFactory("EnchAdd-Scheduled")
        );

        this.taskIdGenerator = new AtomicLong(0);
        this.activeTaskCount = new AtomicInteger(0);
        this.activeTasks = new ConcurrentHashMap<>(128);
    }

    public static AsyncTaskManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 初始化管理器
     */
    public void initialize(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 提交异步任务
     */
    public long submitAsync(Runnable task) {
        if (shutdown) {
            throw new IllegalStateException("AsyncTaskManager is shutdown");
        }

        long taskId = taskIdGenerator.incrementAndGet();
        activeTaskCount.incrementAndGet();

        Future<?> future = asyncExecutor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                if (plugin != null) {
                    plugin.getLogger().warning("Async task " + taskId + " failed: " + e.getMessage());
                }
            } finally {
                activeTaskCount.decrementAndGet();
                activeTasks.remove(taskId);
            }
        });

        activeTasks.put(taskId, future);
        return taskId;
    }

    /**
     * 提交异步任务并返回结果
     */
    public <T> CompletableFuture<T> submitAsyncWithResult(Callable<T> task) {
        if (shutdown) {
            throw new IllegalStateException("AsyncTaskManager is shutdown");
        }

        CompletableFuture<T> future = new CompletableFuture<>();

        submitAsync(() -> {
            try {
                T result = task.call();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * 延迟执行任务
     */
    public long scheduleAsync(Runnable task, long delay, TimeUnit unit) {
        if (shutdown) {
            throw new IllegalStateException("AsyncTaskManager is shutdown");
        }

        long taskId = taskIdGenerator.incrementAndGet();
        activeTaskCount.incrementAndGet();

        ScheduledFuture<?> future = scheduledExecutor.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                if (plugin != null) {
                    plugin.getLogger().warning("Scheduled task " + taskId + " failed: " + e.getMessage());
                }
            } finally {
                activeTaskCount.decrementAndGet();
                activeTasks.remove(taskId);
            }
        }, delay, unit);

        activeTasks.put(taskId, future);
        return taskId;
    }

    /**
     * 周期性执行任务
     */
    public long scheduleAsyncRepeating(Runnable task, long initialDelay, long period, TimeUnit unit) {
        if (shutdown) {
            throw new IllegalStateException("AsyncTaskManager is shutdown");
        }

        long taskId = taskIdGenerator.incrementAndGet();

        ScheduledFuture<?> future = scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                if (plugin != null) {
                    plugin.getLogger().warning("Repeating task " + taskId + " failed: " + e.getMessage());
                }
            }
        }, initialDelay, period, unit);

        activeTasks.put(taskId, future);
        return taskId;
    }

    /**
     * 在主线程执行任务（通过Bukkit调度器）
     */
    public BukkitTask runSync(Runnable task) {
        if (plugin == null) {
            throw new IllegalStateException("Plugin not initialized");
        }
        return plugin.getServer().getScheduler().runTask(plugin, task);
    }

    /**
     * 延迟在主线程执行任务
     */
    public BukkitTask runSyncLater(Runnable task, long delayTicks) {
        if (plugin == null) {
            throw new IllegalStateException("Plugin not initialized");
        }
        return plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    /**
     * 取消任务
     */
    public boolean cancelTask(long taskId) {
        Future<?> future = activeTasks.remove(taskId);
        if (future != null) {
            boolean cancelled = future.cancel(false);
            if (cancelled) {
                activeTaskCount.decrementAndGet();
            }
            return cancelled;
        }
        return false;
    }

    /**
     * 获取活跃任务数
     */
    public int getActiveTaskCount() {
        return activeTaskCount.get();
    }

    /**
     * 获取任务统计
     */
    public TaskStats getStats() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) asyncExecutor;
        return new TaskStats(
            activeTaskCount.get(),
            executor.getPoolSize(),
            executor.getActiveCount(),
            executor.getCompletedTaskCount()
        );
    }

    /**
     * 关闭管理器
     */
    public void shutdown() {
        if (shutdown) {
            return;
        }

        shutdown = true;

        // 取消所有活跃任务
        for (Future<?> future : activeTasks.values()) {
            future.cancel(false);
        }
        activeTasks.clear();

        // 关闭执行器
        asyncExecutor.shutdown();
        scheduledExecutor.shutdown();

        try {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        if (plugin != null) {
            plugin.getLogger().info("AsyncTaskManager shutdown complete");
        }
    }

    /**
     * 命名线程工厂
     */
    private static final class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + "-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }

    /**
     * 任务统计信息
     */
    public static final class TaskStats {
        private final int activeTasks;
        private final int poolSize;
        private final int activeThreads;
        private final long completedTasks;

        public TaskStats(int activeTasks, int poolSize, int activeThreads, long completedTasks) {
            this.activeTasks = activeTasks;
            this.poolSize = poolSize;
            this.activeThreads = activeThreads;
            this.completedTasks = completedTasks;
        }

        public int getActiveTasks() { return activeTasks; }
        public int getPoolSize() { return poolSize; }
        public int getActiveThreads() { return activeThreads; }
        public long getCompletedTasks() { return completedTasks; }

        @Override
        public String toString() {
            return String.format("Active: %d, Pool: %d, Threads: %d, Completed: %d",
                activeTasks, poolSize, activeThreads, completedTasks);
        }
    }
}
