package net.enchadd.commands;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.util.Locale;

final class PerfInjectionMessageComposer {

    private PerfInjectionMessageComposer() {
    }

    static Component noEnchants() {
        return Component.text("[ENCHADD-PERF] 无法执行：当前没有已注册附魔。");
    }

    static Component noWorlds() {
        return Component.text("[ENCHADD-PERF] 当前没有可用世界，仅执行触发计数注入。");
    }

    static Component queued(int triggers, int particles, Key key, int triggerBatchSize, int particleBatchSize) {
        return Component.text(String.format(
                Locale.ROOT,
                "[ENCHADD-PERF] queued triggers=%d particles=%d key=%s batch(trigger=%d,particle=%d)",
                triggers,
                particles,
                key.asString(),
                triggerBatchSize,
                particleBatchSize
        ));
    }

    static String doneStatus(int triggers, int particles, Key key) {
        return String.format(
                Locale.ROOT,
                "[ENCHADD-PERF] triggers=%d particles=%d key=%s",
                triggers,
                particles,
                key.asString()
        );
    }

    static Component done(String status) {
        return Component.text(status);
    }
}
