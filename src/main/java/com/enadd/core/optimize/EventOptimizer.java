package com.enadd.core.optimize;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class EventOptimizer {
    private static final Logger LOGGER = Logger.getLogger(EventOptimizer.class.getName());

    private static final AtomicBoolean ENABLED = new AtomicBoolean(false);
    private static final AtomicInteger MAX_CONCURRENT_EVENTS = new AtomicInteger(100);
    private static final AtomicLong PROCESSED_EVENTS = new AtomicLong(0);
    private static final AtomicLong FAILED_EVENTS = new AtomicLong(0);
    private static final AtomicLong TOTAL_PROCESSING_TIME_MS = new AtomicLong(0);

    private static PriorityBlockingQueue<PrioritizedEvent> eventQueue;
    private static ScheduledExecutorService scheduler;
    private static ExecutorService asyncExecutor;
    private static ConcurrentHashMap<String, EventCounter> eventTypeCounters;

    private static volatile int batchSize = 10;
    private static volatile long batchTimeoutMs = 50L;
    private static volatile boolean asyncMode = true;

    private static final Map<String, Integer> EVENT_PRIORITIES = new ConcurrentHashMap<>();
    private static final Set<String> HIGH_PRIORITY_EVENTS = ConcurrentHashMap.newKeySet();
    private static final Set<String> LOW_PRIORITY_EVENTS = ConcurrentHashMap.newKeySet();

    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 16;
    private static final long KEEP_ALIVE_TIME_MS = 30000L;
    private static final int QUEUE_CAPACITY = 5000;

    private EventOptimizer() {}

    public static synchronized void initialize() {
        if (ENABLED.get()) {
            return;
        }

        eventQueue = new PriorityBlockingQueue<>(QUEUE_CAPACITY, Comparator.comparingInt(PrioritizedEvent::getPriority).reversed());
        eventTypeCounters = new ConcurrentHashMap<>();

        asyncExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME_MS, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY / 2),
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "EventOptimizer-Worker-" + counter++);
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "EventOptimizer-Scheduler");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(EventOptimizer::processBatch,
                                     batchTimeoutMs,
                                     batchTimeoutMs,
                                     TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(EventOptimizer::cleanupCounters,
                                     60000L,
                                     60000L,
                                     TimeUnit.MILLISECONDS);

        registerDefaultPriorities();

        ENABLED.set(true);
        LOGGER.log(Level.INFO, "EventOptimizer initialized with pool size: {0}", MAX_POOL_SIZE);
    }

    private static void registerDefaultPriorities() {
        HIGH_PRIORITY_EVENTS.add("PlayerJoinEvent");
        HIGH_PRIORITY_EVENTS.add("PlayerQuitEvent");
        HIGH_PRIORITY_EVENTS.add("PlayerRespawnEvent");

        LOW_PRIORITY_EVENTS.add("BlockPhysicsEvent");
        LOW_PRIORITY_EVENTS.add("EntitySpawnEvent");
        LOW_PRIORITY_EVENTS.add("ChunkLoadEvent");

        EVENT_PRIORITIES.put("PlayerJoinEvent", 100);
        EVENT_PRIORITIES.put("PlayerQuitEvent", 100);
        EVENT_PRIORITIES.put("PlayerRespawnEvent", 90);
        EVENT_PRIORITIES.put("PlayerInteractEvent", 80);
        EVENT_PRIORITIES.put("PlayerMoveEvent", 75);
        EVENT_PRIORITIES.put("EntityDeathEvent", 70);
        EVENT_PRIORITIES.put("BlockBreakEvent", 65);
        EVENT_PRIORITIES.put("BlockPlaceEvent", 65);
        EVENT_PRIORITIES.put("InventoryClickEvent", 60);
        EVENT_PRIORITIES.put("ChatEvent", 55);
        EVENT_PRIORITIES.put("EntitySpawnEvent", 30);
        EVENT_PRIORITIES.put("BlockPhysicsEvent", 20);
        EVENT_PRIORITIES.put("ChunkLoadEvent", 15);
        EVENT_PRIORITIES.put("ChunkUnloadEvent", 10);
    }

    public static void registerEventType(String eventType, int priority) {
        EVENT_PRIORITIES.put(eventType, Math.max(0, Math.min(100, priority)));

        if (priority >= 70) {
            HIGH_PRIORITY_EVENTS.add(eventType);
            LOW_PRIORITY_EVENTS.remove(eventType);
        } else if (priority <= 30) {
            LOW_PRIORITY_EVENTS.add(eventType);
            HIGH_PRIORITY_EVENTS.remove(eventType);
        }
    }

    public static void submitEvent(String eventType, Runnable handler) {
        if (!ENABLED.get()) {
            handler.run();
            return;
        }

        int priority = getEventPriority(eventType);
        PrioritizedEvent event = new PrioritizedEvent(handler, priority);

        if (asyncMode && !HIGH_PRIORITY_EVENTS.contains(eventType)) {
            if (!eventQueue.offer(event)) {
                FAILED_EVENTS.incrementAndGet();
                handler.run();
            }
        } else {
            handler.run();
        }
    }

    public static void submitHighPriorityEvent(String eventType, Runnable handler) {
        if (!ENABLED.get()) {
            handler.run();
            return;
        }

        int priority = Math.max(100, getEventPriority(eventType) + 10);
        PrioritizedEvent event = new PrioritizedEvent(handler, priority);

        if (!eventQueue.offer(event)) {
            FAILED_EVENTS.incrementAndGet();
            handler.run();
        }
    }

    public static void submitEventAsync(String eventType, Runnable handler) {
        if (!ENABLED.get()) {
            asyncExecutor.execute(handler);
            return;
        }

        int priority = getEventPriority(eventType);
        PrioritizedEvent event = new PrioritizedEvent(handler, priority);

        if (eventQueue.offer(event)) {
            recordEventType(eventType);
        } else {
            FAILED_EVENTS.incrementAndGet();
            asyncExecutor.execute(handler);
        }
    }

    private static int getEventPriority(String eventType) {
        return EVENT_PRIORITIES.getOrDefault(eventType, 50);
    }

    private static void recordEventType(String eventType) {
        eventTypeCounters.computeIfAbsent(eventType, k -> new EventCounter()).increment();
    }

    private static void processBatch() {
        if (!ENABLED.get() || eventQueue == null) return;

        List<PrioritizedEvent> batch = new ArrayList<>(batchSize);

        for (int i = 0; i < batchSize; i++) {
            PrioritizedEvent event = eventQueue.poll();
            if (event == null) break;
            batch.add(event);
        }

        if (batch.isEmpty()) return;

        int currentConcurrent = getCurrentConcurrentEvents();
        if (currentConcurrent >= MAX_CONCURRENT_EVENTS.get()) {
            for (PrioritizedEvent event : batch) {
                eventQueue.offer(event);
            }
            return;
        }

        for (PrioritizedEvent event : batch) {
            long startTime = System.currentTimeMillis();
            try {
                asyncExecutor.execute(() -> {
                    try {
                        event.getHandler().run();
                    } finally {
                        PROCESSED_EVENTS.incrementAndGet();
                    }
                });
                long processingTime = System.currentTimeMillis() - startTime;
                TOTAL_PROCESSING_TIME_MS.addAndGet(processingTime);
            } catch (RejectedExecutionException e) {
                FAILED_EVENTS.incrementAndGet();
                event.getHandler().run();
            }
        }
    }

    private static int getCurrentConcurrentEvents() {
        if (asyncExecutor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) asyncExecutor).getActiveCount();
        }
        return 0;
    }

    private static void cleanupCounters() {
        long oneMinuteAgo = System.currentTimeMillis() - 60000L;

        eventTypeCounters.entrySet().removeIf(entry -> {
            EventCounter counter = entry.getValue();
            return counter.getLastEventTime() < oneMinuteAgo && counter.getCount() < 10;
        });
    }

    public static void setBatchSize(int size) {
        batchSize = Math.max(1, Math.min(100, size));
    }

    public static void setBatchTimeout(long timeoutMs) {
        batchTimeoutMs = Math.max(10L, Math.min(500L, timeoutMs));
    }

    public static void setAsyncMode(boolean async) {
        asyncMode = async;
    }

    public static void setMaxConcurrentEvents(int max) {
        MAX_CONCURRENT_EVENTS.set(Math.max(10, Math.min(500, max)));
    }

    public static EventOptimizerReport getReport() {
        int queueSize = eventQueue != null ? eventQueue.size() : 0;
        int activeThreads = getCurrentConcurrentEvents();
        long processed = PROCESSED_EVENTS.get();
        long failed = FAILED_EVENTS.get();
        double avgProcessingTime = processed > 0 ?
            (double) TOTAL_PROCESSING_TIME_MS.get() / processed : 0;

        Map<String, Long> topEventTypes = new LinkedHashMap<>();
        eventTypeCounters.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue().getCount(), a.getValue().getCount()))
            .limit(10)
            .forEach(entry -> topEventTypes.put(entry.getKey(), entry.getValue().getCount()));

        return new EventOptimizerReport(
            ENABLED.get(),
            queueSize,
            activeThreads,
            processed,
            failed,
            avgProcessingTime,
            batchSize,
            batchTimeoutMs,
            asyncMode,
            MAX_CONCURRENT_EVENTS.get(),
            topEventTypes
        );
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    public static long getProcessedEventCount() {
        return PROCESSED_EVENTS.get();
    }

    public static long getFailedEventCount() {
        return FAILED_EVENTS.get();
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

        if (eventQueue != null) {
            eventQueue.clear();
        }

        eventTypeCounters.clear();
        EVENT_PRIORITIES.clear();
        HIGH_PRIORITY_EVENTS.clear();
        LOW_PRIORITY_EVENTS.clear();

        LOGGER.log(Level.INFO, "EventOptimizer shutdown complete. Processed: {0}, Failed: {1}",
                    new Object[]{PROCESSED_EVENTS.get(), FAILED_EVENTS.get()});
    }

    private static final class PrioritizedEvent {
        private final Runnable handler;
        private final int priority;
        public PrioritizedEvent(Runnable handler, int priority) {
            this.handler = handler;
            this.priority = priority;
        }

        public Runnable getHandler() { return handler; }
        public int getPriority() { return priority; }
    }

    private static final class EventCounter {
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicLong lastEventTime = new AtomicLong(System.currentTimeMillis());

        public void increment() {
            count.incrementAndGet();
            lastEventTime.set(System.currentTimeMillis());
        }

        public long getCount() { return count.get(); }
        public long getLastEventTime() { return lastEventTime.get(); }
    }

    public static final class EventOptimizerReport {
        private final boolean enabled;
        private final int queueSize;
        private final int activeThreads;
        private final long processedEvents;
        private final long failedEvents;
        private final double avgProcessingTimeMs;
        private final int batchSize;
        private final long batchTimeoutMs;
        private final boolean asyncMode;
        private final int maxConcurrentEvents;
        private final Map<String, Long> topEventTypes;

        public EventOptimizerReport(boolean enabled, int queueSize, int activeThreads,
                                   long processedEvents, long failedEvents,
                                   double avgProcessingTimeMs, int batchSize,
                                   long batchTimeoutMs, boolean asyncMode,
                                   int maxConcurrentEvents, Map<String, Long> topEventTypes) {
            this.enabled = enabled;
            this.queueSize = queueSize;
            this.activeThreads = activeThreads;
            this.processedEvents = processedEvents;
            this.failedEvents = failedEvents;
            this.avgProcessingTimeMs = avgProcessingTimeMs;
            this.batchSize = batchSize;
            this.batchTimeoutMs = batchTimeoutMs;
            this.asyncMode = asyncMode;
            this.maxConcurrentEvents = maxConcurrentEvents;
            this.topEventTypes = topEventTypes;
        }

        public boolean isEnabled() { return enabled; }
        public int getQueueSize() { return queueSize; }
        public int getActiveThreads() { return activeThreads; }
        public long getProcessedEvents() { return processedEvents; }
        public long getFailedEvents() { return failedEvents; }
        public double getAvgProcessingTimeMs() { return avgProcessingTimeMs; }
        public int getBatchSize() { return batchSize; }
        public long getBatchTimeoutMs() { return batchTimeoutMs; }
        public boolean isAsyncMode() { return asyncMode; }
        public int getMaxConcurrentEvents() { return maxConcurrentEvents; }
        public Map<String, Long> getTopEventTypes() { return topEventTypes; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Event Optimizer Report ===\n");
            sb.append("Enabled: ").append(enabled).append("\n");
            sb.append("Queue Size: ").append(queueSize).append("\n");
            sb.append("Active Threads: ").append(activeThreads).append("\n");
            sb.append("Processed Events: ").append(processedEvents).append("\n");
            sb.append("Failed Events: ").append(failedEvents).append("\n");
            sb.append("Avg Processing Time: ").append(String.format("%.2fms", avgProcessingTimeMs)).append("\n");
            sb.append("Batch Size: ").append(batchSize).append("\n");
            sb.append("Batch Timeout: ").append(batchTimeoutMs).append("ms\n");
            sb.append("Async Mode: ").append(asyncMode).append("\n");
            sb.append("Max Concurrent: ").append(maxConcurrentEvents).append("\n");
            sb.append("Top Event Types:\n");
            topEventTypes.forEach((type, count) ->
                sb.append("  - ").append(type).append(": ").append(count).append("\n"));
            return sb.toString();
        }
    }
}
