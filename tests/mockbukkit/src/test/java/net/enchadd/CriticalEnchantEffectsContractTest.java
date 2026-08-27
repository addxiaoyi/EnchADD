package net.enchadd;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CriticalEnchantEffectsContractTest {

    private static final Path LISTENERS = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(Path.of("net", "enchadd", "listeners"));
    private static final Path ENCHANTS = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(Path.of("net", "enchadd", "enchants"));

    @Test
    void purifyRemovesNegativeEffectsAndWritesCooldown() throws IOException {
        String source = read("PurifyListener.java");
        assertTrue(source.contains("PlayerItemConsumeEvent"), "Purify should trigger on consume");
        assertTrue(source.contains("PerformanceUtils.isOnCooldown"), "Purify should check cooldown before trigger");
        assertTrue(source.contains("removePotionEffect"), "Purify should remove potion effects");
        assertTrue(source.contains("negatives"), "Purify should maintain negative effect list");
        assertTrue(source.contains("PerformanceUtils.setCooldown"), "Purify should write cooldown after success");
    }

    @Test
    void dispelStripsPositiveEffectsAndWritesCooldown() throws IOException {
        String source = read("DispelListener.java");
        assertTrue(source.contains("EntityDamageByEntityEvent"), "Dispel should trigger on hit");
        assertTrue(source.contains("Math.min(0.95"), "Dispel trigger chance should be capped");
        assertTrue(source.contains("positives"), "Dispel should maintain positive effect list");
        assertTrue(source.contains("removePotionEffect"), "Dispel should remove positive effects");
        assertTrue(source.contains("PerformanceUtils.setCooldown"), "Dispel should write cooldown after success");
    }

    @Test
    void quellReducesMagicAndWitherDamageOnly() throws IOException {
        String source = read("QuellListener.java");
        assertTrue(source.contains("EntityDamageEvent.DamageCause.MAGIC"), "Quell should target magic damage");
        assertTrue(source.contains("EntityDamageEvent.DamageCause.WITHER"), "Quell should target wither damage");
        assertTrue(source.contains("Math.max(0.5"), "Quell minimum damage scale should be clamped");
        assertTrue(source.contains("scale >= 1.0"), "Quell should skip ineffective reductions");
        assertTrue(source.contains("event.setDamage(event.getDamage() * scale)"), "Quell should apply damage scaling");
        assertTrue(source.contains("PerformanceUtils.setCooldown"), "Quell should write cooldown after success");
    }

    @Test
    void volleySpawnsExtraArrowsAndMarksSpawnedProjectiles() throws IOException {
        String source = read("VolleyListener.java");
        assertTrue(source.contains("ProjectileLaunchEvent"), "Volley should trigger on projectile launch");
        assertTrue(source.contains("SpawnReason.ENCHANTMENT"), "Volley spawned arrows should use enchantment spawn reason");
        assertTrue(source.contains("spawnArrowVolley"), "Volley should spawn extra normal arrows");
        assertTrue(source.contains("spawnSpectralVolley"), "Volley should spawn extra spectral arrows");
        assertTrue(source.contains("markVolleyArrow"), "Volley should mark spawned arrows to avoid recursion");
    }

    @Test
    void panicUsesCurseKeyAndRegistersAsCurseEnchant() throws IOException {
        String source = Files.readString(ENCHANTS.resolve("PanicEnchant.java"), StandardCharsets.UTF_8);
        assertTrue(source.contains("Key.key(\"enchadd:panic_curse\")"), "Panic should define its curse key");
        assertTrue(source.contains("getPanicChancePerLevel()"), "Panic should expose panic chance scaling");
        assertTrue(source.contains("#curse"), "Panic should remain tagged as a curse");
        assertTrue(source.contains("super(KEY"), "Panic should inherit common enchant metadata");
    }

    @Test
    void vampirismRefreshesFireExposureDuringSunlight() throws IOException {
        String source = read("VampirismListener.java");
        assertTrue(source.contains("Bukkit.getGlobalRegionScheduler()"), "Vampirism should run on a repeating scheduler");
        assertTrue(source.contains("refreshSunBurn"), "Vampirism should isolate the burn refresh logic");
        assertTrue(source.contains("getLightFromSky"), "Vampirism should require direct sunlight");
        assertTrue(source.contains("setFireTicks"), "Vampirism should refresh fire ticks when exposed");
    }

    @Test
    void descriptionResolutionPrefersMinecraftThenCustomThenLegacyCurseFallback() throws IOException {
        String source = Files.readString(ENCHANTS.resolve("AbstractEnchADDEnchant.java"), StandardCharsets.UTF_8);
        assertTrue(source.contains("getMinecraftTranslationKey()"), "Description resolution should start from the Minecraft key");
        assertTrue(source.contains("getCustomTranslationKey()"), "Description resolution should consult the custom key");
        assertTrue(source.contains("getLegacyCustomTranslationKey()"), "Description resolution should support legacy curse names");
        assertTrue(source.contains("resolveDescriptionFallback()"), "Description resolution should centralize fallback selection");
        assertTrue(source.contains("value.endsWith(\"_curse\")"), "Legacy fallback should only apply to curse keys");
        assertTrue(source.contains("resolveDescriptionFallback()"), "Description resolution should preserve fallback selection");
        assertTrue(source.contains("Component.text(getDescriptionText())"),
                "Description component should emit plain text so clients without resource packs can still read names");
    }

    @Test
    void legacyCurseFallbackDoesNotChangeNonCurseKeys() throws IOException {
        String source = Files.readString(ENCHANTS.resolve("AbstractEnchADDEnchant.java"), StandardCharsets.UTF_8);
        assertTrue(source.contains("return \"EnchADD.enchant.\" + value;"), "Legacy fallback should still return the custom key for non-curse entries");
        assertTrue(source.contains("value = value.substring(0, value.length() - \"_curse\".length());"), "Legacy fallback should strip the curse suffix when present");
        assertTrue(source.contains("String fallback = LangManager.get(getLegacyCustomTranslationKey());"), "Fallback resolution should start from the legacy key");
    }

    @Test
    void immolateAppliesFireAndUsesCooldownRolls() throws IOException {
        String source = read("ImmolateListener.java");
        assertTrue(source.contains("EntityDamageByEntityEvent"), "Immolate should trigger on entity damage");
        assertTrue(source.contains("Math.min(0.6"), "Immolate chance should be capped");
        assertTrue(source.contains("calculateDurationTicksPerLevel"), "Immolate should convert seconds per level into ticks");
        assertTrue(source.contains("setFireTicks"), "Immolate should ignite the victim");
        assertTrue(source.contains("PerformanceUtils.setCooldown"), "Immolate should write cooldown after success");
    }

    private static String read(String fileName) throws IOException {
        return Files.readString(LISTENERS.resolve(fileName), StandardCharsets.UTF_8);
    }

    private static String[] extractEnchantKeys() throws IOException {
        Pattern pattern = Pattern.compile("Key\\.key\\(\\\"enchadd:([^\\\"]+)\\\"\\)");
        List<String> keys = new ArrayList<>();
        try (var stream = Files.list(ENCHANTS)) {
            stream.filter(path -> path.getFileName().toString().endsWith("Enchant.java"))
                    .forEach(path -> {
                        try {
                            Matcher matcher = pattern.matcher(Files.readString(path, StandardCharsets.UTF_8));
                            if (matcher.find()) {
                                keys.add(matcher.group(1));
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        return keys.toArray(String[]::new);
    }
}
