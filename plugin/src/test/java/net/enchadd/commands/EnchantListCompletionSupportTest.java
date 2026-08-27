package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantListCompletionSupportTest {

    @Test
    void completeListSuggestsRecommendedPresets() {
        EnchantListCompletionSupport support = new EnchantListCompletionSupport();

        List<String> suggestions = support.completeList(new String[] {"list", ""});

        assertTrue(suggestions.contains("sort=weight order=asc slot=MAINHAND"));
        assertTrue(suggestions.contains("sort=name order=asc"));
        assertTrue(suggestions.contains("zh"));
        assertTrue(suggestions.contains("10"));
    }

    @Test
    void completeListSuggestsRebasedWeightFilters() {
        EnchantListCompletionSupport support = new EnchantListCompletionSupport();

        List<String> suggestions = support.completeList(new String[] {"list", "1", "20", "sort=weight", "maxWeight="});

        assertTrue(suggestions.contains("maxWeight=1"));
        assertTrue(suggestions.contains("maxWeight=3"));
    }

    @Test
    void completeListSuggestsSourceTierFilters() {
        EnchantListCompletionSupport support = new EnchantListCompletionSupport();

        List<String> suggestions = support.completeList(new String[] {"list", "1", "20", "sort=weight", "source="});

        assertTrue(suggestions.contains("source=table_common"));
        assertTrue(suggestions.contains("source=treasure_only"));
        assertTrue(suggestions.contains("source=curse"));
    }
}
