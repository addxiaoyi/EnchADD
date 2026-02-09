package com.enadd.enchantments.decorative;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * 装饰性附魔的粒子效果配置管理
 */
public final class ParticleEffectConfig {

    private final JavaPlugin plugin;
    private final File configFile;
    private FileConfiguration config;
    private final Map<String, EffectSettings> effectSettings;

    public ParticleEffectConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "decorative_effects.yml");
        this.effectSettings = new HashMap<>();
        reload();
    }

    public void reload() {
        if (!configFile.exists()) {
            plugin.saveResource("decorative_effects.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
        loadSettings();
    }

    private void loadSettings() {
        effectSettings.clear();
        for (DecorativeEnchantmentType type : DecorativeEnchantmentType.values()) {
            String path = "effects." + type.getId();
            boolean enabled = config.getBoolean(path + ".enabled", true);
            double chance = config.getDouble(path + ".chance", 1.0);
            int count = config.getInt(path + ".count", 10);

            effectSettings.put(type.getId(), new EffectSettings(enabled, chance, count));
        }
    }

    public EffectSettings getEffectConfig(String typeId) {
        return effectSettings.getOrDefault(typeId, new EffectSettings(true, 1.0, 10));
    }

    public static class EffectSettings {
        private final boolean enabled;
        private final double chance;
        private final int count;

        public EffectSettings(boolean enabled, double chance, int count) {
            this.enabled = enabled;
            this.chance = chance;
            this.count = count;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public double getChance() {
            return chance;
        }

        public int getCount() {
            return count;
        }
    }
}
