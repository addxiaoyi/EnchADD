package com.enadd.core.conflict;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import java.util.*;
import java.util.Arrays;


public final class EnchantmentConflictChecker {

    private static final EnchantmentConflictChecker INSTANCE = new EnchantmentConflictChecker();
    private final EnchantmentConflictManager conflictManager;

    private EnchantmentConflictChecker() {
        this.conflictManager = EnchantmentConflictManager.getInstance();
    }

    public static EnchantmentConflictChecker getInstance() {
        return INSTANCE;
    }

    public boolean canEnchantmentsCoexist(String enchant1, String enchant2) {
        return !conflictManager.areConflicting(enchant1, enchant2);
    }

    public List<String> getConflictsForEnchantment(String enchantmentId) {
        return new ArrayList<>(conflictManager.getConflicts(enchantmentId));
    }

    public boolean areAllEnchantmentsCompatible(List<String> enchantmentIds) {
        for (int i = 0; i < enchantmentIds.size(); i++) {
            for (int j = i + 1; j < enchantmentIds.size(); j++) {
                if (!canEnchantmentsCoexist(enchantmentIds.get(i), enchantmentIds.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<String> findIncompatibleEnchantments(List<String> enchantmentIds) {
        List<String> incompatible = new ArrayList<>();
        for (int i = 0; i < enchantmentIds.size(); i++) {
            for (int j = i + 1; j < enchantmentIds.size(); j++) {
                String e1 = enchantmentIds.get(i);
                String e2 = enchantmentIds.get(j);
                if (!canEnchantmentsCoexist(e1, e2)) {
                    if (!incompatible.contains(e1)) incompatible.add(e1);
                    if (!incompatible.contains(e2)) incompatible.add(e2);
                }
            }
        }
        return incompatible;
    }

    public boolean canPlayerEquipEnchantment(Player player, String enchantmentId) {
        List<String> equippedEnchantments = getEquippedEnchantments(player);
        for (String equipped : equippedEnchantments) {
            if (!canEnchantmentsCoexist(equipped, enchantmentId)) {
                return false;
            }
        }
        return true;
    }

    private List<String> getEquippedEnchantments(Player player) {
        List<String> enchantments = new ArrayList<>();
        for (ItemStack itemStack : player.getInventory().getArmorContents()) {
            if (itemStack != null && itemStack.hasItemMeta()) {
                for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {
                    enchantments.add(entry.getKey().getKey().getKey());
                }
            }
        }
        return enchantments;
    }

    public String getConflictReason(String enchant1, String enchant2) {
        return "These enchantments conflict and cannot be applied together";
    }

    public boolean areSimilarEffects(String enchant1, String enchant2) {
        return conflictManager.areConflicting(enchant1, enchant2);
    }

    public List<String> getEnchantmentsByCategory(String category) {
        List<String> result = new ArrayList<>();
        switch (category.toLowerCase()) {
            case "combat":
                result.addAll(Arrays.asList(
                    "critical_strike", "precision_strike", "execution", "execute",
                    "vampirism", "life_drain", "leech",
                    "bleeding", "hemorrhage", "wound", "eviscerate",
                    "backstab", "momentum", "frenzy", "rampage", "bloodlust"
                ));
                break;
            case "armor":
                result.addAll(Arrays.asList(
                    "stone_skin", "dodge", "reinforced_thorns", "thorns", "spikes",
                    "barrier", "aegis_shield", "bastion", "adrenaline", "swift_sneak"
                ));
                break;
            case "tool":
                result.addAll(Arrays.asList(
                    "efficiency", "efficiency_plus", "miner", "fortune", "fortune_plus",
                    "fortunes_grace", "treasure_hunter", "area_mining", "vein_miner",
                    "auto_smelt", "smelting_touch", "homing", "triple_shot", "quick_draw"
                ));
                break;
            case "defense":
                result.addAll(Arrays.asList(
                    "elemental_resist", "fire_protection", "frost_protection",
                    "reflect", "thorns", "spikes", "protection", "blast_protection",
                    "energy_shield", "magic_barrier", "physical_barrier"
                ));
                break;
            case "cosmetic":
                result.addAll(Arrays.asList(
                    "weapon_flame_trail", "weapon_frost_trail", "weapon_lightning_trail",
                    "weapon_poison_trail", "weapon_shadow_trail", "weapon_holy_trail",
                    "armor_glow", "armor_aura", "armor_sparkle", "armor_shimmer",
                    "armor_pulse", "armor_ripple"
                ));
                break;
            case "special":
                result.addAll(Arrays.asList(
                    "meteor_strike", "storm_caller", "dragon_breath", "phantom_strike",
                    "teleport", "phase", "void_reach", "clone", "soul_reaper"
                ));
                break;
        }
        return result;
    }
}
