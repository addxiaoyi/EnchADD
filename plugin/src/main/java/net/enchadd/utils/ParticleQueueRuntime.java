package net.enchadd.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

final class ParticleQueueRuntime {

    private static final int QUEUE_CAPACITY = 10000;
    private static final int ADAPT_INTERVAL_TICKS = 20;
    private static final int WARN_INTERVAL_TICKS = 200;

    private final BlockingQueue<ParticleQueueRequest> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

    private final AtomicInteger droppedCount = new AtomicInteger(0);
    private final AtomicLong submittedCount = new AtomicLong(0);
    private final AtomicLong suppressedCount = new AtomicLong(0);
    private final AtomicLong droppedParticleUnits = new AtomicLong(0);
    private final AtomicLong submittedParticleUnits = new AtomicLong(0);
    private final AtomicLong suppressedParticleUnits = new AtomicLong(0);
    private final AtomicInteger offeredInWindow = new AtomicInteger(0);
    private final AtomicInteger droppedInWindow = new AtomicInteger(0);
    private final AtomicInteger peakQueueSize = new AtomicInteger(0);

    private volatile int baseMaxPerTick = 100;
    private volatile int dynamicMaxPerTick = 100;
    private volatile int minDynamicMaxPerTick = 50;
    private volatile int maxDynamicMaxPerTick = 400;
    private volatile double warnDropRateThreshold = 0.05;
    private volatile double lastWindowDropRate = 0.0;
    private volatile double maxWindowDropRate = 0.0;
    private volatile long flushTicks = 0L;

    private volatile BukkitTask flushTask;

    void start(JavaPlugin plugin,
               int basePerTick,
               int minPerTick,
               int maxPerTick,
               double warnDropRate) {
        baseMaxPerTick = Math.max(1, basePerTick);
        minDynamicMaxPerTick = Math.max(1, Math.min(minPerTick, baseMaxPerTick));
        maxDynamicMaxPerTick = Math.max(baseMaxPerTick, maxPerTick);
        dynamicMaxPerTick = baseMaxPerTick;
        warnDropRateThreshold = Math.max(0.0, Math.min(1.0, warnDropRate));
        flushTicks = 0L;
        droppedCount.set(0);
        submittedCount.set(0L);
        suppressedCount.set(0L);
        droppedParticleUnits.set(0L);
        submittedParticleUnits.set(0L);
        suppressedParticleUnits.set(0L);
        offeredInWindow.set(0);
        droppedInWindow.set(0);
        peakQueueSize.set(0);
        lastWindowDropRate = 0.0;
        maxWindowDropRate = 0.0;

        stop();
        flushTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::flush, 1L, 1L);
    }

    void stop() {
        BukkitTask task = flushTask;
        flushTask = null;
        if (task != null) {
            task.cancel();
        }
        queue.clear();
    }

    void submit(World world, Location location, Particle particle,
                int count, double offsetX, double offsetY, double offsetZ, double speed) {
        if (world == null || location == null || particle == null || count <= 0) {
            return;
        }
        if (SafetyModeManager.shouldSuppressParticles()
                || EnchantExecutionBudgetManager.shouldSuppressParticlesForCurrentExecution()) {
            suppressedCount.incrementAndGet();
            suppressedParticleUnits.addAndGet(count);
            return;
        }
        submittedCount.incrementAndGet();
        submittedParticleUnits.addAndGet(count);
        offeredInWindow.incrementAndGet();

        boolean added = queue.offer(new ParticleQueueRequest(
                world,
                location.getX(),
                location.getY(),
                location.getZ(),
                particle,
                count,
                offsetX,
                offsetY,
                offsetZ,
                speed
        ));
        if (!added) {
            droppedCount.incrementAndGet();
            droppedParticleUnits.addAndGet(count);
            droppedInWindow.incrementAndGet();
            return;
        }
        peakQueueSize.accumulateAndGet(queue.size(), Math::max);
    }

    void submit(Location location, Particle particle, int count) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        submit(location.getWorld(), location, particle, count, 0, 0, 0, 0);
    }

    void flush() {
        flushTicks++;
        if (flushTicks % ADAPT_INTERVAL_TICKS == 0) {
            adaptThroughput();
        }
        if (flushTicks % WARN_INTERVAL_TICKS == 0) {
            emitDropWarningIfNeeded();
        }

        int processed = 0;
        int perTickLimit = dynamicMaxPerTick;
        while (processed < perTickLimit) {
            ParticleQueueRequest request = queue.poll();
            if (request == null) {
                break;
            }
            request.spawn();
            processed++;
        }
    }

    void adaptThroughput() {
        int offered = Math.max(0, offeredInWindow.getAndSet(0));
        int dropped = Math.max(0, droppedInWindow.getAndSet(0));
        lastWindowDropRate = ParticleQueueAdaptationSupport.windowDropRate(offered, dropped);
        maxWindowDropRate = Math.max(maxWindowDropRate, lastWindowDropRate);

        double queueFillRatio = queue.size() / (double) QUEUE_CAPACITY;
        int step = ParticleQueueAdaptationSupport.adaptiveStep(baseMaxPerTick);

        if (ParticleQueueAdaptationSupport.shouldIncrease(lastWindowDropRate, queueFillRatio, warnDropRateThreshold)) {
            dynamicMaxPerTick = ParticleQueueAdaptationSupport.increaseBudget(
                    dynamicMaxPerTick,
                    maxDynamicMaxPerTick,
                    step
            );
            return;
        }

        if (ParticleQueueAdaptationSupport.shouldDecrease(lastWindowDropRate, queueFillRatio)) {
            dynamicMaxPerTick = ParticleQueueAdaptationSupport.decreaseBudget(
                    dynamicMaxPerTick,
                    minDynamicMaxPerTick,
                    step
            );
        }
    }

    void emitDropWarningIfNeeded() {
        if (!ParticleQueueAdaptationSupport.shouldWarn(lastWindowDropRate, warnDropRateThreshold)) {
            return;
        }
        Bukkit.getLogger().warning(String.format(
                "[EnchADD] ParticleQueue dropRate=%.2f%% dropped=%d queue=%d dynamicMaxPerTick=%d",
                lastWindowDropRate * 100.0,
                droppedCount.get(),
                queue.size(),
                dynamicMaxPerTick
        ));
    }

    int getDroppedCount() {
        return droppedCount.get();
    }

    long getSubmittedCount() {
        return submittedCount.get();
    }

    long getDroppedParticleUnits() {
        return droppedParticleUnits.get();
    }

    long getSubmittedParticleUnits() {
        return submittedParticleUnits.get();
    }

    long getSuppressedCount() {
        return suppressedCount.get();
    }

    long getSuppressedParticleUnits() {
        return suppressedParticleUnits.get();
    }

    int getQueueSize() {
        return queue.size();
    }

    int getDynamicMaxPerTick() {
        return dynamicMaxPerTick;
    }

    int getPeakQueueSize() {
        return peakQueueSize.get();
    }

    double getLastWindowDropRate() {
        return lastWindowDropRate;
    }

    double getMaxWindowDropRate() {
        return maxWindowDropRate;
    }

    double getWarnDropRateThreshold() {
        return warnDropRateThreshold;
    }

    double getCumulativeDropRate() {
        long submitted = submittedCount.get();
        if (submitted <= 0L) {
            return 0.0;
        }
        return droppedCount.get() / (double) submitted;
    }

    double getCumulativeUnitDropRate() {
        long submitted = submittedParticleUnits.get();
        if (submitted <= 0L) {
            return 0.0;
        }
        return droppedParticleUnits.get() / (double) submitted;
    }
}
