package net.enchadd.commands;

import net.enchadd.EnchADDConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

final class EnchantListAdminSupport {

    private final JavaPlugin plugin;
    private final EnchantListAdminIdentitySupport identitySupport;

    EnchantListAdminSupport(JavaPlugin plugin) {
        this.plugin = plugin;
        this.identitySupport = new EnchantListAdminIdentitySupport();
    }

    boolean handleLegacyScan(CommandSender sender, String[] args) {
        return LegacyScanService.handle(plugin, sender, args);
    }

    boolean handleCi(CommandSender sender) {
        String message = CiStatusService.build(plugin, this::resolveCiEnchantName, this::translationReadyForVerify);
        plugin.getLogger().info(message);
        sender.sendMessage(Component.text(message));
        return true;
    }

    boolean handleSafeMode(CommandSender sender, String[] args) {
        return EnchantListSafeModeCommand.handle(plugin, sender, args);
    }

    boolean handleReload(CommandSender sender) {
        return EnchantListReloadCommand.handle(plugin, sender);
    }

    boolean handlePerf(CommandSender sender, String[] args) {
        EnchantListPerfRequest request = EnchantListPerfRequest.from(args);
        return PerfInjectionService.handle(plugin, sender, EnchADDConfig.ENCHANTS, request.triggerCount(), request.particleCount());
    }

    boolean handleVerify(CommandSender sender) {
        String message = VerifyService.run(plugin, this::resolveCiEnchantName, this::translationReadyForVerify);
        sender.sendMessage(Component.text(message));
        return true;
    }

    boolean handleBalance(CommandSender sender) {
        for (String message : BalanceService.run(
                plugin,
                EnchantListCommand.BALANCE_SCORE_THRESHOLD,
                EnchantListCommand.BALANCE_SIMULATED_ROUNDS,
                EnchantListCommand.COMBO_GATE_TEAM_THRESHOLD,
                EnchantListCommand.COMBO_GATE_ROUNDS
        )) {
            sender.sendMessage(Component.text(message));
        }
        return true;
    }

    boolean translationReadyForVerify(String keyArg) {
        return identitySupport.translationReadyForVerify(keyArg);
    }

    String resolveCiEnchantName(String keyArg) {
        return identitySupport.resolveCiEnchantName(keyArg);
    }

    String sanitizeMetricValue(String value) {
        return EnchantListAdminIdentitySupport.sanitizeMetricValue(value);
    }
}
