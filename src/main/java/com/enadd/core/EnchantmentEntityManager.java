package com.enadd.core;

import com.enadd.core.budget.EntityBudgetController;
import com.enadd.core.entity.EntityLifecycleManager;
import com.enadd.core.entity.factory.EnchantmentEntityFactory;
import com.enadd.core.memory.MemoryManager;
import com.enadd.core.memory.ReferenceTracker;
import com.enadd.core.monitor.PerformanceMonitor;
import com.enadd.core.tracking.EntityTracker;
import com.enadd.core.update.UpdateFrequencyController;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 附魔实体管理器
 * 统一管理所有附魔实体相关的子系统
 *
 * 线程安全：使用AtomicBoolean和线程安全的调度器
 * 单例模式：使用Holder模式实现延迟加载
 *
 * @author EnchAdd Team
 * @version 2.0
 */
public final class EnchantmentEntityManager {
    private static final Logger LOGGER = Logger.getLogger(EnchantmentEntityManager.class.getName());

    // 配置常量
    private static final long DEFAULT_MAINTENANCE_INTERVAL = 60000; // 1分钟（优化：从30秒延长）
    private static final long DEFAULT_PERFORMANCE_CHECK_INTERVAL = 5000; // 5秒（优化：从1秒延长）
    private static final long IDLE_CHECK_INTERVAL = 30000; // 30秒
    private static final double MEMORY_GROWTH_THRESHOLD = 30.0; // 30%
    private static final double BUDGET_USAGE_THRESHOLD = 80.0; // 80%
    private static final long DEFAULT_IDLE_TIME = 60000; // 60秒
    private static final int SCHEDULER_THREAD_POOL_SIZE = 2;
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 5;
    // Holder模式优化单例
    private static final class Holder {
        private static final EnchantmentEntityManager INSTANCE = new EnchantmentEntityManager();
    }

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final AtomicBoolean idleMode = new AtomicBoolean(false); // CPU优化：空闲模式
    private final ScheduledExecutorService scheduler;

    // 缓存所有管理器实例，避免重复调用getInstance()
    private final EntityLifecycleManager lifecycleManager;
    private final EntityBudgetController budgetController;
    private final MemoryManager memoryManager;
    private final ReferenceTracker referenceTracker;
    private final PerformanceMonitor performanceMonitor;
    private final EntityTracker entityTracker;
    private final UpdateFrequencyController updateController;
    private final EnchantmentEntityFactory entityFactory;

    private volatile long maintenanceInterval = DEFAULT_MAINTENANCE_INTERVAL;
    private volatile long performanceCheckInterval = DEFAULT_PERFORMANCE_CHECK_INTERVAL;

    private EnchantmentEntityManager() {
        LOGGER.info("Initializing EnchantmentEntityManager...");

        try {
            // Bug修复1: scheduler初始化异常处理
            this.scheduler = new ScheduledThreadPoolExecutor(SCHEDULER_THREAD_POOL_SIZE, r -> {
                Thread t = new Thread(r, "EnchantmentEntity-Maintenance");
                t.setDaemon(true);
                t.setPriority(Thread.MIN_PRIORITY + 1); // CPU优化：降低优先级
                t.setUncaughtExceptionHandler((thread, throwable) ->
                    LOGGER.log(Level.SEVERE, "Uncaught exception in maintenance thread", throwable));
                return t;
            });

            // Bug修复2-3: 管理器初始化异常处理和null检查
            this.lifecycleManager = safeGetInstance(EntityLifecycleManager::getInstance, "EntityLifecycleManager");
            this.budgetController = safeGetInstance(EntityBudgetController::getInstance, "EntityBudgetController");
            this.memoryManager = safeGetInstance(MemoryManager::getInstance, "MemoryManager");
            this.referenceTracker = safeGetInstance(ReferenceTracker::getInstance, "ReferenceTracker");
            this.performanceMonitor = safeGetInstance(PerformanceMonitor::getInstance, "PerformanceMonitor");
            this.entityTracker = safeGetInstance(EntityTracker::getInstance, "EntityTracker");
            this.updateController = safeGetInstance(UpdateFrequencyController::getInstance, "UpdateFrequencyController");
            this.entityFactory = safeGetInstance(EnchantmentEntityFactory::getInstance, "EnchantmentEntityFactory");

            isInitialized.set(true);
            LOGGER.info("EnchantmentEntityManager initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize EnchantmentEntityManager", e);
            throw new RuntimeException("Failed to initialize EnchantmentEntityManager", e);
        }
    }

