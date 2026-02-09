package com.enadd.gui.search;

import com.enadd.gui.EnchantmentGuiManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


/**
 * Handles search input using modern Paper AsyncChatEvent
 * Replaces deprecated AsyncPlayerChatEvent
 */
public final class SearchInputHandler implements Listener {

    private static final Pattern VALID_INPUT_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$");
    private static final int MAX_INPUT_LENGTH = 64;
    private static final long SEARCH_TIMEOUT_MS = 30000L;

    private final JavaPlugin plugin;
    private final EnchantmentGuiManager guiManager;
    private final ConcurrentHashMap<Player, Long> pendingSearches = new ConcurrentHashMap<>();

    public SearchInputHandler(JavaPlugin plugin, EnchantmentGuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        registerEvents();
        startTimeoutChecker();
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (!pendingSearches.containsKey(player)) {
            return;
        }

        long pendingTime = pendingSearches.get(player);
        if (System.currentTimeMillis() - pendingTime > SEARCH_TIMEOUT_MS) {
            pendingSearches.remove(player);
            return;
        }

        event.setCancelled(true);
        pendingSearches.remove(player);

        // Use modern Adventure API to get message content
        String input = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("取消")) {
            player.sendMessage(Component.text("搜索已取消", NamedTextColor.YELLOW));
            reopenGui(player);
            return;
        }

        if (input.length() > MAX_INPUT_LENGTH) {
            player.sendMessage(Component.text("搜索关键词过长，请缩短至 " + MAX_INPUT_LENGTH + " 个字符以内", NamedTextColor.RED));
            reopenGui(player);
            return;
        }

        if (!VALID_INPUT_PATTERN.matcher(input).matches()) {
            player.sendMessage(Component.text("搜索关键词包含无效字符，仅支持字母、数字、下划线和中文", NamedTextColor.RED));
            reopenGui(player);
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                guiManager.setSearchQuery(player, input);
                guiManager.refreshGui(player, 0);
                player.sendMessage(Component.text("搜索完成，找到匹配附魔", NamedTextColor.GREEN));
                player.sendMessage(Component.text("关键词: ", NamedTextColor.GRAY)
                    .append(Component.text(input, NamedTextColor.WHITE)));
            } catch (Exception e) {
                com.enadd.util.ErrorHandler.handleException(null, "Search processing", e);
                player.sendMessage(Component.text("搜索处理时发生错误，请重试", NamedTextColor.RED));
                reopenGui(player);
            }
        });
    }

    private void reopenGui(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                guiManager.openMainGui(player);
            } catch (Exception e) {
                com.enadd.util.ErrorHandler.handleException(null, "GUI reopening", e);
            }
        }, 1L);
    }

    public boolean isPendingSearch(Player player) {
        return pendingSearches.containsKey(player);
    }

    public void startSearch(Player player) {
        if (player != null) {
            pendingSearches.put(player, System.currentTimeMillis());
        }
    }

    public void cancelSearch(Player player) {
        if (player != null) {
            pendingSearches.remove(player);
        }
    }

    private void startTimeoutChecker() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                long currentTime = System.currentTimeMillis();
                pendingSearches.entrySet().removeIf(entry -> {
                    return currentTime - entry.getValue() > SEARCH_TIMEOUT_MS;
                });
            } catch (Exception e) {
                com.enadd.util.ErrorHandler.handleException(null, "Search timeout checker", e);
            }
        }, 600L, 600L);
    }

    /**
     * Cleanup method for plugin shutdown
     */
    public void shutdown() {
        try {
            pendingSearches.clear();
        } catch (Exception e) {
            // Silent cleanup
        }
    }
}
