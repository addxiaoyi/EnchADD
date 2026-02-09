package com.enadd.core.tracking;

import com.enadd.core.entity.EntityState;
import com.enadd.core.entity.ManagedEntity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public final class EntityTracker {
    // Holder模式优化单例
    private static final class Holder {
        private static final EntityTracker INSTANCE = new EntityTracker();
    }

    // 预分配容量，减少扩容
    private final ConcurrentHashMap<String, EntityRecord> entityRecords = new ConcurrentHashMap<>(128);
    private final CopyOnWriteArrayList<TrackerListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicInteger totalTracked = new AtomicInteger(0);
    private final AtomicInteger totalTransitions = new AtomicInteger(0);

    private volatile boolean loggingEnabled = true;

    private EntityTracker() {}

    public static EntityTracker getInstance() {
        return Holder.INSTANCE;
    }

    public void trackEntity(ManagedEntity entity) {
        String entityId = entity.getEntityId();
        if (entityRecords.containsKey(entityId)) {
            return;
        }

        EntityRecord record = new EntityRecord(entityId, entity.getClass().getSimpleName());
        record.addEntry(new TrackerEntry(
                entity.getState(),
                "ENTITY_CREATED",
                System.currentTimeMillis(),
                null
        ));

        entityRecords.put(entityId, record);
        totalTracked.incrementAndGet();

        notifyListeners(l -> l.onEntityTracked(entityId));
    }

    public void recordTransition(ManagedEntity entity, EntityState oldState, EntityState newState) {
        String entityId = entity.getEntityId();
        EntityRecord record = entityRecords.get(entityId);

        if (record != null) {
            String transition = String.format("%s -> %s", oldState.getDisplayName(), newState.getDisplayName());
            record.addEntry(new TrackerEntry(newState, transition, System.currentTimeMillis(), null));
            totalTransitions.incrementAndGet();
        }

        notifyListeners(l -> l.onStateChanged(entityId, oldState, newState));
    }

    public void recordEvent(String entityId, String eventType, String details) {
        EntityRecord record = entityRecords.get(entityId);
        if (record != null) {
            ManagedEntity entity = record.getEntity();
            if (entity != null) {
                record.addEntry(new TrackerEntry(entity.getState(), eventType, System.currentTimeMillis(), details));
            }
        }
    }

    public EntityRecord getRecord(String entityId) {
        return entityRecords.get(entityId);
    }

    public void untrackEntity(String entityId) {
        EntityRecord record = entityRecords.remove(entityId);
        if (record != null) {
            record.close();
            notifyListeners(l -> l.onEntityUntracked(entityId));
        }
    }

    public void addListener(TrackerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TrackerListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(java.util.function.Consumer<TrackerListener> action) {
        listeners.forEach(action);
    }

    public void setLoggingEnabled(boolean enabled) {
        loggingEnabled = enabled;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void cleanupInactiveEntities(long maxIdleTime) {
        entityRecords.entrySet().removeIf(entry -> {
            ManagedEntity entity = entry.getValue().getEntity();
            return entity != null && entity.getIdleTime() > maxIdleTime;
        });
    }

    public void clearAllRecords() {
        entityRecords.clear();
    }

    public int getTrackedCount() {
        return entityRecords.size();
    }

    public int getTotalTracked() {
        return totalTracked.get();
    }

    public int getTotalTransitions() {
        return totalTransitions.get();
    }

    public TrackerStats getStats() {
        int alive = 0;
        int destroyed = 0;
        int orphaned = 0;

        for (EntityRecord record : entityRecords.values()) {
            ManagedEntity entity = record.getEntity();
            if (entity != null) {
                switch (entity.getState()) {
                    case DESTROYED -> destroyed++;
                    case ORPHAN -> orphaned++;
                    default -> alive++;
                }
            }
        }

        return new TrackerStats(
                totalTracked.get(),
                totalTransitions.get(),
                alive,
                destroyed,
                orphaned,
                entityRecords.size()
        );
    }

    public static final class EntityRecord {
        private final String entityId;
        private final String entityType;
        private final CopyOnWriteArrayList<TrackerEntry> entries = new CopyOnWriteArrayList<>();
        private volatile long lastUpdateTime = System.currentTimeMillis();
        private volatile boolean finalized = false;

        public EntityRecord(String id, String type) {
            this.entityId = id;
            this.entityType = type;
        }

        public void addEntry(TrackerEntry entry) {
            if (!finalized) {
                entries.add(entry);
                lastUpdateTime = System.currentTimeMillis();
            }
        }

        public void close() {
            finalized = true;
        }

        public String getEntityId() { return entityId; }
        public String getEntityType() { return entityType; }
        public java.util.List<TrackerEntry> getEntries() { return entries; }
        public long getLastUpdateTime() { return lastUpdateTime; }
        public boolean isFinalized() { return finalized; }

        public ManagedEntity getEntity() {
            return com.enadd.core.entity.EntityLifecycleManager.getInstance().getEntity(entityId);
        }

        public String getLogSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Entity: %s (%s)\n", entityId, entityType));
            sb.append("State History:\n");
            for (TrackerEntry entry : entries) {
                sb.append(String.format("  [%s] %s: %s\n",
                        entry.timestamp, entry.state, entry.eventType));
                if (entry.details != null) {
                    sb.append(String.format("    Details: %s\n", entry.details));
                }
            }
            return sb.toString();
        }
    }

    public static final class TrackerEntry {
        private final EntityState state;
        private final String eventType;
        private final long timestamp;
        private final String details;

        public TrackerEntry(EntityState state, String eventType, long timestamp, String details) {
            this.state = state;
            this.eventType = eventType;
            this.timestamp = timestamp;
            this.details = details;
        }

        public EntityState getState() { return state; }
        public String getEventType() { return eventType; }
        public long getTimestamp() { return timestamp; }
        public String getDetails() { return details; }

        public String getFormattedTimestamp() {
            return LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(timestamp),
                    java.time.ZoneId.systemDefault()
            ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        }
    }

    public interface TrackerListener {
        void onEntityTracked(String entityId);
        void onEntityUntracked(String entityId);
        void onStateChanged(String entityId, EntityState oldState, EntityState newState);
    }

    public static final class TrackerStats {
        private final int totalTracked;
        private final int totalTransitions;
        private final int aliveCount;
        private final int destroyedCount;
        private final int orphanedCount;
        private final int currentCount;

        public TrackerStats(int total, int transitions, int alive, int destroyed, int orphaned, int current) {
            this.totalTracked = total;
            this.totalTransitions = transitions;
            this.aliveCount = alive;
            this.destroyedCount = destroyed;
            this.orphanedCount = orphaned;
            this.currentCount = current;
        }

        public int getTotalTracked() { return totalTracked; }
        public int getTotalTransitions() { return totalTransitions; }
        public int getAliveCount() { return aliveCount; }
        public int getDestroyedCount() { return destroyedCount; }
        public int getOrphanedCount() { return orphanedCount; }
        public int getCurrentCount() { return currentCount; }
        public float getSurvivalRate() { return totalTracked > 0 ? (float) aliveCount / totalTracked * 100 : 0; }
    }
}
