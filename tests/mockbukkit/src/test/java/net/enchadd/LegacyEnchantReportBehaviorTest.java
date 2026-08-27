package net.enchadd;

import net.enchadd.utils.LegacyEnchantReport;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyEnchantReportBehaviorTest {

    @Test
    void reportFindsLegacyEnchantHitAndSummarizesIt() {
        @SuppressWarnings("unchecked")
        org.bukkit.Registry<Enchantment> registry = Mockito.mock(org.bukkit.Registry.class);
        Enchantment legacy = Mockito.mock(Enchantment.class);
        ItemStack item = Mockito.mock(ItemStack.class);

        Mockito.when(legacy.getKey()).thenReturn(new NamespacedKey("enchadd", "cloaking"));
        Mockito.when(registry.get(net.kyori.adventure.key.Key.key("enchadd:cloaking"))).thenReturn(legacy);
        Mockito.when(item.getType()).thenReturn(Material.DIAMOND_CHESTPLATE);
        Mockito.when(item.getEnchantments()).thenReturn(Map.of(legacy, 2));

        List<LegacyEnchantReport.LegacyEnchantHit> hits = LegacyEnchantReport.scanItem(registry, "player_inventory", 5, item);

        assertEquals(1, hits.size());
        LegacyEnchantReport.LegacyEnchantHit hit = hits.getFirst();
        assertEquals("player_inventory", hit.container());
        assertEquals(5, hit.slot());
        assertEquals("enchadd:cloaking", hit.legacyKey());
        assertEquals("-", hit.migrationTarget());
        assertEquals(2, hit.level());
        assertTrue(LegacyEnchantReport.summarize(hits).contains("hits=1"));
    }
}
