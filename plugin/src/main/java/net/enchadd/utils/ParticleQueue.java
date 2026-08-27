package net.enchadd.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 优化4：粒子效果队列（Visual Effect Buffering）
 *
 * 保留兼容门面，同时把运行时状态、动态预算与调度逻辑拆到独立支撑类。
 */
public final class ParticleQueue {

    private static final ParticleQueueRuntime RUNTIME = new ParticleQueueRuntime();
    private static final String DYNAMIC_MAX_PER_TICK_ANCHOR = "dynamicMaxPerTick";
    private static final String WARN_DROP_RATE_THRESHOLD_ANCHOR = "warnDropRateThreshold";

    private ParticleQueue() {}

    public static void start(JavaPlugin plugin, int maxPerTick) {
        int normalizedBase = Math.max(1, maxPerTick);
        int normalizedMin = Math.max(1, normalizedBase / 2);
        int normalizedMax = Math.max(normalizedBase, normalizedBase * 4);
        start(plugin, normalizedBase, normalizedMin, normalizedMax, 0.05);
    }

    public static void start(JavaPlugin plugin,
                             int basePerTick,
                             int minPerTick,
                             int maxPerTick,
                             double warnDropRate) {
        RUNTIME.start(plugin, basePerTick, minPerTick, maxPerTick, warnDropRate);
    }

    public static void stop() {
        RUNTIME.stop();
    }

    public static void submit(World world, Location location, Particle particle,
                               int count, double offsetX, double offsetY, double offsetZ, double speed) {
        RUNTIME.submit(world, location, particle, count, offsetX, offsetY, offsetZ, speed);
    }

    public static void submit(Location location, Particle particle, int count) {
        RUNTIME.submit(location, particle, count);
    }

    public static int getDroppedCount() {
        return RUNTIME.getDroppedCount();
    }

    public static long getSubmittedCount() {
        return RUNTIME.getSubmittedCount();
    }

    public static long getDroppedParticleUnits() {
        return RUNTIME.getDroppedParticleUnits();
    }

    public static long getSubmittedParticleUnits() {
        return RUNTIME.getSubmittedParticleUnits();
    }

    public static long getSuppressedCount() {
        return RUNTIME.getSuppressedCount();
    }

    public static long getSuppressedParticleUnits() {
        return RUNTIME.getSuppressedParticleUnits();
    }

    public static int getQueueSize() {
        return RUNTIME.getQueueSize();
    }

    public static int getCurrentMaxPerTick() {
        return getDynamicMaxPerTick();
    }

    public static int getPeakQueueSize() {
        return RUNTIME.getPeakQueueSize();
    }

    public static double getLastWindowDropRate() {
        return RUNTIME.getLastWindowDropRate();
    }

    public static double getMaxWindowDropRate() {
        return RUNTIME.getMaxWindowDropRate();
    }

    public static double getCumulativeDropRate() {
        return RUNTIME.getCumulativeDropRate();
    }

    public static double getCumulativeUnitDropRate() {
        return RUNTIME.getCumulativeUnitDropRate();
    }

    private static void flush() {
        RUNTIME.flush();
    }

    private static void adaptThroughput() {
        RUNTIME.adaptThroughput();
    }

    private static void emitDropWarningIfNeeded() {
        RUNTIME.emitDropWarningIfNeeded();
    }

    private static int getDynamicMaxPerTick() {
        return RUNTIME.getDynamicMaxPerTick();
    }

    private static double getWarnDropRateThreshold() {
        return RUNTIME.getWarnDropRateThreshold();
    }
}
