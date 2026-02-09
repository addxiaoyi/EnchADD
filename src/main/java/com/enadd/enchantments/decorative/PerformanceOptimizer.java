package com.enadd.enchantments.decorative;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * 装饰性附魔的性能优化管理
 */
public final class PerformanceOptimizer {

    private final JavaPlugin plugin;
    private final ParticleEffectConfig config;
    private final Map<UUID, Integer> activeParticles;

    public PerformanceOptimizer(JavaPlugin plugin, ParticleEffectConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.activeParticles = new HashMap<>();
    }

    public boolean shouldRender(Player player) {
        // 简单的性能控制逻辑
        return activeParticles.getOrDefault(player.getUniqueId(), 0) < 100;
    }

    public void trackParticle(Player player, int count) {
        UUID uuid = player.getUniqueId();
        activeParticles.put(uuid, activeParticles.getOrDefault(uuid, 0) + count);

        // 异步清理计数
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            int current = activeParticles.getOrDefault(uuid, 0);
            activeParticles.put(uuid, Math.max(0, current - count));
        }, 20L);
    }
}
