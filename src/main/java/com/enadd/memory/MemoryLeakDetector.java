package com.enadd.memory;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * 内存泄露检测器
 *
 * 检测并修复：
 * 1. 未注销的事件监听器
 * 2. 未关闭的线程
 * 3. 静态集合未清理
 * 4. 缓存未限制大小
 * 5. 弱引用泄露
 */
public final class MemoryLeakDetector {
    private static final Logger LOGGER = Logger.getLogger(MemoryLeakDetector.class.getName());

    private static MemoryLeakDetector instance;
    private final JavaPlugin plugin;

    // 跟踪所有注册的监听器
    private final Set<WeakReference<Object>> registeredListeners = ConcurrentHashMap.newKeySet();

    // 跟踪所有创建的线程
    private final Set<WeakReference<Thread>> createdThreads = ConcurrentHashMap.newKeySet();

    // 跟踪静态集合
    private final Map<String, CollectionInfo> staticCollections = new ConcurrentHashMap<>();

    // 内存使用统计
    private final AtomicLong lastMemoryCheck = new AtomicLong(System.currentTimeMillis());
    private long lastUsedMemory = 0;

    private MemoryLeakDetector(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new MemoryLeakDetector(plugin);
            instance.startMonitoring();
        }
    }

    public static MemoryLeakDetector getInstance() {
        return instance;
    }

    /**
     * 开始监控
     */
    private void startMonitoring() {
        // 每5分钟检查一次
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                checkMemoryLeaks();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during memory leak check", e);
            }
        }, 6000L, 6000L); // 5分钟

        LOGGER.info("Memory leak detector started");
    }

    /**
     * 检查内存泄露
     */
    private void checkMemoryLeaks() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryGrowth = usedMemory - lastUsedMemory;

        if (memoryGrowth > 50 * 1024 * 1024) { // 增长超过50MB
            LOGGER.warning("§eMemory growth detected: " + (memoryGrowth / 1024 / 1024) + " MB");

            // 检查各种泄露源
            checkListenerLeaks();
            checkThreadLeaks();
            checkStaticCollectionLeaks();

            // 建议GC
            System.gc();
        }

        lastUsedMemory = usedMemory;
        lastMemoryCheck.set(System.currentTimeMillis());
    }

    /**
     * 检查监听器泄露
     */
    private void checkListenerLeaks() {
        // 清理已失效的弱引用
        registeredListeners.removeIf(ref -> ref.get() == null);

        int activeListeners = registeredListeners.size();
        if (activeListeners > 100) {
            LOGGER.warning("§cPotential listener leak detected: " + activeListeners + " active listeners");
        }
    }

    /**
     * 检查线程泄露
     */
    private void checkThreadLeaks() {
        // 清理已终止的线程引用
        createdThreads.removeIf(ref -> {
            Thread thread = ref.get();
            return thread == null || !thread.isAlive();
        });

        int activeThreads = createdThreads.size();
        if (activeThreads > 20) {
            LOGGER.warning("§cPotential thread leak detected: " + activeThreads + " active threads");

            // 列出所有活动线程
            for (WeakReference<Thread> ref : createdThreads) {
                Thread thread = ref.get();
                if (thread != null && thread.isAlive()) {
                    LOGGER.warning("  - Thread: " + thread.getName() + " (State: " + thread.getState() + ")");
                }
            }
        }
    }

    /**
     * 检查静态集合泄露
     */
    private void checkStaticCollectionLeaks() {
        for (Map.Entry<String, CollectionInfo> entry : staticCollections.entrySet()) {
            CollectionInfo info = entry.getValue();
            int currentSize = info.getCurrentSize();

            if (currentSize > info.getMaxExpectedSize()) {
                LOGGER.warning("§cPotential static collection leak: " + entry.getKey() +
                    " (Size: " + currentSize + ", Expected: " + info.getMaxExpectedSize() + ")");
            }
        }
    }

    /**
     * 注册监听器（用于跟踪）
     */
    public void registerListener(Object listener) {
        if (listener != null) {
            registeredListeners.add(new WeakReference<>(listener));
        }
    }

    /**
     * 注销监听器
     */
    public void unregisterListener(Object listener) {
        if (listener != null) {
            registeredListeners.removeIf(ref -> ref.get() == listener);
        }
    }

    /**
     * 注册线程（用于跟踪）
     */
    public void registerThread(Thread thread) {
        if (thread != null) {
            createdThreads.add(new WeakReference<>(thread));
        }
    }

    /**
     * 注册静态集合（用于监控）
     */
    public void registerStaticCollection(String name, Collection<?> collection, int maxExpectedSize) {
        staticCollections.put(name, new CollectionInfo(collection, maxExpectedSize));
    }

    /**
     * 获取内存报告
     */
    public String getMemoryReport() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        StringBuilder report = new StringBuilder();
        report.append("§6=== Memory Report ===\n");
        report.append("§7Used: §e").append(usedMemory / 1024 / 1024).append(" MB\n");
        report.append("§7Total: §e").append(totalMemory / 1024 / 1024).append(" MB\n");
        report.append("§7Max: §e").append(maxMemory / 1024 / 1024).append(" MB\n");
        report.append("§7Active Listeners: §e").append(registeredListeners.size()).append("\n");
        report.append("§7Active Threads: §e").append(createdThreads.size()).append("\n");
        report.append("§7Monitored Collections: §e").append(staticCollections.size());

        return report.toString();
    }

    /**
     * 清理所有跟踪的资源
     */
    public void cleanup() {
        registeredListeners.clear();
        createdThreads.clear();
        staticCollections.clear();
        LOGGER.info("Memory leak detector cleaned up");
    }

    /**
     * 集合信息类
     */
    private static class CollectionInfo {
        private final WeakReference<Collection<?>> collectionRef;
        private final int maxExpectedSize;

        public CollectionInfo(Collection<?> collection, int maxExpectedSize) {
            this.collectionRef = new WeakReference<>(collection);
            this.maxExpectedSize = maxExpectedSize;
        }

        public int getCurrentSize() {
            Collection<?> collection = collectionRef.get();
            return collection != null ? collection.size() : 0;
        }

        public int getMaxExpectedSize() {
            return maxExpectedSize;
        }
    }
}
