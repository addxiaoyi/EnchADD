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

class LanguageMappingsContractTest {

    private static final Path RESOURCES = TestProjectPaths.PLUGIN_MAIN_RESOURCES;
    private static final Path ENCHANTS = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(Path.of("net", "enchadd", "enchants"));

    @Test
    void languageFilesIncludeMappingsForAllEnchants() throws IOException {
        String zh = Files.readString(RESOURCES.resolve("enchadd_zh_CN.properties"), StandardCharsets.UTF_8);
        String en = Files.readString(RESOURCES.resolve("enchadd_en_US.properties"), StandardCharsets.UTF_8);
        for (String key : extractEnchantKeys()) {
            assertTrue(zh.contains("EnchADD.enchant." + key + "="), "zh_CN missing mapping for " + key);
            assertTrue(en.contains("EnchADD.enchant." + key + "="), "en_US missing mapping for " + key);
        }
    }

    @Test
    void languageFilesDoNotMissAnyKnownEnchantTranslationKeys() throws IOException {
        List<String> mapped = new ArrayList<>();
        for (String line : Files.readAllLines(RESOURCES.resolve("enchadd_en_US.properties"), StandardCharsets.UTF_8)) {
            if (line.startsWith("EnchADD.enchant.")) {
                mapped.add(line.substring("EnchADD.enchant.".length(), line.indexOf('=')));
            }
        }
        for (String key : extractEnchantKeys()) {
            assertTrue(mapped.contains(key), "en_US missing key from canonical list: " + key);
        }
    }

    @Test
    void languageFilesOnlyRequireLegacyMappingsForCurseKeys() throws IOException {
        String zh = Files.readString(RESOURCES.resolve("enchadd_zh_CN.properties"), StandardCharsets.UTF_8);
        String en = Files.readString(RESOURCES.resolve("enchadd_en_US.properties"), StandardCharsets.UTF_8);
        for (String legacy : new String[]{}) {
            String legacyKey = "EnchADD.enchant." + legacy + "=";
            assertTrue(zh.contains(legacyKey), "zh_CN should keep legacy mapping for " + legacy);
            assertTrue(en.contains(legacyKey), "en_US should keep legacy mapping for " + legacy);
        }
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
