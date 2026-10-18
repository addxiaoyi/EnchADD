package net.enchadd.mod;

import net.enchadd.mod.common.ModBalanceRules;
import net.enchadd.mod.common.ModVersion;

public final class EnchADDModCommon {
    private EnchADDModCommon() {}

    public static String status() {
        return "EnchADD mod core 2.1.3; supported=" + ModVersion.MINIMUM + ".." + ModVersion.MAXIMUM
                + "; customWeight=" + ModBalanceRules.weight(0);
    }
}
