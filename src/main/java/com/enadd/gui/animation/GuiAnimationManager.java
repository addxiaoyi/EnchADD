package com.enadd.gui.animation;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import com.enadd.enchantments.Rarity;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;


public final class GuiAnimationManager {

    private final int ANIMATION_INTERVAL_TICKS = 5;
    private static final int MAX_FRAMES = 20;
    private static final long DURATION_MS = 2000L;

    private JavaPlugin plugin;
    private final Map<UUID, AnimationSession> activeAnimations = new ConcurrentHashMap<>();

    private final Map<Rarity, List<String>> rarityColors = new HashMap<>();

    private static final String[] SPARKLE_CHARS = {"✨", "⭐", "💫", "🌟", "✦", "✧"};
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
    private static final Map<String, String> COLOR_CACHE = new ConcurrentHashMap<>();

    public GuiAnimationManager(JavaPlugin plugin) {
        this.plugin = plugin;
        initializeRarityColors();
    }

    private void initializeRarityColors() {
        rarityColors.put(Rarity.LEGENDARY, Arrays.asList("GOLD", "YELLOW", "ORANGE"));
        rarityColors.put(Rarity.EPIC, Arrays.asList("PURPLE", "MAGENTA", "BLUE"));
        rarityColors.put(Rarity.RARE, Arrays.asList("BLUE", "CYAN", "LIGHT_BLUE"));
        rarityColors.put(Rarity.UNCOMMON, Arrays.asList("GREEN", "LIME", "CYAN"));
        rarityColors.put(Rarity.COMMON, Arrays.asList("WHITE", "GRAY", "SILVER"));
    }

    @SuppressWarnings("unused")
    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        activeAnimations.entrySet().removeIf(entry -> {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                return true;
            }

            AnimationSession session = entry.getValue();
            if (currentTime - session.startTime > DURATION_MS) {
                return true;
            }

            session.incrementFrame();
            applyAnimationEffect(player, session);

            return false;
        });
    }

    private void applyAnimationEffect(Player player, AnimationSession session) {
        switch (session.getAnimationType()) {
            case PULSE -> applyPulseEffect(player, session);
            case RAINBOW -> applyRainbowEffect(player, session);
            case SPARKLE -> applySparkleEffect(player, session);
        }
    }

    private void applyPulseEffect(Player player, AnimationSession session) {
        ItemStack item = player.getOpenInventory().getTopInventory().getItem(session.getSlot());
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        float pulseIntensity = calculatePulseIntensity(session.getFrame());
        int alpha = (int) (128 + 127 * pulseIntensity);

        String color = getPulseColor(alpha);

        String currentName = com.enadd.util.ItemMetaHelper.getDisplayNameAsString(meta);
        if (currentName != null && !currentName.startsWith("§")) {
            com.enadd.util.ItemMetaHelper.setDisplayName(meta, color + currentName);
        }

        item.setItemMeta(meta);
    }

    private String getPulseColor(int alpha) {
        return COLOR_CACHE.computeIfAbsent("pulse_" + alpha, k -> {
            int r = (alpha / 16) % 16;
            int g = alpha % 16;
            return String.format("&x&%c&%c&%c&%c&%c&%c",
                    HEX_CHARS[r], HEX_CHARS[g], HEX_CHARS[r], HEX_CHARS[g], HEX_CHARS[r], HEX_CHARS[g]);
        });
    }

    private void applyRainbowEffect(Player player, AnimationSession session) {
        ItemStack item = player.getOpenInventory().getTopInventory().getItem(session.getSlot());
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int hue = (session.getFrame() * 15) % 360;
        int rgb = java.awt.Color.HSBtoRGB(hue / 360f, 1f, 1f) & 0xFFFFFF;
        String color = "§x§" + String.format("#%06X", rgb).replace("", "§").substring(1);

        String currentName = com.enadd.util.ItemMetaHelper.getDisplayNameAsString(meta);
        if (currentName != null && currentName.length() > 2) {
            com.enadd.util.ItemMetaHelper.setDisplayName(meta, color + currentName.substring(2));
            item.setItemMeta(meta);
        }
    }

    private void applySparkleEffect(Player player, AnimationSession session) {
        if (session.getFrame() % 3 != 0) {
            return;
        }

        ItemStack item = player.getOpenInventory().getTopInventory().getItem(session.getSlot());
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = com.enadd.util.ItemMetaHelper.hasLore(meta) ?
            com.enadd.util.ItemMetaHelper.getLoreAsStrings(meta) : new ArrayList<>();
        if (lore == null) {
            lore = new ArrayList<>(1);
        }

        String sparkle = getRandomSparkle();
        lore.add(0, sparkle);

        if (lore.size() > 5) {
            lore = lore.subList(0, 5);
        }

        com.enadd.util.ItemMetaHelper.setLore(meta, lore);
        item.setItemMeta(meta);
    }

    private float calculatePulseIntensity(int frame) {
        return (float) Math.sin(frame * Math.PI / MAX_FRAMES);
    }

    private String getRandomSparkle() {
        return "§e" + SPARKLE_CHARS[ThreadLocalRandom.current().nextInt(SPARKLE_CHARS.length)];
    }

    public void startAnimation(Player player, int slot, AnimationType type) {
        if (player == null || !player.isOnline()) {
            return;
        }

        activeAnimations.put(player.getUniqueId(),
                new AnimationSession(slot, type, System.currentTimeMillis()));
    }

    public void stopAnimation(Player player) {
        activeAnimations.remove(player.getUniqueId());
    }

    public boolean hasActiveAnimation(Player player) {
        return activeAnimations.containsKey(player.getUniqueId());
    }

    @SuppressWarnings("deprecation")
    public void animateRarity(ItemStack item, Rarity rarity, int frame) {
        List<String> colors = rarityColors.get(rarity);
        if (colors == null || colors.isEmpty()) {
            return;
        }

        int colorIndex = frame % colors.size();
        String colorName = colors.get(colorIndex);

        Material targetMaterial = getMaterialFromColor(colorName);
        if (targetMaterial != null) {
            item.setType(targetMaterial);
        }
    }

    private Material getMaterialFromColor(String colorName) {
        try {
            return Material.valueOf(colorName + "_STAINED_GLASS_PANE");
        } catch (Exception e) {
            return Material.GLASS_PANE;
        }
    }

    public enum AnimationType {
        PULSE,
        RAINBOW,
        SPARKLE
    }

    public enum Rarity {
        LEGENDARY,
        EPIC,
        RARE,
        UNCOMMON,
        COMMON
    }

    private static class AnimationSession {
        private final int slot;
        private final AnimationType animationType;
        private final long startTime;
        private int frame;

        AnimationSession(int slot, AnimationType animationType, long startTime) {
            this.slot = slot;
            this.animationType = animationType;
            this.startTime = startTime;
            this.frame = 0;
        }

        public int getSlot() {
            return slot;
        }

        public AnimationType getAnimationType() {
            return animationType;
        }

        public int getFrame() {
            return frame;
        }

        public void incrementFrame() {
            frame++;
        }
    }
}
