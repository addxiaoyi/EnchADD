package net.enchadd.utils;

import java.time.OffsetDateTime;
import java.util.Map;

record EnchantStatsSnapshot(Map<String, Long> counts, OffsetDateTime time, boolean shutdown) {
}
