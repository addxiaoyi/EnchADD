package net.enchadd.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

final class EnchantListCommandSupport {

    private final EnchantListAdminSupport adminSupport;
    private final EnchantListDisplaySupport displaySupport;
    private final EnchantListQuerySupport querySupport;
    private final EnchantListCompletionSupport completionSupport;
    private final EnchantListPermissionSupport permissionSupport;

    EnchantListCommandSupport(JavaPlugin plugin) {
        this.adminSupport = new EnchantListAdminSupport(plugin);
        this.displaySupport = new EnchantListDisplaySupport();
        this.querySupport = new EnchantListQuerySupport();
        this.completionSupport = new EnchantListCompletionSupport();
        this.permissionSupport = new EnchantListPermissionSupport();
    }

    boolean hasListPermission(CommandSender sender) {
        return permissionSupport.hasPermission(sender, EnchantListCommand.PERM_LIST);
    }

    boolean hasExportPermission(CommandSender sender) {
        return permissionSupport.hasPermission(sender, EnchantListCommand.PERM_EXPORT);
    }

    boolean hasInfoPermission(CommandSender sender) {
        return permissionSupport.hasPermission(sender, EnchantListCommand.PERM_INFO);
    }

    boolean hasAdminPermission(CommandSender sender) {
        return permissionSupport.hasPermission(sender, EnchantListCommand.PERM_ADMIN);
    }

    boolean handleInfo(CommandSender sender, String keyArg) {
        return displaySupport.handleInfo(sender, keyArg);
    }

    boolean handleLegacyScan(CommandSender sender, String[] args) {
        if (!hasAdminPermission(sender)) {
            return true;
        }
        return adminSupport.handleLegacyScan(sender, args);
    }

    boolean handleFind(CommandSender sender, String q) {
        return displaySupport.handleFind(sender, q);
    }

    boolean handleCi(CommandSender sender) {
        if (!hasAdminPermission(sender)) {
            return true;
        }
        return adminSupport.handleCi(sender);
    }

    boolean handleSafeMode(CommandSender sender, String[] args) {
        if (!hasAdminPermission(sender)) {
            return true;
        }
        return adminSupport.handleSafeMode(sender, args);
    }

    boolean handleReload(CommandSender sender) {
        if (!hasAdminPermission(sender)) {
            return true;
        }
        return adminSupport.handleReload(sender);
    }

    boolean handlePerf(CommandSender sender, String[] args) {
        if (!hasAdminPermission(sender)) {
            return true;
        }
        return adminSupport.handlePerf(sender, args);
    }

    boolean handleVerify(CommandSender sender) {
        if (!hasAdminPermission(sender)) {
            return true;
        }
        return adminSupport.handleVerify(sender);
    }

    boolean handleBalance(CommandSender sender) {
        if (!hasAdminPermission(sender)) {
            return true;
        }
        return adminSupport.handleBalance(sender);
    }

    boolean handleList(CommandSender sender, int page, int size, EnchantListOptions opt) {
        return displaySupport.handleList(sender, page, size, opt);
    }

    boolean handleHelp(CommandSender sender, String label) {
        return displaySupport.handleHelp(sender, label);
    }

    boolean hasLangArg(String[] args) {
        return querySupport.hasLangArg(args);
    }

    int[] resolvePageSize(String[] args, boolean hasLang) {
        return querySupport.resolvePageSize(args, hasLang);
    }

    EnchantListOptions parseOptions(String[] args, int fromIdx) {
        return querySupport.parseOptions(args, fromIdx);
    }

    java.util.List<String> suggestRoot() {
        return completionSupport.suggestRoot();
    }

    java.util.List<String> suggestLang() {
        return completionSupport.suggestLang();
    }

    java.util.List<String> suggestPerf(String[] args) {
        return completionSupport.suggestPerf(args);
    }

    java.util.List<String> suggestSafeMode(String[] args) {
        return completionSupport.suggestSafeMode(args);
    }

    java.util.List<String> completeInfo(String input) {
        return completionSupport.completeInfo(input);
    }

    java.util.List<String> completeList(String[] args) {
        return completionSupport.completeList(args);
    }
}
