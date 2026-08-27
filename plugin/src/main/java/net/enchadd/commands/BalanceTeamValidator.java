package net.enchadd.commands;

import net.enchadd.EnchADDConfig;
import net.kyori.adventure.key.Key;

final class BalanceTeamValidator {

    private BalanceTeamValidator() {
    }

    static boolean hasMissingOrConflict(Key[] team) {
        if (team == null || team.length == 0) {
            return true;
        }
        for (int i = 0; i < team.length; i++) {
            if (team[i] == null || EnchADDConfig.ENCHANTS.get(team[i]) == null) {
                return true;
            }
            for (int j = i + 1; j < team.length; j++) {
                if (team[j] == null) {
                    return true;
                }
                if (EnchADDConfig.areIncompatible(team[i], team[j])) {
                    return true;
                }
            }
        }
        return false;
    }
}
