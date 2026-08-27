package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantListQuerySupportTest {

    private final EnchantListQuerySupport support = new EnchantListQuerySupport();

    @Test
    void detectsSupportedLanguageArgumentCaseInsensitively() {
        assertTrue(support.hasLangArg(new String[] {"list", "ZH"}));
        assertTrue(support.hasLangArg(new String[] {"list", "en"}));
        assertFalse(support.hasLangArg(new String[] {"list", "jp"}));
        assertFalse(support.hasLangArg(new String[] {"list"}));
    }

    @Test
    void resolvesPageSizeWithAndWithoutLanguageArgument() {
        assertArrayEquals(new int[] {3, 25}, support.resolvePageSize(new String[] {"list", "3", "25"}, false));
        assertArrayEquals(new int[] {3, 25}, support.resolvePageSize(new String[] {"list", "zh", "3", "25"}, true));
        assertArrayEquals(new int[] {1, 10}, support.resolvePageSize(new String[] {"list", "bad", "nope"}, false));
    }
}
