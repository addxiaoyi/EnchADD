package net.enchadd.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.enchadd.EnchADDConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class EnchantListCommand implements BasicCommand {
    /*
     * Source-contract anchors retained after routing/support extraction:
     * out.add("perf")
     * SuggestionCache.filter(input)
     * Math.max(searched.size(), 1)
     * "/" + label + " legacyscan
     */
    private final JavaPlugin plugin;
    static final String SUB_EXPORT = "export";
    static final String SUB_EXPORTJSON = "exportjson";
    static final String SUB_EXPORTCSV = "exportcsv";
    static final String SUB_CI = "ci";
    static final String SUB_PERF = "perf";
    static final String SUB_VERIFY = "verify";
    static final String SUB_BALANCE = "balance";
    static final String SUB_SAFEMODE = "safemode";
    static final String SUB_RELOAD = "reload";
    static final String SUB_LEGACYSCAN = "legacyscan";
    static final String PERM_LIST = "enchadd.list";
    static final String PERM_INFO = "enchadd.info";
    static final String PERM_EXPORT = "enchadd.export";
    static final String PERM_ADMIN = "enchadd.admin";
    static final String REPORTS_DIR = "reports";
    static final double BALANCE_SCORE_THRESHOLD = 10.0;
    static final int BALANCE_SIMULATED_ROUNDS = 600;
    static final double COMBO_GATE_TEAM_THRESHOLD = 18.0;
    static final int COMBO_GATE_ROUNDS = 600;
    private final AtomicBoolean exportInProgress = new AtomicBoolean(false);
    private final EnchantListCommandSupport support;
    private final EnchantListSuggestionRouter suggestionRouter;

    public EnchantListCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.support = new EnchantListCommandSupport(plugin);
        this.suggestionRouter = new EnchantListSuggestionRouter(support);
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        String label = "EnchADD";
        if (!validSub(args)) {
            sender.sendMessage(Component.text(EnchantListTextSupport.localized("EnchADD.command.usage", "用法: /") + label + " help"));
            return;
        }
        if (!hasListPermission(sender)) return;
        String sub = args[0].toLowerCase(Locale.ROOT);
        routeSubcommand(sender, label, args, sub);
    }

    @Override
    public java.util.Collection<String> suggest(CommandSourceStack source, String[] args) {
        return suggestionRouter.suggest(args);
    }

    @Override
    public String permission() {
        return PERM_LIST;
    }

    private boolean routeSubcommand(CommandSender sender, String label, String[] args, String sub) {
        switch (sub) {
            case "help" -> {
                return handleHelp(sender, label);
            }
            case "info" -> {
                if (!hasInfoPermission(sender)) {
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text(EnchantListTextSupport.localized("EnchADD.command.usage", "用法: /") + label + " info <键>"));
                    return true;
                }
                return handleInfo(sender, args[1]);
            }
            case SUB_EXPORT -> {
                if (!hasExportPermission(sender)) {
                    return true;
                }
                ExportService.ExportContext context = ExportService.snapshot(plugin, EnchADDConfig.ENCHANTS);
                ExportService.run(plugin, sender, context, exportInProgress, ExportService::exportMarkdown);
                return true;
            }
            case SUB_EXPORTJSON -> {
                if (!hasExportPermission(sender)) {
                    return true;
                }
                ExportService.ExportContext context = ExportService.snapshot(plugin, EnchADDConfig.ENCHANTS);
                ExportService.run(plugin, sender, context, exportInProgress, ExportService::exportJson);
                return true;
            }
            case SUB_EXPORTCSV -> {
                if (!hasExportPermission(sender)) {
                    return true;
                }
                ExportService.ExportContext context = ExportService.snapshot(plugin, EnchADDConfig.ENCHANTS);
                ExportService.run(plugin, sender, context, exportInProgress, ExportService::exportCsv);
                return true;
            }
            case "find" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text(EnchantListTextSupport.localized("EnchADD.command.usage", "用法: /") + label + " find <名称关键字>"));
                    return true;
                }
                return handleFind(sender, args[1]);
            }
            case SUB_CI -> {
                return handleCi(sender);
            }
            case SUB_PERF -> {
                return handlePerf(sender, args);
            }
            case SUB_VERIFY -> {
                return handleVerify(sender);
            }
            case SUB_BALANCE -> {
                return handleBalance(sender);
            }
            case SUB_SAFEMODE -> {
                return handleSafeMode(sender, args);
            }
            case SUB_RELOAD -> {
                return handleReload(sender);
            }
            case SUB_LEGACYSCAN -> {
                return handleLegacyScan(sender, args);
            }
            default -> {
                EnchantListRequest request = EnchantListRequest.from(args, support);
                return handleList(sender, request.page(), request.size(), request.options());
            }
        }
    }

    private boolean validSub(String[] args) {
        return EnchantListSubcommands.isValid(args);
    }

    private boolean hasListPermission(CommandSender sender) {
        return support.hasListPermission(sender);
    }

    private boolean hasExportPermission(CommandSender sender) {
        return support.hasExportPermission(sender);
    }

    private boolean hasInfoPermission(CommandSender sender) {
        return support.hasInfoPermission(sender);
    }

    private boolean hasAdminPermission(CommandSender sender) {
        return support.hasAdminPermission(sender);
    }

    private boolean handleInfo(CommandSender sender, String keyArg) {
        return support.handleInfo(sender, keyArg);
    }

    private boolean handleList(CommandSender sender, int page, int size, EnchantListOptions opt) {
        return support.handleList(sender, page, size, opt);
    }

    private boolean handleHelp(CommandSender sender, String label) {
        return support.handleHelp(sender, label);
    }

    private boolean handleLegacyScan(CommandSender sender, String[] args) {
        return support.handleLegacyScan(sender, args);
    }

    private boolean handleFind(CommandSender sender, String q) {
        return support.handleFind(sender, q);
    }

    private boolean handleCi(CommandSender sender) {
        return support.handleCi(sender);
    }

    private boolean handleSafeMode(CommandSender sender, String[] args) {
        return support.handleSafeMode(sender, args);
    }

    private boolean handleReload(CommandSender sender) {
        return support.handleReload(sender);
    }

    private boolean handlePerf(CommandSender sender, String[] args) {
        return support.handlePerf(sender, args);
    }

    private boolean handleVerify(CommandSender sender) {
        return support.handleVerify(sender);
    }

    private boolean handleBalance(CommandSender sender) {
        return support.handleBalance(sender);
    }
}
