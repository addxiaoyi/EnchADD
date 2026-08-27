package net.enchadd.utils;

import net.enchadd.EnchADDConfig;
import net.kyori.adventure.key.Key;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 优化9：统计数据持久化层（Statistics Persistence Layer）
 *
 * 保留兼容门面，同时把计数、调度、写盘、JSON 组装拆到独立支撑类。
 */
public final class EnchantStats {

    private static final int WRITE_QUEUE_CAPACITY = 64;
    private static final BlockingQueue<EnchantStatsSnapshot> writeQueue = new ArrayBlockingQueue<>(WRITE_QUEUE_CAPACITY);
    private static final EnchantStatsRuntime RUNTIME = new EnchantStatsRuntime(writeQueue);

    private EnchantStats() {
    }

    public static void init(JavaPlugin plugin) {
        RUNTIME.init(plugin, EnchADDConfig.ENCHANTS.keySet());
    }

    public static void record(Key enchantKey) {
        RUNTIME.record(enchantKey);
    }

    public static void recordBatch(Key enchantKey, int count) {
        RUNTIME.recordBatch(enchantKey, count);
    }

    public static long getCount(Key enchantKey) {
        return RUNTIME.getCount(enchantKey);
    }

    public static long getTotalCount() {
        return RUNTIME.getTotalCount();
    }

    public static int getPendingWriteCount() {
        return RUNTIME.getPendingWriteCount();
    }

    public static long getDroppedSnapshotCount() {
        return RUNTIME.getDroppedSnapshotCount();
    }

    public static void flush() {
        RUNTIME.flush();
    }

    public static void flushSync() {
        RUNTIME.flushSync();
    }

    public static List<String> summary() {
        return RUNTIME.summary();
    }

    private static void writerLoop() {
        RUNTIME.writerLoop();
    }

    private static void writeBatch(List<EnchantStatsSnapshot> batch) {
        RUNTIME.writeBatch(batch);
    }
}
