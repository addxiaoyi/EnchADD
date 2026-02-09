package com.enadd.gui.adapter;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;


public final class ResolutionAdapter {

    private static final double DEFAULT_SCALE = 1.0;
    private static final int DEFAULT_WIDTH = 854;
    private static final int DEFAULT_HEIGHT = 480;

    private final ConcurrentHashMap<UUID, Double> playerScales = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> playerScreenWidth = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> playerScreenHeight = new ConcurrentHashMap<>();

    public ResolutionAdapter(JavaPlugin plugin) {
    }

    public double getScale(Player player) {
        return playerScales.getOrDefault(player.getUniqueId(), DEFAULT_SCALE);
    }

    public int getScreenWidth(Player player) {
        return playerScreenWidth.getOrDefault(player.getUniqueId(), DEFAULT_WIDTH);
    }

    public int getScreenHeight(Player player) {
        return playerScreenHeight.getOrDefault(player.getUniqueId(), DEFAULT_HEIGHT);
    }

    public int calculateOptimalFontSize(Player player) {
        double scale = getScale(player);
        int baseSize = 12;
        int adjustedSize = (int) Math.round(baseSize * scale);
        return Math.max(8, Math.min(20, adjustedSize));
    }

    public void updatePlayerResolution(Player player, double scale, int width, int height) {
        playerScales.put(player.getUniqueId(), scale);
        playerScreenWidth.put(player.getUniqueId(), width);
        playerScreenHeight.put(player.getUniqueId(), height);
    }
}
