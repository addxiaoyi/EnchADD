package com.enadd.enchantments.decorative;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;



/**
 * 负责在世界中渲染粒子效果
 */
public final class ParticleEffectRenderer {

    private final JavaPlugin plugin;
    private final ParticleEffectConfig config;
    private final Map<UUID, Long> playerLastTrigger;

    public ParticleEffectRenderer(JavaPlugin plugin, ParticleEffectConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.playerLastTrigger = new HashMap<>();
    }

    public boolean canTrigger(Player player) {
        long last = playerLastTrigger.getOrDefault(player.getUniqueId(), 0L);
        return System.currentTimeMillis() - last > 100; // 最小间隔 100ms
    }

    public void recordTrigger(Player player) {
        playerLastTrigger.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void startEffect(Location location, DecorativeEnchantmentType type, Player player) {
        ParticleEffectConfig.EffectSettings settings = config.getEffectConfig(type.getId());
        if (!settings.isEnabled()) return;

        for (ParticleType pType : type.getParticleTypes()) {
            location.getWorld().spawnParticle(
                pType.getBukkitParticle(),
                location.clone().add(0, 1, 0),
                settings.getCount(),
                type.getEffectRadius(),
                type.getEffectRadius(),
                type.getEffectRadius(),
                0.1
            );
        }
    }
}
