package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantListAdminSupportTest {

    @Test
    void perfRequestAppliesDefaultsAndBounds() {
        assertEquals(new EnchantListPerfRequest(1200, 600), EnchantListPerfRequest.from(new String[] {"perf"}));
        assertEquals(new EnchantListPerfRequest(1, 0), EnchantListPerfRequest.from(new String[] {"perf", "-5", "-1"}));
        assertEquals(new EnchantListPerfRequest(200_000, 100_000), EnchantListPerfRequest.from(new String[] {"perf", "999999", "999999"}));
        assertEquals(new EnchantListPerfRequest(1200, 600), EnchantListPerfRequest.from(new String[] {"perf", "bad", "bad"}));
    }

    @Test
    void metricSanitizationIsStable() {
        assertEquals("missing", EnchantListAdminIdentitySupport.sanitizeMetricValue(null));
        assertEquals("missing", EnchantListAdminIdentitySupport.sanitizeMetricValue("   "));
        assertEquals("a_b-c", EnchantListAdminIdentitySupport.sanitizeMetricValue("a b=c"));
    }

    @Test
    void translationReadinessRejectsFallbackKeys() {
        assertTrue(EnchantListAdminIdentitySupport.isTranslatedName("安全气囊"));
        assertFalse(EnchantListAdminIdentitySupport.isTranslatedName("EnchADD.enchant.airbag"));
        assertFalse(EnchantListAdminIdentitySupport.isTranslatedName("enchantment.minecraft.sharpness"));
        assertFalse(EnchantListAdminIdentitySupport.isTranslatedName(""));
    }
}
