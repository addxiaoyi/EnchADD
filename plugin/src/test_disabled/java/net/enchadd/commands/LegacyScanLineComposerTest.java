package net.enchadd.commands;

import net.enchadd.utils.LegacyEnchantReport;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyScanLineComposerTest {

    @Test
    void beginUsesRequestDisplayFilterAndSummaryFlag() {
        LegacyScanRequest request = new LegacyScanRequest("", false);

        assertEquals("[ENCHADD-LEGACYSCAN] begin filter=* summaryOnly=false", LegacyScanLineComposer.begin(request));
    }

    @Test
    void playerSummaryAndHitLinesStayStable() {
        LegacyEnchantReport.LegacyEnchantHit hit = hit();

        assertEquals("[ENCHADD-LEGACYSCAN] player=Alice hits=1 migrate=1 remove=0", LegacyScanLineComposer.playerSummary("Alice", List.of(hit)));
        assertEquals("  slot=4 container=player_inventory material=minecraft:diamond_sword legacy=enchadd:legacy target=enchadd:new level=3", LegacyScanLineComposer.hit(hit));
    }

    @Test
    void doneUsesStableMachineReadableFormat() {
        String line = LegacyScanLineComposer.done(2, 3, 4);

        assertTrue(Pattern.compile("^\\[ENCHADD-LEGACYSCAN] done players=2 inventories=3 hits=4 legacyKeys=\\d+$").matcher(line).matches(), line);
    }

    private static LegacyEnchantReport.LegacyEnchantHit hit() {
        return new LegacyEnchantReport.LegacyEnchantHit(
                "player_inventory",
                4,
                "minecraft:diamond_sword",
                "enchadd:legacy",
                "enchadd:new",
                3
        );
    }
}
