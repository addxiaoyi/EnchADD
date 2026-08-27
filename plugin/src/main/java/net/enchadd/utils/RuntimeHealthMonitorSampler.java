package net.enchadd.utils;

import org.bukkit.plugin.java.JavaPlugin;

final class RuntimeHealthMonitorSampler {

    private RuntimeHealthMonitorSampler() {
    }

    static void sample(JavaPlugin plugin, RuntimeHealthMonitorState state, long intervalTicks) {
        state.incrementSampleCount();
        RuntimeHealthSample sample = RuntimeHealthSample.capture(plugin, state, intervalTicks);
        RuntimeHealthAlertDecision decision = RuntimeHealthAlertPolicy.evaluate(sample);

        SafetyModeManager.onRuntimeSample(decision.alertTriggered(), decision.reason());
        if (!decision.alertTriggered()) {
            return;
        }

        state.incrementAlertCount();
        plugin.getLogger().warning(RuntimeHealthAlertFormatter.format(sample, decision));
    }
}
