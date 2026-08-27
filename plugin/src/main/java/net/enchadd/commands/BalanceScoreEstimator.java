package net.enchadd.commands;

import net.enchadd.enchants.BreakguardEnchant;
import net.enchadd.enchants.CadenceEnchant;
import net.enchadd.enchants.ChanceEnchant;
import net.enchadd.enchants.CooldownEnchant;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.HemorrhageEnchant;
import net.enchadd.enchants.HighgroundEnchant;
import net.enchadd.enchants.InitiativeEnchant;
import net.enchadd.enchants.MeteorEnchant;
import net.enchadd.enchants.MortalWoundEnchant;
import net.enchadd.enchants.OverwhelmEnchant;
import net.enchadd.enchants.ShadowstrikeEnchant;
import net.enchadd.enchants.SunderEnchant;
import net.enchadd.enchants.UnderdogEnchant;
import net.kyori.adventure.key.Key;

final class BalanceScoreEstimator {

    private BalanceScoreEstimator() {
    }

    static double estimateBurstScore(EnchADDEnchant enchant) {
        if (enchant == null) {
            return 0.0;
        }
        if (enchant instanceof CadenceEnchant cadence) {
            return cadence.getMaxBonusDamage();
        }
        if (enchant instanceof HighgroundEnchant highground) {
            return highground.getMaxBonusDamage();
        }
        if (enchant instanceof BreakguardEnchant breakguard) {
            return breakguard.getMaxBonusDamage();
        }
        if (enchant instanceof UnderdogEnchant underdog) {
            return underdog.getMaxBonusDamage();
        }
        if (enchant instanceof InitiativeEnchant initiative) {
            return initiative.getMaxBonusDamage();
        }
        if (enchant instanceof SunderEnchant sunder) {
            return 8.0 * sunder.getMaxBonusMultiplier();
        }
        if (enchant instanceof ShadowstrikeEnchant shadowstrike) {
            return Math.max(0.0, shadowstrike.getBonusDamagePerLevel() * shadowstrike.getMaxLevel());
        }
        if (enchant instanceof MeteorEnchant meteor) {
            return meteor.getMaxBonusDamage();
        }
        if (enchant instanceof OverwhelmEnchant overwhelm) {
            return overwhelm.getWeaknessSecondsPerLevel() * (1.0 + overwhelm.getWeaknessAmplifier() * 0.5);
        }
        if (enchant instanceof HemorrhageEnchant hemorrhage) {
            return hemorrhage.getTriggerChance() * hemorrhage.getMaxLevel() * (0.8 + hemorrhage.getWitherSecondsPerLevel() * 0.25);
        }
        if (enchant instanceof MortalWoundEnchant mortalWound) {
            return mortalWound.getTriggerChance() * mortalWound.getMaxLevel() * (1.0 + mortalWound.getAntiHealScale());
        }
        if (enchant instanceof ChanceEnchant chance) {
            return Math.max(0.5, chance.getTriggerChance() * enchant.getMaxLevel() * 1.2);
        }
        if (enchant instanceof CooldownEnchant cooldown) {
            return Math.max(0.5, enchant.getMaxLevel() * 1.0 / Math.max(1.0, cooldown.getCooldownTicks() / 20.0));
        }
        return Math.max(0.4, enchant.getMaxLevel() * 0.6);
    }

    static double estimateTeamBurstScore(Key[] team) {
        if (team == null) {
            return 0.0;
        }
        double score = 0.0;
        for (Key key : team) {
            if (key == null) {
                continue;
            }
            score += estimateBurstScore(net.enchadd.EnchADDConfig.ENCHANTS.get(key));
        }
        return score;
    }
}
