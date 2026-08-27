package net.enchadd;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.enchadd.enchants.AbstractEnchADDEnchant;
import net.enchadd.utils.EnchantCache;
import net.enchadd.utils.LangManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LangAndCacheBehaviorTest {

    @AfterEach
    void tearDown() throws Exception {
        LangManager.shutdown();
        clearLangTranslations();
        setActiveLang("zh");
        EnchantCache.clear();
    }

    @Test
    void langManagerFallsBackToEnglishAndSupportsLegacyCurseNames() throws Exception {
        clearLangTranslations();

        Path pluginDataDir = Files.createTempDirectory("enchadd-lang-tests");
        Path languagesDir = pluginDataDir.resolve("languages");
        Files.createDirectories(languagesDir);
        Files.writeString(
                languagesDir.resolve("enchadd_en_US.properties"),
                String.join(
                        "\n",
                        "custom.only.en=Only EN value",
                        "EnchADD.enchant.legacy_probe=Legacy Probe Name",
                        "EnchADD.enchant.prefer_custom=Legacy Should Lose",
                        "EnchADD.enchant.prefer_custom_curse=Custom Curse Name",
                        ""
                ),
                StandardCharsets.UTF_8
        );

        LangManager.init(pluginDataDir, "en");
        setActiveLang("zh");

        assertEquals("Only EN value", LangManager.get("custom.only.en"));
        assertEquals("missing.translation.key", LangManager.get("missing.translation.key"));

        ProbeCurseEnchant legacyOnly = new ProbeCurseEnchant(Key.key("enchadd:legacy_probe_curse"));
        assertEquals("Legacy Probe Name", legacyOnly.getDescriptionText());

        ProbeCurseEnchant preferCustom = new ProbeCurseEnchant(Key.key("enchadd:prefer_custom_curse"));
        assertEquals("Custom Curse Name", preferCustom.getDescriptionText());
        assertEquals("Custom Curse Name", LangManager.get("enchantment.enchadd.prefer_custom_curse"));
    }

    @Test
    void enchantCacheReturnsCachedValueUntilInvalidateIsCalled() {
        EnchantCache.clear();

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        assertEquals(1, EnchantCache.getLevel(sword, Enchantment.SHARPNESS));

        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        assertEquals(1, EnchantCache.getLevel(sword, Enchantment.SHARPNESS));

        EnchantCache.invalidate(sword);
        assertEquals(3, EnchantCache.getLevel(sword, Enchantment.SHARPNESS));
    }

    @Test
    void langManagerInitIsSafeWhenCalledMultipleTimes() throws Exception {
        clearLangTranslations();
        Path pluginDataDir = Files.createTempDirectory("enchadd-lang-init-twice");

        LangManager.init(pluginDataDir, "zh");
        LangManager.init(pluginDataDir, "zh");

        assertEquals("灵魂绑定", LangManager.get("EnchADD.enchant.soulbound"));
    }

    @Test
    void langManagerReloadRefreshesGlobalTranslatorValues() throws Exception {
        clearLangTranslations();
        Path pluginDataDir = Files.createTempDirectory("enchadd-lang-reload");
        Path languagesDir = pluginDataDir.resolve("languages");
        Files.createDirectories(languagesDir);

        Path enFile = languagesDir.resolve("enchadd_en_US.properties");
        Files.writeString(enFile, "custom.reload.value=Value One\n", StandardCharsets.UTF_8);
        LangManager.init(pluginDataDir, "en");

        String first = PlainTextComponentSerializer.plainText().serialize(
                GlobalTranslator.render(Component.translatable("custom.reload.value"), Locale.US)
        );
        assertEquals("Value One", first);

        Files.writeString(enFile, "custom.reload.value=Value Two\n", StandardCharsets.UTF_8);
        LangManager.reload(pluginDataDir);

        String second = PlainTextComponentSerializer.plainText().serialize(
                GlobalTranslator.render(Component.translatable("custom.reload.value"), Locale.US)
        );
        assertEquals("Value Two", second);
    }

    @SuppressWarnings("unchecked")
    private static void clearLangTranslations() throws Exception {
        Field field = LangManager.class.getDeclaredField("translations");
        field.setAccessible(true);
        ((Map<String, String>) field.get(null)).clear();
    }

    private static void setActiveLang(String lang) throws Exception {
        Field field = LangManager.class.getDeclaredField("activeLang");
        field.setAccessible(true);
        field.set(null, lang);
    }

    private static final class ProbeCurseEnchant extends AbstractEnchADDEnchant {

        private ProbeCurseEnchant(Key key) {
            super(
                    key,
                    1,
                    1,
                    1,
                    EnchantmentRegistryEntry.EnchantmentCost.of(1, 0),
                    EnchantmentRegistryEntry.EnchantmentCost.of(2, 0),
                    Collections.<TagKey<Enchantment>>emptyList(),
                    Collections.<TagEntry<ItemType>>emptyList(),
                    true,
                    "COMMON"
            );
        }

        @Override
        public Iterable<EquipmentSlotGroup> getActiveSlots() {
            return List.of(EquipmentSlotGroup.ARMOR);
        }
    }
}
