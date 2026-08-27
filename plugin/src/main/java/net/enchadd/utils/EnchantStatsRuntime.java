package net.enchadd.utils;

import net.enchadd.EnchADDConfig;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

final class EnchantStatsRuntime {

    private static final String STATS_FILE = "data/enchant_stats.jsonl";
    private static final String SESSION_ID = UUID.randomUUID().toString().substring(0, 8);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private static final int FLUSH_INTERVAL_TICKS = 6000;
    private static final int WRITE_BATCH_SIZE = 16;
    private static final long WRITER_POLL_MILLIS = 1000L;

    private final Map<String, AtomicLong> triggerCounts = new java.util.concurrent.ConcurrentHashMap<>();
    private final BlockingQueue<EnchantStatsSnapshot> writeQueue;
    private final AtomicLong droppedSnapshots = new AtomicLong(0L);

    private volatile boolean writerRunning;
    private volatile Thread writerThread;
    private volatile BukkitTask flushTask;
    private volatile Path dataDir;

    EnchantStatsRuntime(BlockingQueue<EnchantStatsSnapshot> writeQueue) {
        this.writeQueue = writeQueue;
    }

    void init(JavaPlugin plugin, Iterable<Key> enchantKeys) {
        dataDir = plugin.getDataFolder().toPath();
        triggerCounts.clear();
        for (Key key : enchantKeys) {
            triggerCounts.put(key.asString(), new AtomicLong(0L));
        }

        stopFlushTask();
        writeQueue.clear();
        droppedSnapshots.set(0L);
        startWriterThread();
        flushTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::flush,
                FLUSH_INTERVAL_TICKS,
                FLUSH_INTERVAL_TICKS
        );
    }

    void record(Key enchantKey) {
        triggerCounts.computeIfAbsent(enchantKey.asString(), k -> new AtomicLong(0L)).incrementAndGet();
    }

    void recordBatch(Key enchantKey, int count) {
        if (count <= 0) {
            return;
        }
        triggerCounts
                .computeIfAbsent(enchantKey.asString(), k -> new AtomicLong(0L))
                .addAndGet(count);
    }

    long getCount(Key enchantKey) {
        AtomicLong counter = triggerCounts.get(enchantKey.asString());
        return counter != null ? counter.get() : 0L;
    }

    long getTotalCount() {
        long total = 0L;
        for (AtomicLong value : triggerCounts.values()) {
            total += value.get();
        }
        return total;
    }

    int getPendingWriteCount() {
        return writeQueue.size();
    }

    long getDroppedSnapshotCount() {
        return droppedSnapshots.get();
    }

    void flush() {
        if (dataDir == null) {
            return;
        }
        enqueueSnapshot(false);
    }

    void flushSync() {
        stopFlushTask();
        if (dataDir == null) {
            return;
        }
        enqueueSnapshot(false);
        stopWriterThread();

        List<EnchantStatsSnapshot> remaining = new ArrayList<>();
        writeQueue.drainTo(remaining);
        remaining.add(buildSnapshot(true));
        writeBatch(remaining);
    }

    List<String> summary() {
        List<Map.Entry<String, AtomicLong>> entries = new ArrayList<>(triggerCounts.entrySet());
        entries.sort((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()));

        List<String> lines = new ArrayList<>();
        lines.add("=== EnchADD 附魔触发统计（本次会话）===");
        for (Map.Entry<String, AtomicLong> entry : entries) {
            if (entry.getValue().get() > 0) {
                lines.add(String.format("  %-40s %,d 次", entry.getKey(), entry.getValue().get()));
            }
        }
        if (lines.size() == 1) {
            lines.add("  暂无触发记录。");
        }
        return lines;
    }

    void writerLoop() {
        while (writerRunning || !writeQueue.isEmpty()) {
            EnchantStatsSnapshot head;
            try {
                head = writeQueue.poll(WRITER_POLL_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
                if (!writerRunning) {
                    break;
                }
                continue;
            }
            if (head == null) {
                continue;
            }

            List<EnchantStatsSnapshot> batch = new ArrayList<>(WRITE_BATCH_SIZE);
            batch.add(head);
            writeQueue.drainTo(batch, WRITE_BATCH_SIZE - 1);
            writeBatch(batch);
        }
    }

    void writeBatch(List<EnchantStatsSnapshot> batch) {
        if (dataDir == null || batch == null || batch.isEmpty()) {
            return;
        }
        try {
            Path statsFile = dataDir.resolve(STATS_FILE);
            Files.createDirectories(statsFile.getParent());

            StringBuilder sb = new StringBuilder();
            for (EnchantStatsSnapshot snapshot : batch) {
                EnchantStatsJsonWriter.appendJsonLine(sb, SESSION_ID, ISO, snapshot);
                sb.append(System.lineSeparator());
            }

            Files.writeString(
                    statsFile,
                    sb.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            RuntimeErrorTracker.recordError("EnchantStats.writeBatch");
            if (EnchADDConfig.DEBUG) {
                Bukkit.getLogger().warning("[EnchADD] 统计数据写入失败: " + e.getMessage());
            }
        }
    }

    private void enqueueSnapshot(boolean shutdown) {
        EnchantStatsSnapshot snapshot = buildSnapshot(shutdown);
        if (!writeQueue.offer(snapshot)) {
            long dropped = droppedSnapshots.incrementAndGet();
            RuntimeErrorTracker.recordError("EnchantStats.queue_full");
            if (EnchADDConfig.DEBUG && (dropped == 1 || dropped % 50 == 0)) {
                Bukkit.getLogger().warning("[EnchADD] 统计快照队列已满，已丢弃 " + dropped + " 条快照");
            }
        }
    }

    private EnchantStatsSnapshot buildSnapshot(boolean shutdown) {
        Map<String, Long> snapshot = new HashMap<>();
        for (Map.Entry<String, AtomicLong> entry : triggerCounts.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue().get());
        }
        return new EnchantStatsSnapshot(Map.copyOf(snapshot), OffsetDateTime.now(), shutdown);
    }

    private void startWriterThread() {
        stopWriterThread();
        writerRunning = true;
        Thread thread = new Thread(this::writerLoop, "EnchADD-StatsWriter");
        thread.setDaemon(true);
        thread.start();
        writerThread = thread;
    }

    private void stopWriterThread() {
        writerRunning = false;
        Thread thread = writerThread;
        writerThread = null;
        if (thread == null) {
            return;
        }
        thread.interrupt();
        try {
            thread.join(2000L);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private void stopFlushTask() {
        BukkitTask task = flushTask;
        flushTask = null;
        if (task == null) {
            return;
        }
        task.cancel();
    }
}
