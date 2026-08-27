package net.enchadd.commands;

import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.*;
import net.kyori.adventure.key.Key;

final class VerifyDamageRules {

    private VerifyDamageRules() {
    }

    static void run(VerifySupport.VerifyTally tally) {
        tally.damageChecks++;
        VerifySupport.passOrFail(checkCadence(), "cadence bonus formula invalid", tally);

        tally.damageChecks++;
        VerifySupport.passOrFail(checkSunderChance(), "sunder chance clamp invalid", tally);

        tally.damageChecks++;
        VerifySupport.passOrFail(checkSunderBonus(), "sunder bonus clamp invalid", tally);

        tally.damageChecks++;
        VerifySupport.passOrFail(checkBreakguard(), "breakguard bonus formula invalid", tally);

        tally.damageChecks++;
        VerifySupport.passOrFail(checkHighground(), "highground threshold/bonus invalid", tally);

        tally.damageChecks++;
        VerifySupport.passOrFail(checkUnderdog(), "underdog threshold/bonus invalid", tally);

        tally.damageChecks++;
        VerifySupport.passOrFail(checkInitiative(), "initiative threshold/bonus invalid", tally);

        tally.damageChecks++;
        VerifySupport.passOrFail(checkShadowstrike(), "shadowstrike bonus invalid", tally);

        tally.damageChecks++;
        VerifySupport.passOrFail(checkMeteor(), "meteor threshold/bonus invalid", tally);

        tally.damageChecks++;
        VerifySupport.passOrFail(checkHemorrhage(), "hemorrhage chance/effect invalid", tally);

        tally.damageChecks++;
        VerifySupport.passOrFail(checkMortalWound(), "mortal_wound chance/effect invalid", tally);

        tally.damageChecks++;
        VerifySupport.passOrFail(checkOverwhelm(), "overwhelm weakness invalid", tally);
    }

    private static boolean checkCadence() {
        CadenceEnchant enchant = asEnchant(Key.key("enchadd:cadence"), CadenceEnchant.class);
        if (enchant == null) return false;
        double bonus = Math.min(enchant.getMaxBonusDamage(), enchant.getBonusDamagePerLevel() * 3.0);
        return bonus > 0.0 && bonus <= enchant.getMaxBonusDamage() + 1.0E-9;
    }

    private static boolean checkSunderChance() {
        SunderEnchant enchant = asEnchant(Key.key("enchadd:sunder"), SunderEnchant.class);
        if (enchant == null) return false;
        double chance = Math.min(enchant.getMaxTriggerChance(), enchant.getTriggerChance() * 3.0);
        return chance >= 0.0 && chance <= 1.0 + 1.0E-9 && chance <= enchant.getMaxTriggerChance() + 1.0E-9;
    }

    private static boolean checkSunderBonus() {
        SunderEnchant enchant = asEnchant(Key.key("enchadd:sunder"), SunderEnchant.class);
        if (enchant == null) return false;
        double bonus = Math.min(enchant.getMaxBonusMultiplier(), 20.0 * enchant.getBonusPerArmorPointPerLevel() * 3.0);
        return bonus >= 0.0 && bonus <= enchant.getMaxBonusMultiplier() + 1.0E-9;
    }

    private static boolean checkBreakguard() {
        BreakguardEnchant enchant = asEnchant(Key.key("enchadd:breakguard"), BreakguardEnchant.class);
        if (enchant == null) return false;
        double bonus = Math.min(enchant.getMaxBonusDamage(), enchant.getBonusDamagePerLevel() * 3.0);
        return bonus > 0.0 && bonus <= enchant.getMaxBonusDamage() + 1.0E-9;
    }

    private static boolean checkHighground() {
        HighgroundEnchant enchant = asEnchant(Key.key("enchadd:highground"), HighgroundEnchant.class);
        if (enchant == null) return false;
        double bonus = Math.min(enchant.getMaxBonusDamage(), enchant.getBonusDamagePerLevel() * 3.0);
        return enchant.getRequiredHeightAdvantage() > 0.0 && bonus > 0.0;
    }

    private static boolean checkUnderdog() {
        UnderdogEnchant enchant = asEnchant(Key.key("enchadd:underdog"), UnderdogEnchant.class);
        if (enchant == null) return false;
        double bonus = Math.min(enchant.getMaxBonusDamage(), enchant.getBonusDamagePerLevel() * 3.0);
        return enchant.getRequiredHealthGap() > 0.0 && bonus > 0.0 && bonus <= enchant.getMaxBonusDamage() + 1.0E-9;
    }

    private static boolean checkInitiative() {
        InitiativeEnchant enchant = asEnchant(Key.key("enchadd:initiative"), InitiativeEnchant.class);
        if (enchant == null) return false;
        double bonus = Math.min(enchant.getMaxBonusDamage(), enchant.getBonusDamagePerLevel() * 3.0);
        return enchant.getRequiredTargetHealthFraction() > 0.0
                && enchant.getRequiredTargetHealthFraction() <= 1.0
                && bonus > 0.0
                && bonus <= enchant.getMaxBonusDamage() + 1.0E-9;
    }

    private static boolean checkShadowstrike() {
        ShadowstrikeEnchant enchant = asEnchant(Key.key("enchadd:shadowstrike"), ShadowstrikeEnchant.class);
        return enchant != null && enchant.getBonusDamagePerLevel() * 3.0 > 0.0;
    }

    private static boolean checkMeteor() {
        MeteorEnchant enchant = asEnchant(Key.key("enchadd:meteor"), MeteorEnchant.class);
        if (enchant == null) return false;
        double bonus = Math.min(enchant.getMaxBonusDamage(), enchant.getBonusDamagePerLevel() * 3.0);
        return enchant.getRequiredFallDistance() > 0.0 && bonus > 0.0;
    }

    private static boolean checkHemorrhage() {
        HemorrhageEnchant enchant = asEnchant(Key.key("enchadd:hemorrhage"), HemorrhageEnchant.class);
        return enchant != null
                && enchant.getTriggerChance() > 0.0
                && enchant.getTriggerChance() <= 1.0
                && enchant.getWitherSecondsPerLevel() > 0
                && enchant.getWitherAmplifier() >= 0;
    }

    private static boolean checkMortalWound() {
        MortalWoundEnchant enchant = asEnchant(Key.key("enchadd:mortal_wound"), MortalWoundEnchant.class);
        return enchant != null
                && enchant.getTriggerChance() > 0.0
                && enchant.getTriggerChance() <= 1.0
                && enchant.getAntiHealSecondsPerLevel() > 0
                && enchant.getAntiHealScale() > 0.0
                && enchant.getAntiHealScale() <= 1.0 + 1.0E-9;
    }

    private static boolean checkOverwhelm() {
        OverwhelmEnchant enchant = asEnchant(Key.key("enchadd:overwhelm"), OverwhelmEnchant.class);
        return enchant != null && enchant.getWeaknessSecondsPerLevel() > 0 && enchant.getWeaknessAmplifier() >= 0;
    }

    private static <T extends EnchADDEnchant> T asEnchant(Key key, Class<T> type) {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(key);
        if (!type.isInstance(enchantObj)) {
            return null;
        }
        return type.cast(enchantObj);
    }
}
