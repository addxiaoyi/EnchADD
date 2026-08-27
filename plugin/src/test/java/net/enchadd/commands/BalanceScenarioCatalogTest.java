package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BalanceScenarioCatalogTest {

    @Test
    void combatPairCatalogKeepsExpectedSizeAndShape() {
        assertEquals(30, BalanceScenarioCatalog.combatPairs().size());
        assertTrue(BalanceScenarioCatalog.combatPairs().stream().allMatch(pair -> pair.length == 2));
    }

    @Test
    void comboCatalogKeepsExpectedSizeAndShape() {
        assertEquals(6, BalanceScenarioCatalog.comboMatrix().size());
        assertTrue(BalanceScenarioCatalog.comboMatrix().stream()
                .allMatch(scenario -> scenario.length == 2
                        && scenario[0].length == 2
                        && scenario[1].length == 2));
    }
}