    /**
     * 安全获取单例实例
     * Bug修复2-3: 添加null检查和异常处理
     */
    private <T> T safeGetInstance(InstanceSupplier<T> supplier, String name) {
        try {
            T instance = supplier.get();
            if (instance == null) {
                throw new IllegalStateException(name + " getInstance() returned null");
            }
            return instance;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get instance of {0}", name);
            throw new RuntimeException("Failed to get instance of " + name, e);
        }
    }

    @FunctionalInterface
    private interface InstanceSupplier<T> {
        T get();
    }

    public static EnchantmentEntityManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 启动管理器
     * 开始所有维护任务
     *
     * @throws IllegalStateException 如果未初始化
     */
    public void start() {
        if (!isInitialized.get()) {
            throw new IllegalStateException("EnchantmentEntityManager not initialized");
        }

        if (isRunning.compareAndSet(false, true)) {
            try {
                // Bug修复4: 启动异常处理
                startMaintenanceTasks();
                LOGGER.info("EnchantmentEntityManager started successfully");
            } catch (Exception e) {
                // Bug修复25: 启动失败回滚
                isRunning.set(false);
                LOGGER.log(Level.SEVERE, "Failed to start EnchantmentEntityManager", e);
                throw new RuntimeException("Failed to start EnchantmentEntityManager", e);
            }
        } else {
            LOGGER.warning("EnchantmentEntityManager is already running");
        }
    }

