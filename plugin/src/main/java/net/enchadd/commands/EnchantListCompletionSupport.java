package net.enchadd.commands;

import net.enchadd.utils.SuggestionCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class EnchantListCompletionSupport {

    List<String> suggestRoot() {
        List<String> out = new ArrayList<>();
        out.add("list");
        out.add("export");
        out.add("info");
        out.add("exportjson");
        out.add("find");
        out.add("help");
        out.add("exportcsv");
        out.add("ci");
        out.add("perf");
        out.add("verify");
        out.add("balance");
        out.add("safemode");
        out.add("reload");
        out.add("legacyscan");
        return out;
    }

    List<String> suggestLang() {
        List<String> out = new ArrayList<>();
        out.add("zh");
        out.add("en");
        return out;
    }

    List<String> suggestPerf(String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 2) {
            out.add("1200");
            out.add("2000");
            out.add("5000");
            return out;
        }
        if (args.length == 3) {
            out.add("400");
            out.add("800");
            out.add("1200");
            return out;
        }
        return out;
    }

    List<String> suggestSafeMode(String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 2) {
            out.add("status");
            out.add("on");
            out.add("off");
            out.add("toggle");
            return out;
        }
        return out;
    }

    List<String> completeInfo(String input) {
        return new ArrayList<>(SuggestionCache.filter(input));
    }

    List<String> completeList(String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 2) {
            out.addAll(suggestLang());
            out.addAll(suggestListPresets());
            out.addAll(suggestListPageSize());
            return out;
        }
        if (args.length == 3 || args.length == 4) {
            out.addAll(suggestListPageSize());
            out.addAll(suggestListPresets());
            return dedupe(out);
        }
        String last = args[args.length - 1];
        String key = optionKey(last);
        switch (key) {
            case "slot" -> {
                out.add("slot=HEAD");
                out.add("slot=CHEST");
                out.add("slot=LEGS");
                out.add("slot=FEET");
                out.add("slot=MAINHAND");
                out.add("slot=OFFHAND");
                return out;
            }
            case "source", "tier" -> {
                out.addAll(suggestSourceTiers());
                return out;
            }
            case "sort" -> {
                out.add("sort=name");
                out.add("sort=weight");
                out.add("sort=max");
                return out;
            }
            case "order" -> {
                out.add("order=asc");
                out.add("order=desc");
                return out;
            }
            case "minweight" -> {
                out.add("minWeight=0");
                out.add("minWeight=1");
                out.add("minWeight=2");
                return out;
            }
            case "maxweight" -> {
                out.add("maxWeight=1");
                out.add("maxWeight=2");
                out.add("maxWeight=3");
                return out;
            }
            case "minlevel" -> {
                out.add("minLevel=1");
                out.add("minLevel=2");
                return out;
            }
            case "maxlevel" -> {
                out.add("maxLevel=3");
                out.add("maxLevel=4");
                return out;
            }
            case "namespace" -> {
                out.add("namespace=EnchADD");
                return out;
            }
            case "keywords" -> {
                out.add("keywords=斩首,吸血");
                out.add("keywords=夜视,收割");
                return out;
            }
            default -> {
                out.add("sort=name");
                out.add("sort=weight");
                out.add("sort=max");
                out.add("sort=weight order=asc slot=MAINHAND");
                out.add("sort=name order=asc");
                out.add("sort=max order=desc");
                out.add("order=asc");
                out.add("order=desc");
                out.add("slot=HEAD");
                out.add("slot=CHEST");
                out.add("slot=LEGS");
                out.add("slot=FEET");
                out.add("slot=MAINHAND");
                out.add("slot=OFFHAND");
                out.add("source=table_common");
                out.add("source=treasure_only");
                out.add("minWeight=0");
                out.add("maxWeight=3");
                out.add("minLevel=1");
                out.add("maxLevel=5");
                out.add("namespace=EnchADD");
                out.add("keywords=斩首,吸血");
                out.addAll(suggestListPresets());
                return dedupe(out);
            }
        }
    }

    private List<String> suggestListPageSize() {
        List<String> out = new ArrayList<>();
        out.add("1");
        out.add("10");
        out.add("20");
        return out;
    }

    private List<String> suggestListPresets() {
        List<String> out = new ArrayList<>();
        out.add("sort=weight order=asc slot=MAINHAND");
        out.add("sort=name order=asc");
        out.add("sort=max order=desc");
        out.add("source=table_common");
        return out;
    }

    private List<String> suggestSourceTiers() {
        List<String> out = new ArrayList<>();
        for (String value : EnchantListDisplayMetadataSupport.sourceTierFilterValues()) {
            out.add("source=" + value);
        }
        return out;
    }

    private List<String> dedupe(List<String> suggestions) {
        List<String> deduped = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (!deduped.contains(suggestion)) {
                deduped.add(suggestion);
            }
        }
        return deduped;
    }

    private String optionKey(String last) {
        String key = last;
        int eq = last.indexOf('=');
        if (eq > 0) key = last.substring(0, eq);
        return key.toLowerCase(Locale.ROOT);
    }
}
