package net.enchadd.commands;

import net.enchadd.enchants.LegacyEnchantCompat;
import net.enchadd.utils.LegacyEnchantReport;

import java.util.List;
import java.util.Locale;

final class LegacyScanLineComposer {

    private LegacyScanLineComposer() {
    }

    static String begin(LegacyScanRequest request) {
        return "[ENCHADD-LEGACYSCAN] begin filter=" + request.displayFilter() + " summaryOnly=" + request.summaryOnly();
    }

    static String playerSummary(String playerName, List<LegacyEnchantReport.LegacyEnchantHit> hits) {
        return "[ENCHADD-LEGACYSCAN] player=" + playerName + " " + LegacyEnchantReport.summarize(hits);
    }

    static String hit(LegacyEnchantReport.LegacyEnchantHit hit) {
        return "  slot=" + hit.slot()
                + " container=" + hit.container()
                + " material=" + hit.material()
                + " legacy=" + hit.legacyKey()
                + " target=" + hit.migrationTarget()
                + " level=" + hit.level();
    }

    static String done(int playersScanned, int inventoriesScanned, int totalHits) {
        return String.format(
                Locale.ROOT,
                "[ENCHADD-LEGACYSCAN] done players=%d inventories=%d hits=%d legacyKeys=%d",
                playersScanned,
                inventoriesScanned,
                totalHits,
                LegacyEnchantCompat.legacyKeys().size()
        );
    }
}