    /**
     * 停止管理器
     * 停止所有维护任务并清理资源
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            try {
                // Bug修复5: 停止异常处理
                shutdownAllSystems();
                LOGGER.info("EnchantmentEntityManager stopped successfully");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during shutdown", e);
                // 即使出错也要确保状态正确
            }
        } else {
            LOGGER.warning("EnchantmentEntityManager is not running");
        }
    }

    /**
     * 重启管理器
     * Bug修复33: 添加重启功能
     */
    public void restart() {
        LOGGER.info("Restarting EnchantmentEntityManager...");
        stop();
        try {
            Thread.sleep(1000); // 等待清理完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        start();
    }

    /**
     * 启动维护任务
     * Bug修复6: 添加异常处理
     * CPU优化: 降低检查频率，添加空闲模式
     */
    private void startMaintenanceTasks() {
        try {
            scheduler.scheduleAtFixedRate(
                this::performMaintenanceWithErrorHandling,
                maintenanceInterval,
                maintenanceInterval,
                TimeUnit.MILLISECONDS
            );

            scheduler.scheduleAtFixedRate(
                this::performPerformanceCheckWithErrorHandling,
                performanceCheckInterval,
                performanceCheckInterval,
                TimeUnit.MILLISECONDS
            );

            // CPU优化：添加空闲检测
            scheduler.scheduleAtFixedRate(
                this::checkIdleMode,
                IDLE_CHECK_INTERVAL,
                IDLE_CHECK_INTERVAL,
                TimeUnit.MILLISECONDS
            );

            LOGGER.log(Level.INFO, "Maintenance tasks started: maintenance={0}ms, performance={1}ms (CPU optimized)",
                new Object[]{maintenanceInterval, performanceCheckInterval});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start maintenance tasks", e);
            throw new RuntimeException("Failed to start maintenance tasks", e);
        }
    }

    /**
     * 检查空闲模式
     * CPU优化：无玩家时降低任务频率
     */
    private void checkIdleMode() {
        try {
            int onlinePlayers = org.bukkit.Bukkit.getOnlinePlayers().size();

            if (onlinePlayers == 0) {
                if (idleMode.compareAndSet(false, true)) {
                    LOGGER.info("Entering idle mode - reducing task frequency to save CPU");
                    // 空闲时大幅降低频率
                    maintenanceInterval = 120000; // 2分钟
                    performanceCheckInterval = 30000; // 30秒
                }
            } else {
                if (idleMode.compareAndSet(true, false)) {
                    LOGGER.info("Exiting idle mode - restoring normal frequency");
                    maintenanceInterval = DEFAULT_MAINTENANCE_INTERVAL;
                    performanceCheckInterval = DEFAULT_PERFORMANCE_CHECK_INTERVAL;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking idle mode", e);
        }
    }

    /**
     * 执行维护任务（带错误处理）
     * Bug修复7: 包装异常处理
     */
    private void performMaintenanceWithErrorHandling() {
        if (!isRunning.get()) return;

        try {
            long startTime = System.currentTimeMillis();
            performMaintenance();
            long duration = System.currentTimeMillis() - startTime;

            if (duration > 500) {
                LOGGER.log(Level.WARNING, "Maintenance task took too long: {0}ms", duration);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in maintenance task", e);
        }
    }

    /**
     * 执行性能检查（带错误处理）
     * Bug修复7: 包装异常处理
     */
    private void performPerformanceCheckWithErrorHandling() {
        if (!isRunning.get()) return;

        try {
            checkPerformance();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in performance check task", e);
        }
    }

    /**
     * 执行维护任务
     * Bug修复27: 添加执行时间监控
     */
    private void performMaintenance() {
        if (!isRunning.get()) return;

        long startTime = System.currentTimeMillis();
        try {
            performMemoryMaintenance();
            performEntityMaintenance();
            performBudgetMaintenance();

            long duration = System.currentTimeMillis() - startTime;
            if (duration > 5000) {
                LOGGER.log(Level.WARNING, "Maintenance task took too long: {0}ms", duration);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in performMaintenance", e);
        }
    }

    /**
     * 执行内存维护
     * Bug修复8-9,28: 添加异常处理、null检查和配置化阈值
     */
    private void performMemoryMaintenance() {
        try {
            if (memoryManager != null) {
                memoryManager.checkForLeaks();
            }

            if (referenceTracker != null) {
                referenceTracker.cleanup();
            }

            if (memoryManager != null) {
                MemoryManager.MemoryStats stats = memoryManager.getStats();
                if (stats != null && stats.getGrowthPercent() > MEMORY_GROWTH_THRESHOLD) {
                    LOGGER.log(Level.WARNING, "Memory growth exceeds threshold: {0}%", stats.getGrowthPercent());
                    forceCleanup();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error in memory maintenance", e);
        }
    }

    /**
     * 执行实体维护
     * Bug修复10-11,29: 添加异常处理、配置化参数和阈值
     */
    private void performEntityMaintenance() {
        try {
            if (lifecycleManager != null) {
                lifecycleManager.updateAllEntities();
            }

            if (entityTracker != null) {
                entityTracker.cleanupInactiveEntities(DEFAULT_IDLE_TIME);
            }

            if (budgetController != null) {
                double usagePercent = budgetController.getGlobalUsagePercent();
                if (usagePercent > BUDGET_USAGE_THRESHOLD) {
                    LOGGER.warning("Budget usage exceeds threshold: " + usagePercent + "%");
                    forceCleanup();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error in entity maintenance", e);
        }
    }

    /**
     * 执行预算维护
     * Bug修复12: 使用返回值
     */
    private void performBudgetMaintenance() {
        try {
            if (budgetController != null) {
                EntityBudgetController.BudgetStatus status = budgetController.getStatus();
                if (status != null && !status.isGlobalHealthy()) {
                    LOGGER.warning("Budget status unhealthy: " + status);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error in budget maintenance", e);
        }
    }

    /**
     * 检查性能
     * Bug修复13-14: 添加null检查和异常处理
     */
    private void checkPerformance() {
        if (!isRunning.get()) return;

        try {
            if (performanceMonitor == null) {
                return;
            }

            PerformanceMonitor.PerformanceStats stats = performanceMonitor.getStats();
            if (stats == null) {
                LOGGER.warning("Performance stats is null");
                return;
            }

            if (!stats.isHealthy()) {
                onPerformanceWarning(stats);
            }

            if (performanceMonitor.isCriticalLoad()) {
                onCriticalLoad();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error in performance check", e);
        }
    }

    /**
     * 性能警告处理
     * Bug修复31: 添加默认实现
     */
    protected void onPerformanceWarning(PerformanceMonitor.PerformanceStats stats) {
        LOGGER.warning("Performance warning: " + stats);
    }

    /**
     * 临界负载处理
     * Bug修复15-16: 添加异常处理
     */
    protected void onCriticalLoad() {
        LOGGER.severe("Critical load detected, forcing cleanup");
        try {
            forceCleanup();
            if (budgetController != null) {
                budgetController.resetAll();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling critical load", e);
        }
    }

    /**
     * 强制清理
     * Bug修复17: 添加异常处理
     */
    public void forceCleanup() {
        LOGGER.info("Forcing cleanup...");
        try {
            if (lifecycleManager != null) {
                lifecycleManager.forceCleanupAll();
            }
            if (referenceTracker != null) {
                referenceTracker.cleanup();
            }
            LOGGER.info("Cleanup completed");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during force cleanup", e);
        }
    }

    /**
     * 关闭所有系统
     * Bug修复18-19,26: 添加异常处理、顺序控制和超时处理
     */
    private void shutdownAllSystems() {
        LOGGER.info("Shutting down all systems...");

        // 1. 停止调度器
        scheduler.shutdown();
        try {
            // Bug修复26: 添加超时处理
            if (!scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                LOGGER.warning("Scheduler did not terminate in time, forcing shutdown");
                scheduler.shutdownNow();
                if (!scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    LOGGER.severe("Scheduler did not terminate after force shutdown");
                }
            }
        } catch (InterruptedException e) {
            LOGGER.warning("Interrupted while waiting for scheduler shutdown");
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 2. 清理实体（按依赖顺序）
        safeShutdown("EntityLifecycleManager", () -> {
            if (lifecycleManager != null) lifecycleManager.forceCleanupAll();
        });

        safeShutdown("EntityFactory", () -> {
            if (entityFactory != null) entityFactory.shutdown();
        });

        // 3. 清理控制器
        safeShutdown("BudgetController", () -> {
            if (budgetController != null) budgetController.shutdown();
        });

        safeShutdown("UpdateController", () -> {
            if (updateController != null) updateController.clearAll();
        });

        // 4. 清理监控和追踪
        safeShutdown("PerformanceMonitor", () -> {
            if (performanceMonitor != null) performanceMonitor.shutdown();
        });

        safeShutdown("ReferenceTracker", () -> {
            if (referenceTracker != null) referenceTracker.clearAllRecords();
        });

        // 5. 最后清理内存管理器
        safeShutdown("MemoryManager", () -> {
            if (memoryManager != null) memoryManager.shutdown();
        });

        LOGGER.info("All systems shut down");
    }

    /**
     * 安全关闭组件
     * Bug修复18: 添加异常隔离
     */
    private void safeShutdown(String componentName, Runnable shutdownAction) {
        try {
            shutdownAction.run();
            LOGGER.log(Level.INFO, "{0} shut down successfully", componentName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error shutting down {0}", componentName);
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * 设置维护间隔
     * Bug修复20-21: 添加参数验证和运行时更新
     *
     * @param millis 维护间隔（毫秒），必须>0
     * @throws IllegalArgumentException 如果参数无效
     */
    public void setMaintenanceInterval(long millis) {
        if (millis <= 0) {
            throw new IllegalArgumentException("Maintenance interval must be positive, got: " + millis);
        }

        long oldInterval = this.maintenanceInterval;
        this.maintenanceInterval = millis;

        // 如果正在运行，需要重启任务以应用新间隔
        if (isRunning.get()) {
            LOGGER.log(Level.INFO, "Maintenance interval changed from {0}ms to {1}ms (CPU optimization: {2}), restarting tasks...",
                new Object[]{oldInterval, millis, millis > oldInterval ? "REDUCED LOAD" : "INCREASED LOAD"});
            restart();
        }
    }

    public long getMaintenanceInterval() {
        return maintenanceInterval;
    }

    /**
     * 设置性能检查间隔
     *
     * @param millis 检查间隔（毫秒），必须>0
     * @throws IllegalArgumentException 如果参数无效
     */
    public void setPerformanceCheckInterval(long millis) {
        if (millis <= 0) {
            throw new IllegalArgumentException("Performance check interval must be positive, got: " + millis);
        }

        long oldInterval = this.performanceCheckInterval;
        this.performanceCheckInterval = millis;

        if (isRunning.get()) {
            LOGGER.log(Level.INFO, "Performance check interval changed from {0}ms to {1}ms (CPU optimization: {2}), restarting tasks...",
                new Object[]{oldInterval, millis, millis > oldInterval ? "REDUCED LOAD" : "INCREASED LOAD"});
            restart();
        }
    }

    public long getPerformanceCheckInterval() {
        return performanceCheckInterval;
    }

    /**
     * 获取系统状态
     * Bug修复22: 添加null检查
     *
     * @return 系统状态快照
     */
    public SystemStatus getSystemStatus() {
        try {
            return new SystemStatus(
                    isRunning.get(),
                    lifecycleManager != null ? lifecycleManager.getStats() : null,
                    budgetController != null ? budgetController.getStatus() : null,
                    memoryManager != null ? memoryManager.getStats() : null,
                    performanceMonitor != null ? performanceMonitor.getStats() : null,
                    entityTracker != null ? entityTracker.getStats() : null,
                    updateController != null ? updateController.getStats() : null
            );
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting system status", e);
            return new SystemStatus(isRunning.get(), null, null, null, null, null, null);
        }
    }

    public EntityLifecycleManager getLifecycleManager() {
        return lifecycleManager;
    }

    public EntityBudgetController getBudgetController() {
        return budgetController;
    }

    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    public PerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }

    public EntityTracker getEntityTracker() {
        return entityTracker;
    }

    public EnchantmentEntityFactory getEntityFactory() {
        return entityFactory;
    }

    public UpdateFrequencyController getUpdateController() {
        return updateController;
    }

    /**
     * 系统状态快照
     * 包含所有子系统的状态信息
     */
    public static final class SystemStatus {
        private final boolean running;
        private final EntityLifecycleManager.EntityStateStats lifecycleStats;
        private final EntityBudgetController.BudgetStatus budgetStatus;
        private final MemoryManager.MemoryStats memoryStats;
        private final PerformanceMonitor.PerformanceStats performanceStats;
        private final EntityTracker.TrackerStats trackerStats;
        private final UpdateFrequencyController.UpdateStats updateStats;

        public SystemStatus(boolean running,
                          EntityLifecycleManager.EntityStateStats lifecycle,
                          EntityBudgetController.BudgetStatus budget,
                          MemoryManager.MemoryStats memory,
                          PerformanceMonitor.PerformanceStats performance,
                          EntityTracker.TrackerStats tracker,
                          UpdateFrequencyController.UpdateStats update) {
            this.running = running;
            // Bug修复23: 参数null检查（允许null，因为可能获取失败）
            this.lifecycleStats = lifecycle;
            this.budgetStatus = budget;
            this.memoryStats = memory;
            this.performanceStats = performance;
            this.trackerStats = tracker;
            this.updateStats = update;
        }

        public boolean isRunning() { return running; }
        public EntityLifecycleManager.EntityStateStats getLifecycleStats() { return lifecycleStats; }
        public EntityBudgetController.BudgetStatus getBudgetStatus() { return budgetStatus; }
        public MemoryManager.MemoryStats getMemoryStats() { return memoryStats; }
        public PerformanceMonitor.PerformanceStats getPerformanceStats() { return performanceStats; }
        public EntityTracker.TrackerStats getTrackerStats() { return trackerStats; }
        public UpdateFrequencyController.UpdateStats getUpdateStats() { return updateStats; }

        /**
         * 检查系统是否健康
         * Bug修复32: 改进健康检查逻辑
         *
         * @return true如果系统健康
         */
        public boolean isHealthy() {
            if (!running) {
                return false;
            }

            // 检查预算状态
            if (budgetStatus != null && !budgetStatus.isGlobalHealthy()) {
                return false;
            }

            // 检查性能状态
            if (performanceStats != null && !performanceStats.isHealthy()) {
                return false;
            }

            // 检查内存增长
            if (memoryStats != null && memoryStats.getGrowthPercent() > MEMORY_GROWTH_THRESHOLD) {
                return false;
            }

            return true;
        }

        /**
         * Bug修复35: 添加toString方法
         */
        @Override
        public String toString() {
            return String.format(
                "SystemStatus{running=%s, healthy=%s, lifecycle=%s, budget=%s, memory=%s, performance=%s}",
                running, isHealthy(),
                lifecycleStats != null ? "OK" : "NULL",
                budgetStatus != null ? "OK" : "NULL",
                memoryStats != null ? "OK" : "NULL",
                performanceStats != null ? "OK" : "NULL"
            );
        }
    }

    /**
     * Bug修复34: 添加toString方法
     */
    @Override
    public String toString() {
        return String.format(
            "EnchantmentEntityManager{running=%s, initialized=%s, maintenanceInterval=%dms, performanceCheckInterval=%dms}",
            isRunning.get(), isInitialized.get(), maintenanceInterval, performanceCheckInterval
        );
    }
}
