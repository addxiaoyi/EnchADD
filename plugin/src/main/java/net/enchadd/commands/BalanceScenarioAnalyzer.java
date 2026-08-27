package net.enchadd.commands;

import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.kyori.adventure.key.Key;

final class BalanceScenarioAnalyzer {

    private BalanceScenarioAnalyzer() {
    }

    static Result analyzeBurst(double threshold, int simulatedRounds) {
        int pairCount = 0;
        int blockedPairs = 0;
        int violations = 0;
        int scoredPairs = 0;
        double highestScore = 0.0;
        String hotspot = "none";

        for (Key[] pair : BalanceScenarioCatalog.combatPairs()) {
            if (pair.length != 2) {
                continue;
            }
            EnchADDEnchant first = EnchADDConfig.ENCHANTS.get(pair[0]);
            EnchADDEnchant second = EnchADDConfig.ENCHANTS.get(pair[1]);
            if (first == null || second == null) {
                continue;
            }
            pairCount++;
            if (EnchADDConfig.areIncompatible(pair[0], pair[1])) {
                blockedPairs++;
                continue;
            }
            scoredPairs++;
            double firstScore = BalanceScoreEstimator.estimateBurstScore(first);
            double secondScore = BalanceScoreEstimator.estimateBurstScore(second);
            double score = firstScore + secondScore;
            highestScore = Math.max(highestScore, score);
            if (score >= highestScore) {
                hotspot = describePair(first, second);
            }
            if (score > threshold) {
                violations++;
            }
        }

        return new Result(
                pairCount,
                blockedPairs,
                scoredPairs,
                violations,
                highestScore,
                Math.max(0.0, highestScore) * simulatedRounds,
                violations == 0 ? "OK" : "FAIL",
                0,
                hotspot
        );
    }

    static Result analyzeCombo(double comboThreshold, int comboRounds) {
        int scenarioCount = 0;
        int blockedPairs = 0;
        int scoredPairs = 0;
        int violations = 0;
        double highestScore = 0.0;
        String hotspot = "none";

        for (Key[][] scenario : BalanceScenarioCatalog.comboMatrix()) {
            if (scenario.length != 2) {
                continue;
            }
            Key[] teamA = scenario[0];
            Key[] teamB = scenario[1];
            if (teamA == null || teamB == null || teamA.length != 2 || teamB.length != 2) {
                continue;
            }
            scenarioCount++;
            if (BalanceTeamValidator.hasMissingOrConflict(teamA) || BalanceTeamValidator.hasMissingOrConflict(teamB)) {
                blockedPairs++;
                continue;
            }
            scoredPairs++;
            double teamAScore = BalanceScoreEstimator.estimateTeamBurstScore(teamA);
            double teamBScore = BalanceScoreEstimator.estimateTeamBurstScore(teamB);
            double scenarioScore = Math.max(teamAScore, teamBScore);
            if (scenarioScore >= highestScore) {
                hotspot = describeScenario(teamA, teamB);
            }
            highestScore = Math.max(highestScore, scenarioScore);
            if (teamAScore > comboThreshold || teamBScore > comboThreshold) {
                violations++;
            }
        }

        return new Result(
                scenarioCount,
                blockedPairs,
                scoredPairs,
                violations,
                highestScore,
                highestScore * comboRounds,
                violations == 0 ? "OK" : "FAIL",
                scenarioCount,
                hotspot
        );
    }

    private static String describePair(EnchADDEnchant first, EnchADDEnchant second) {
        return describeEnchant(first) + " + " + describeEnchant(second);
    }

    private static String describeScenario(Key[] teamA, Key[] teamB) {
        return describeTeam(teamA) + " vs " + describeTeam(teamB);
    }

    private static String describeTeam(Key[] team) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < team.length; i++) {
            if (i > 0) {
                builder.append(" + ");
            }
            builder.append(describeKey(team[i]));
        }
        return builder.toString();
    }

    private static String describeEnchant(EnchADDEnchant enchant) {
        if (enchant == null) {
            return "unknown";
        }
        String name = EnchantListFormatSupport.resolveName(enchant);
        if (name == null || name.isBlank()) {
            return enchant.getKey() == null ? "unknown" : enchant.getKey().asString();
        }
        return name;
    }

    private static String describeKey(Key key) {
        if (key == null) {
            return "unknown";
        }
        EnchADDEnchant enchant = EnchADDConfig.ENCHANTS.get(key);
        if (enchant != null) {
            return describeEnchant(enchant);
        }
        return key.asString();
    }

    record Result(int pairCount,
                  int blockedPairs,
                  int scoredPairs,
                  int violations,
                  double highestScore,
                  double estimatedDamage,
                  String status,
                  int scenarioCount,
                  String reviewLabel) {
    }
}
