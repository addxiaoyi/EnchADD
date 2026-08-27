package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CiStatusMetricFormatterTest {

    @Test
    void sanitizeMetricValueHandlesMissingWhitespaceAndEquals() {
        assertEquals("missing", CiStatusMetricFormatter.sanitizeMetricValue(null));
        assertEquals("missing", CiStatusMetricFormatter.sanitizeMetricValue("  "));
        assertEquals("alpha_beta-gamma", CiStatusMetricFormatter.sanitizeMetricValue("alpha beta=gamma"));
    }

    @Test
    void encodeMetricCodepointsHandlesMissingTranslationKeysAndUnicode() {
        assertEquals("missing", CiStatusMetricFormatter.encodeMetricCodepoints(null));
        assertEquals("missing", CiStatusMetricFormatter.encodeMetricCodepoints(""));
        assertEquals("missing", CiStatusMetricFormatter.encodeMetricCodepoints("EnchADD.enchant.alpha"));
        assertEquals("missing", CiStatusMetricFormatter.encodeMetricCodepoints("enchantment.minecraft.sharpness"));
        assertEquals("41-5a-4e2d", CiStatusMetricFormatter.encodeMetricCodepoints("AZ中"));
    }
}
