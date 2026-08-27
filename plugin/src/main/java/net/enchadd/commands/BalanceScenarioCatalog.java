package net.enchadd.commands;

import net.kyori.adventure.key.Key;

import java.util.List;

final class BalanceScenarioCatalog {

    private static final List<Key[]> COMBAT_PAIRS = List.of(
            pair("enchadd:cadence", "enchadd:highground"),
            pair("enchadd:cadence", "enchadd:sunder"),
            pair("enchadd:underdog", "enchadd:sunder"),
            pair("enchadd:breakguard", "enchadd:sunder"),
            pair("enchadd:initiative", "enchadd:cadence"),
            pair("enchadd:initiative", "enchadd:highground"),
            pair("enchadd:initiative", "enchadd:sunder"),
            pair("enchadd:shadowstrike", "enchadd:cadence"),
            pair("enchadd:shadowstrike", "enchadd:highground"),
            pair("enchadd:shadowstrike", "enchadd:sunder"),
            pair("enchadd:meteor", "enchadd:highground"),
            pair("enchadd:meteor", "enchadd:cadence"),
            pair("enchadd:meteor", "enchadd:sunder"),
            pair("enchadd:overwhelm", "enchadd:sunder"),
            pair("enchadd:overwhelm", "enchadd:breakguard"),
            pair("enchadd:overwhelm", "enchadd:cadence"),
            pair("enchadd:hemorrhage", "enchadd:cadence"),
            pair("enchadd:hemorrhage", "enchadd:sunder"),
            pair("enchadd:hemorrhage", "enchadd:highground"),
            pair("enchadd:mortal_wound", "enchadd:farshot"),
            pair("enchadd:mortal_wound", "enchadd:steady_aim"),
            pair("enchadd:mortal_wound", "enchadd:stillness"),
            pair("enchadd:farshot", "enchadd:steady_aim"),
            pair("enchadd:farshot", "enchadd:tracer"),
            pair("enchadd:tracer", "enchadd:pursuit"),
            pair("enchadd:pursuit", "enchadd:highground"),
            pair("enchadd:wingclip", "enchadd:initiative"),
            pair("enchadd:wingclip", "enchadd:shadowstrike"),
            pair("enchadd:breakguard", "enchadd:initiative"),
            pair("enchadd:underdog", "enchadd:highground")
    );

    private static final List<Key[][]> COMBO_MATRIX = List.of(
            scenario(pair("enchadd:cadence", "enchadd:breakguard"), pair("enchadd:highground", "enchadd:sunder")),
            scenario(pair("enchadd:initiative", "enchadd:wingclip"), pair("enchadd:shadowstrike", "enchadd:cadence")),
            scenario(pair("enchadd:meteor", "enchadd:overwhelm"), pair("enchadd:hemorrhage", "enchadd:sunder")),
            scenario(pair("enchadd:farshot", "enchadd:tracer"), pair("enchadd:pursuit", "enchadd:mortal_wound")),
            scenario(pair("enchadd:underdog", "enchadd:initiative"), pair("enchadd:highground", "enchadd:cadence")),
            scenario(pair("enchadd:breakguard", "enchadd:underdog"), pair("enchadd:sunder", "enchadd:hemorrhage"))
    );

    private BalanceScenarioCatalog() {
    }

    static List<Key[]> combatPairs() {
        return COMBAT_PAIRS;
    }

    static List<Key[][]> comboMatrix() {
        return COMBO_MATRIX;
    }

    private static Key[] pair(String first, String second) {
        return new Key[] {Key.key(first), Key.key(second)};
    }

    private static Key[][] scenario(Key[] firstTeam, Key[] secondTeam) {
        return new Key[][] {firstTeam, secondTeam};
    }
}
