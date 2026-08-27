package net.enchadd.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantAcquisitionPolicyTest {

    @Test
    void applyAcquisitionPolicyDefaultsScalesAndClassifiesRepresentativeRoutes() {
        YamlConfiguration configuration = new YamlConfiguration();
        ConfigurationSection enchants = configuration.createSection("enchants");
        ConfigurationSection curses = configuration.createSection("curses");

        ConfigurationSection tableCommon = enchants.createSection("telepathy");
        tableCommon.set("weight", 100);
        ConfigurationSection treasureOnly = enchants.createSection("airbag");
        treasureOnly.set("weight", 100);
        ConfigurationSection curse = curses.createSection("panic");
        curse.set("weight", 100);

        boolean changed = EnchantAcquisitionPolicy.applyAcquisitionPolicyDefaults(configuration, enchants, curses);

        assertAll(
                () -> assertTrue(changed, "Policy migration should update an unversioned config"),
                () -> assertEquals(4, configuration.getInt(EnchantAcquisitionPolicy.ACQUISITION_POLICY_VERSION_KEY), "Policy version should be stamped"),
                () -> assertEquals(10, tableCommon.getInt("weight"), "Table-common weight should be reduced"),
                () -> assertEquals(7, treasureOnly.getInt("weight"), "Treasure-only weight should be reduced"),
                () -> assertEquals(9, curse.getInt("weight"), "Curse weight should be reduced"),
                () -> assertTrue(tableCommon.getStringList("enchantmentTags").contains("#in_enchanting_table"), "Table-common enchants should remain table-eligible"),
                () -> assertTrue(tableCommon.getBoolean("canGetFromEnchantingTable"), "Table-common enchants should remain table-eligible"),
                () -> assertTrue(treasureOnly.getStringList("enchantmentTags").contains("#treasure"), "Treasure-only enchants should remain treasure-tagged"),
                () -> assertFalse(treasureOnly.getStringList("enchantmentTags").contains("#in_enchanting_table"), "Treasure-only enchants should not be table-eligible"),
                () -> assertFalse(treasureOnly.getBoolean("canGetFromEnchantingTable"), "Treasure-only enchants should not be table-eligible"),
                () -> assertTrue(curse.getStringList("enchantmentTags").contains("#curse"), "Curse entries should stay marked as curses"),
                () -> assertTrue(curse.getStringList("enchantmentTags").contains("#in_enchanting_table"), "Curse entries should remain table-eligible"),
                () -> assertTrue(curse.getBoolean("canGetFromEnchantingTable"), "Curse entries should remain table-eligible")
        );
    }
}
