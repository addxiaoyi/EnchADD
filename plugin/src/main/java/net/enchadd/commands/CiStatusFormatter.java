package net.enchadd.commands;

import java.util.Locale;

final class CiStatusFormatter {

    private CiStatusFormatter() {
    }

    static String format(CiStatusSnapshot s) {
        return String.format(
                Locale.ROOT,
                "[ENCHADD-CI] status=%s enchants=%d hasAirbag=%s version=%s activeLang=%s translationReady=%s airbagName=%s braceName=%s airbagCodepoints=%s braceCodepoints=%s totalTriggers=%d triggerRatePerMin=%d particleDropped=%d particleSubmitted=%d particleSuppressed=%d particleDropRate=%.4f particleDroppedUnits=%d particleSubmittedUnits=%d particleSuppressedUnits=%d particleUnitDropRate=%.4f particleWindowDropRate=%.4f particlePeakWindowDropRate=%.4f particleQueue=%d particlePeakQueue=%d particleMaxPerTick=%d statsPending=%d statsDropped=%d runtimeErrors=%d monitorSamples=%d monitorAlerts=%d errorRatePerMin=%.2f tps1m=%.2f tps5m=%.2f tps15m=%.2f safetyModeState=%s safetyModeEnabled=%s safetyModeManual=%s safetyModeAuto=%s safetyModeAlertBurst=%d safetyModeHealthyStreak=%d safetyModeChanceMultiplier=%.2f safetyModeTickModuloMultiplier=%d safetyModeSuppressParticles=%s safetyModeLastChangeEpochMs=%d safetyModeReason=%s budgetDegradedExecutions=%d budgetSkippedExecutions=%d budgetOpenBreakers=%d budgetTrackedEnchants=%d budgetNanosPerTick=%d budgetChanceMultiplier=%.2f budgetTickModuloMultiplier=%d budgetSuppressParticles=%s legacySanitizedTotal=%d legacySanitizedKeys=%s heapUsedMB=%d heapCommittedMB=%d heapMaxMB=%d uptimeSeconds=%d",
                s.status(),
                s.enchants(),
                s.hasAirbag(),
                s.version(),
                CiStatusMetricFormatter.sanitizeMetricValue(s.activeLang()),
                s.translationReady(),
                CiStatusMetricFormatter.sanitizeMetricValue(s.airbagName()),
                CiStatusMetricFormatter.sanitizeMetricValue(s.braceName()),
                s.airbagCodepoints(),
                s.braceCodepoints(),
                s.totalTriggers(),
                s.triggerRatePerMin(),
                s.particleDropped(),
                s.particleSubmitted(),
                s.particleSuppressed(),
                s.particleDropRate(),
                s.particleDroppedUnits(),
                s.particleSubmittedUnits(),
                s.particleSuppressedUnits(),
                s.particleUnitDropRate(),
                s.particleWindowDropRate(),
                s.particlePeakWindowDropRate(),
                s.particleQueueSize(),
                s.particlePeakQueueSize(),
                s.particleMaxPerTick(),
                s.statsPending(),
                s.statsDropped(),
                s.runtimeErrors(),
                s.monitorSamples(),
                s.monitorAlerts(),
                s.errorRatePerMin(),
                s.tps1m(),
                s.tps5m(),
                s.tps15m(),
                CiStatusMetricFormatter.sanitizeMetricValue(s.safetyModeState()),
                s.safetyModeEnabled(),
                s.safetyModeManual(),
                s.safetyModeAuto(),
                s.safetyModeAlertBurst(),
                s.safetyModeHealthyStreak(),
                s.safetyModeChanceMultiplier(),
                s.safetyModeTickModuloMultiplier(),
                s.safetyModeSuppressParticles(),
                s.safetyModeLastChangeEpochMs(),
                CiStatusMetricFormatter.sanitizeMetricValue(s.safetyModeReason()),
                s.budgetDegradedExecutions(),
                s.budgetSkippedExecutions(),
                s.budgetOpenBreakers(),
                s.budgetTrackedEnchants(),
                s.budgetNanosPerTick(),
                s.budgetChanceMultiplier(),
                s.budgetTickModuloMultiplier(),
                s.budgetSuppressParticles(),
                s.legacySanitizedTotal(),
                CiStatusMetricFormatter.sanitizeMetricValue(s.legacySanitizedKeys()),
                s.heapUsedMb(),
                s.heapCommittedMb(),
                s.heapMaxMb(),
                s.uptimeSeconds()
        );
    }
}
