package net.enchadd.commands;

import net.enchadd.EnchADDConfig;
import net.enchadd.legacy.LegacyEnchantStats;
import net.enchadd.utils.EnchantExecutionBudgetManager;
import net.enchadd.utils.EnchantStats;
import net.enchadd.utils.LangManager;
import net.enchadd.utils.ParticleQueue;
import net.enchadd.utils.RuntimeErrorTracker;
import net.enchadd.utils.RuntimeHealthMonitor;
import net.enchadd.utils.SafetyModeManager;
import net.kyori.adventure.key.Key;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.management.ManagementFactory;
import java.util.Locale;
import java.util.function.Function;

final class CiStatusComposer {

    private CiStatusComposer() {
    }

    static String compose(JavaPlugin plugin,
                          Function<String, String> resolveCiEnchantName,
                          Function<String, Boolean> translationReadyForVerify) {
        CiStatusSnapshot snapshot = CiStatusSnapshot.capture(plugin, resolveCiEnchantName, translationReadyForVerify);
        return CiStatusFormatter.format(snapshot);
    }
}
