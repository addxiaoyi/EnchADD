package net.enchadd.commands;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.utils.LegacyEnchantReport;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;


final class LegacyScanService {

    private LegacyScanService() {
    }

    static boolean handle(JavaPlugin plugin, CommandSender sender, String[] args) {
        Registry<org.bukkit.enchantments.Enchantment> enchantmentRegistry =
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        LegacyScanRequest request = LegacyScanRequest.from(args);

        int playersScanned = 0;
        int inventoriesScanned = 0;
        int totalHits = 0;
        List<String> lines = new ArrayList<>();
        lines.add(LegacyScanLineComposer.begin(request));
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!request.filter().isBlank() && !player.getName().toLowerCase(java.util.Locale.ROOT).contains(request.filter())) {
                continue;
            }
            playersScanned++;
            List<LegacyEnchantReport.LegacyEnchantHit> hits = new ArrayList<>();
            hits.addAll(LegacyEnchantReport.scanInventory(enchantmentRegistry, "player_inventory", player.getInventory()));
            inventoriesScanned++;
            InventoryViewHolderUtil.collectOpenInventories(player).forEach(inventory -> {
                if (inventory != null && inventory != player.getInventory()) {
                    hits.addAll(LegacyEnchantReport.scanInventory(enchantmentRegistry, "open_inventory", inventory));
                }
            });
            totalHits += hits.size();
            if (!hits.isEmpty()) {
                lines.add(LegacyScanLineComposer.playerSummary(player.getName(), hits));
                if (!request.summaryOnly()) {
                    for (LegacyEnchantReport.LegacyEnchantHit hit : hits) {
                        lines.add(LegacyScanLineComposer.hit(hit));
                    }
                }
            }
        }
        lines.add(LegacyScanLineComposer.done(playersScanned, inventoriesScanned, totalHits));
        for (String line : lines) {
            plugin.getLogger().info(line);
            sender.sendMessage(net.kyori.adventure.text.Component.text(line));
        }
        return true;
    }
}
