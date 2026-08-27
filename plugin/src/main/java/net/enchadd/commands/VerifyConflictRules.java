package net.enchadd.commands;

import net.enchadd.EnchADDConfig;
import net.kyori.adventure.key.Key;

final class VerifyConflictRules {

    private VerifyConflictRules() {
    }

    static void run(VerifySupport.VerifyTally tally) {
        check(tally, "ward~barrier", Key.key("enchadd:ward"), Key.key("enchadd:barrier"), true);
        check(tally, "frostbrand~fire_aspect", Key.key("enchadd:frostbrand"), Key.key("minecraft:fire_aspect"), true);
        check(tally, "volley~farshot", Key.key("enchadd:volley"), Key.key("enchadd:farshot"), true);
        check(tally, "cadence~highground", Key.key("enchadd:cadence"), Key.key("enchadd:highground"), false);
        check(tally, "initiative~shadowstrike", Key.key("enchadd:initiative"), Key.key("enchadd:shadowstrike"), true);
        check(tally, "airbag~wingguard", Key.key("enchadd:airbag"), Key.key("enchadd:wingguard"), true);
        check(tally, "parry~riposte", Key.key("enchadd:parry"), Key.key("enchadd:riposte"), true);
        check(tally, "steady_aim~stillness", Key.key("enchadd:steady_aim"), Key.key("enchadd:stillness"), true);
        check(tally, "self ward", Key.key("enchadd:ward"), Key.key("enchadd:ward"), false);
        check(tally, "self sunder", Key.key("enchadd:sunder"), Key.key("enchadd:sunder"), false);
    }

    private static void check(VerifySupport.VerifyTally tally, String label, Key first, Key second, boolean expected) {
        tally.conflictChecks++;
        boolean actual = EnchADDConfig.areIncompatible(first, second);
        VerifySupport.passOrFail(actual == expected, (expected ? "conflict missing " : "unexpected conflict ") + label, tally);
    }
}
