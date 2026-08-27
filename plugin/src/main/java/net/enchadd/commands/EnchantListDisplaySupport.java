package net.enchadd.commands;

import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.List;

final class EnchantListDisplaySupport {

    boolean handleInfo(CommandSender sender, String keyArg) {
        EnchADDEnchant ench = findEnchantByArg(keyArg);
        if (ench == null) {
            sender.sendMessage(Component.translatable("EnchADD.command.not_found"));
            return true;
        }
        sendLines(sender, EnchantListInfoComposer.compose(ench));
        return true;
    }

    boolean handleFind(CommandSender sender, String q) {
        List<EnchADDEnchant> found = EnchantListSearchSupport.findByName(EnchADDConfig.ENCHANTS.values(), q);
        sendLines(sender, EnchantListResultComposer.composeFindResults(found));
        return true;
    }

    boolean handleList(CommandSender sender, int page, int size, EnchantListOptions opt) {
        List<EnchADDEnchant> results = EnchantListSearchSupport.prepareListResults(EnchADDConfig.ENCHANTS.values(), opt);
        int[] adjusted = EnchantListPaginationSupport.adjustPageSize(page, size, results.size());
        page = adjusted[0];
        size = adjusted[1];
        sendLines(sender, EnchantListResultComposer.composeListResults(results, page, size, adjusted[2] == 1, opt));
        return true;
    }

    boolean handleHelp(CommandSender sender, String label) {
        sendLines(sender, EnchantListHelpComposer.compose(label));
        return true;
    }

    EnchADDEnchant findEnchantByArg(String arg) {
        return EnchantQueryService.findEnchantByArg(EnchADDConfig.ENCHANTS, arg);
    }

    private void sendLines(CommandSender sender, Iterable<Component> lines) {
        for (Component line : lines) {
            sender.sendMessage(line);
        }
    }
}
