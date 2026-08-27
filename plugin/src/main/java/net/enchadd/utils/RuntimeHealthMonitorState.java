package net.enchadd.utils;

import java.util.concurrent.atomic.AtomicLong;

final class RuntimeHealthMonitorState {

    private static final double DEFAULT_TPS = 20.0;

    private final AtomicLong sampleCount = new AtomicLong(0L);
    private final AtomicLong alertCount = new AtomicLong(0L);
    private final AtomicLong lastTriggerTotal = new AtomicLong(0L);
    private final AtomicLong lastErrorTotal = new AtomicLong(0L);

    private volatile double lastTps1m = DEFAULT_TPS;
    private volatile double lastTps5m = DEFAULT_TPS;
    private volatile double lastTps15m = DEFAULT_TPS;
    private volatile double lastErrorRatePerMinute = 0.0;
    private volatile long lastTriggerRatePerMinute = 0L;

    void reset(long triggerTotal, long errorTotal) {
        sampleCount.set(0L);
        alertCount.set(0L);
        lastTriggerTotal.set(triggerTotal);
        lastErrorTotal.set(errorTotal);
        lastTps1m = DEFAULT_TPS;
        lastTps5m = DEFAULT_TPS;
        lastTps15m = DEFAULT_TPS;
        lastErrorRatePerMinute = 0.0;
        lastTriggerRatePerMinute = 0L;
    }

    long incrementSampleCount() {
        return sampleCount.incrementAndGet();
    }

    long incrementAlertCount() {
        return alertCount.incrementAndGet();
    }

    long getSampleCount() {
        return sampleCount.get();
    }

    long getAlertCount() {
        return alertCount.get();
    }

    double getLastTps1m() {
        return lastTps1m;
    }

    double getLastTps5m() {
        return lastTps5m;
    }

    double getLastTps15m() {
        return lastTps15m;
    }

    double getLastErrorRatePerMinute() {
        return lastErrorRatePerMinute;
    }

    long getLastTriggerRatePerMinute() {
        return lastTriggerRatePerMinute;
    }

    long takePreviousTriggerTotal(long currentTotal) {
        return lastTriggerTotal.getAndSet(currentTotal);
    }

    long takePreviousErrorTotal(long currentTotal) {
        return lastErrorTotal.getAndSet(currentTotal);
    }

    void updateTps(double tps1m, double tps5m, double tps15m) {
        lastTps1m = tps1m;
        lastTps5m = tps5m;
        lastTps15m = tps15m;
    }

    void updateRates(double errorRatePerMinute, long triggerRatePerMinute) {
        lastErrorRatePerMinute = errorRatePerMinute;
        lastTriggerRatePerMinute = triggerRatePerMinute;
    }
}
