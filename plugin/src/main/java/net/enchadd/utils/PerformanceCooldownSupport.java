package net.enchadd.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

final class PerformanceCooldownSupport {

    private static final long NANOS_PER_MILLI = 1_000_000L;
    private static final long MILLIS_PER_TICK = 50L;
    private static final long MAX_WINDOW_NANOS = 86_400L * 1_000_000_000L;

    private PerformanceCooldownSupport() {
    }

    static boolean isOnCooldown(@NotNull PersistentDataContainer pdc,
                                @NotNull NamespacedKey key,
                                long cooldownTicks) {
        if (cooldownTicks <= 0) {
            return false;
        }

        Long lastNano = pdc.get(key, PersistentDataType.LONG);
        if (lastNano == null) {
            return false;
        }

        long now = System.nanoTime();
        if (lastNano > now) {
            pdc.remove(key);
            return false;
        }
        long cooldownNanos = nanosForTicks(cooldownTicks);
        long elapsedNanos = now - lastNano;
        return elapsedNanos < cooldownNanos;
    }

    static void setCooldown(@NotNull PersistentDataContainer pdc,
                            @NotNull NamespacedKey key) {
        pdc.set(key, PersistentDataType.LONG, System.nanoTime());
    }

    static long getRemainingCooldown(@NotNull PersistentDataContainer pdc,
                                     @NotNull NamespacedKey key,
                                     long cooldownTicks) {
        if (cooldownTicks <= 0) {
            return 0L;
        }

        Long lastNano = pdc.get(key, PersistentDataType.LONG);
        if (lastNano == null) {
            return 0L;
        }

        long now = System.nanoTime();
        if (lastNano > now) {
            pdc.remove(key);
            return 0L;
        }
        long cooldownNanos = nanosForTicks(cooldownTicks);
        long elapsedNanos = now - lastNano;
        long remainingNanos = cooldownNanos - elapsedNanos;
        return Math.max(0L, remainingNanos / NANOS_PER_MILLI);
    }

    static void setWindowUntilTicks(@NotNull PersistentDataContainer pdc,
                                    @NotNull NamespacedKey key,
                                    int ticks) {
        long until = saturatingAdd(System.nanoTime(), nanosForTicks(ticks));
        pdc.set(key, PersistentDataType.LONG, until);
    }

    static void setWindowUntilSeconds(@NotNull PersistentDataContainer pdc,
                                      @NotNull NamespacedKey key,
                                      int seconds) {
        long until = saturatingAdd(System.nanoTime(), nanosForSeconds(seconds));
        pdc.set(key, PersistentDataType.LONG, until);
    }

    static boolean isWindowActive(@NotNull PersistentDataContainer pdc,
                                  @NotNull NamespacedKey key) {
        Long until = pdc.get(key, PersistentDataType.LONG);
        if (until == null) {
            return false;
        }
        long now = System.nanoTime();
        if (until <= now || until - now > MAX_WINDOW_NANOS) {
            pdc.remove(key);
            return false;
        }
        return true;
    }

    private static long nanosForTicks(long ticks) {
        return saturatingMultiply(Math.max(0L, ticks), MILLIS_PER_TICK * NANOS_PER_MILLI);
    }

    private static long nanosForSeconds(long seconds) {
        return saturatingMultiply(Math.max(0L, seconds), 1_000_000_000L);
    }

    private static long saturatingMultiply(long left, long right) {
        if (left == 0 || right == 0) {
            return 0L;
        }
        return left > Long.MAX_VALUE / right ? Long.MAX_VALUE : left * right;
    }

    private static long saturatingAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }
}
