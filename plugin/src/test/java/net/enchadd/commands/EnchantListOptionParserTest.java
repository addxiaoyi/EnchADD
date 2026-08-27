package net.enchadd.commands;

import org.bukkit.inventory.EquipmentSlotGroup;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EnchantListOptionParserTest {

    @Test
    void parsesSupportedOptionsFromOffset() {
        EnchantListOptions options = EnchantListOptionParser.parse(new String[] {
                "list",
                "2",
                "25",
                "sort=WEIGHT",
                "order=DESC",
                "slot=mainhand",
                "source=CURSE",
                "minweight=3",
                "maxweight=12",
                "minlevel=2",
                "maxlevel=5",
                "namespace=enchadd",
                "keywords= Alpha ,,斩首 "
        }, 3);

        assertEquals("weight", options.sort);
        assertEquals("desc", options.order);
        assertEquals(EquipmentSlotGroup.MAINHAND, options.slot);
        assertEquals(EnchantListDisplayMetadataSupport.SourceTier.CURSE_TREASURE, options.sourceTier);
        assertEquals(3, options.minWeight);
        assertEquals(12, options.maxWeight);
        assertEquals(2, options.minLevel);
        assertEquals(5, options.maxLevel);
        assertEquals("enchadd", options.namespace);
        assertEquals(List.of("alpha", "斩首"), options.keywords);
    }

    @Test
    void ignoresUnknownOrInvalidOptionsWithoutBreakingDefaults() {
        EnchantListOptions options = EnchantListOptionParser.parse(new String[] {
                "sort=rarity",
                "order=random",
                "slot=nope",
                "source=nope",
                "minweight=bad",
                "unknown=value"
        }, 0);

        assertEquals("name", options.sort);
        assertEquals("asc", options.order);
        assertNull(options.slot);
        assertNull(options.sourceTier);
        assertNull(options.minWeight);
    }

    @Test
    void negativeOffsetStartsAtFirstArgument() {
        EnchantListOptions options = EnchantListOptionParser.parse(new String[] {"sort=max"}, -10);

        assertEquals("max", options.sort);
    }
}
