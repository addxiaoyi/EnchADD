package com.enadd.core.conflict;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for EnchantmentConflictManager
 */
public class EnchantmentConflictManagerTest {
    
    private EnchantmentConflictManager manager;
    
    @BeforeEach
    void setUp() {
        manager = EnchantmentConflictManager.getInstance();
        if (!manager.isInitialized()) {
            manager.initialize();
        }
    }
    
    // ============================================
    // Basic Conflict Detection Tests
    // ============================================
    
    @Test
    void testCriticalStrikeConflict() {
        assertTrue(manager.areConflicting("critical_strike", "precision_strike"),
            "critical_strike should conflict with precision_strike");
        assertTrue(manager.areConflicting("critical_strike", "execution"),
            "critical_strike should conflict with execution");
        assertTrue(manager.areConflicting("precision_strike", "execution"),
            "precision_strike should conflict with execution");
    }
    
    
    @Test
    void testBleedConflict() {
        assertTrue(manager.areConflicting("bleeding", "hemorrhage"),
            "bleeding should conflict with hemorrhage");
        assertTrue(manager.areConflicting("bleeding", "wound"),
            "bleeding should conflict with wound");
        assertTrue(manager.areConflicting("bleeding", "eviscerate"),
            "bleeding should conflict with eviscerate");
    }
    
    @Test
    void testWeaponTrailConflict() {
        assertTrue(manager.areConflicting("weapon_flame_trail", "weapon_frost_trail"),
            "weapon_flame_trail should conflict with weapon_frost_trail");
        assertTrue(manager.areConflicting("weapon_flame_trail", "weapon_lightning_trail"),
            "weapon_flame_trail should conflict with weapon_lightning_trail");
        assertTrue(manager.areConflicting("weapon_frost_trail", "weapon_poison_trail"),
            "weapon_frost_trail should conflict with weapon_poison_trail");
    }
    
    @Test
    void testArmorEffectConflict() {
        assertTrue(manager.areConflicting("armor_glow", "armor_aura"),
            "armor_glow should conflict with armor_aura");
        assertTrue(manager.areConflicting("armor_sparkle", "armor_shimmer"),
            "armor_sparkle should conflict with armor_shimmer");
    }
    
    @Test
    void testCurseConflict() {
        assertTrue(manager.areConflicting("doom_blade", "annihilate"),
            "doom_blade should conflict with annihilate");
        assertTrue(manager.areConflicting("rampage", "bloodlust"),
            "rampage should conflict with bloodlust");
    }
    
    // ============================================
    // Non-Conflict Tests
    // ============================================
    
    @Test
    void testNonConflictingEnchantments() {
        assertFalse(manager.areConflicting("critical_strike", "vampirism"),
            "critical_strike should not conflict with vampirism");
        assertFalse(manager.areConflicting("stone_skin", "efficiency"),
            "stone_skin should not conflict with efficiency");
        assertFalse(manager.areConflicting("weapon_flame_trail", "armor_glow"),
            "weapon_flame_trail should not conflict with armor_glow");
    }
    
    @Test
    void testNormalization() {
        // Case sensitivity
        assertTrue(manager.areConflicting("CRITICAL_STRIKE", "precision_strike"),
            "Normalization should handle uppercase names");
        
        // Namespace prefixing
        assertTrue(manager.areConflicting("enadd:critical_strike", "precision_strike"),
            "Explicit namespace should match implicit one");
        assertTrue(manager.areConflicting("critical_strike", "enadd:precision_strike"),
            "Implicit namespace should match explicit one");
    }
    
    @Test
    void testNullAndEmptyHandling() {
        assertFalse(manager.areConflicting(null, "critical_strike"), "Null should not conflict");
        assertFalse(manager.areConflicting("critical_strike", null), "Null should not conflict");
        assertFalse(manager.areConflicting("", "critical_strike"), "Empty string should not conflict");
        assertFalse(manager.areConflicting("critical_strike", "  "), "Blank string should not conflict");
    }

    @Test
    void testVanillaConflicts() {
        assertTrue(manager.areConflicting("minecraft:protection", "minecraft:blast_protection"),
            "Vanilla protection enchantments should conflict");
        assertTrue(manager.areConflicting("minecraft:sharpness", "minecraft:smite"),
            "Vanilla damage enchantments should conflict");
        assertTrue(manager.areConflicting("minecraft:fortune", "minecraft:silk_touch"),
            "Vanilla tool enchantments should conflict");
        assertFalse(manager.areConflicting("minecraft:sharpness", "minecraft:protection"),
            "Sharpness and Protection should not conflict");
    }

    @Test
    void testBidirectionalConsistency() {
        String e1 = "critical_strike";
        String e2 = "precision_strike";
        
        boolean conflict1 = manager.areConflicting(e1, e2);
        boolean conflict2 = manager.areConflicting(e2, e1);
        
        assertEquals(conflict1, conflict2, "Conflict check must be bidirectional");
        assertTrue(conflict1, "These two should conflict");
    }

    @Test
    void testGetConflictsNormalization() {
        Set<String> conflicts = manager.getConflicts("CRITICAL_STRIKE");
        assertNotNull(conflicts);
        assertFalse(conflicts.isEmpty());
        assertTrue(conflicts.contains("enadd:precision_strike"));
    }
    
    // ============================================
    // Category Tests
    // ============================================
    
    @Test
    void testAllWeaponTrailsConflict() {
        String[] weaponTrails = {
            "weapon_flame_trail", "weapon_frost_trail", "weapon_lightning_trail",
            "weapon_poison_trail", "weapon_shadow_trail", "weapon_holy_trail"
        };
        
        for (int i = 0; i < weaponTrails.length; i++) {
            for (int j = i + 1; j < weaponTrails.length; j++) {
                assertTrue(manager.areConflicting(weaponTrails[i], weaponTrails[j]),
                    weaponTrails[i] + " should conflict with " + weaponTrails[j]);
            }
        }
    }
    
    @Test
    void testAllArmorEffectsConflict() {
        String[] armorEffects = {
            "armor_glow", "armor_aura", "armor_sparkle", 
            "armor_shimmer", "armor_pulse", "armor_ripple"
        };
        
        for (int i = 0; i < armorEffects.length; i++) {
            for (int j = i + 1; j < armorEffects.length; j++) {
                assertTrue(manager.areConflicting(armorEffects[i], armorEffects[j]),
                    armorEffects[i] + " should conflict with " + armorEffects[j]);
            }
        }
    }
    
    @Test
    void testElementalResistConflict() {
        String[] elementalResists = {
            "elemental_resist", "fire_protection", "frost_protection", 
            "lightning_ward", "poison_ward"
        };
        
        for (int i = 0; i < elementalResists.length; i++) {
            for (int j = i + 1; j < elementalResists.length; j++) {
                assertTrue(manager.areConflicting(elementalResists[i], elementalResists[j]),
                    elementalResists[i] + " should conflict with " + elementalResists[j]);
            }
        }
    }
    
    @Test
    void testReflectConflict() {
        String[] reflectEnchants = {
            "reflect", "thorns", "spikes", "retaliate"
        };
        
        for (int i = 0; i < reflectEnchants.length; i++) {
            for (int j = i + 1; j < reflectEnchants.length; j++) {
                assertTrue(manager.areConflicting(reflectEnchants[i], reflectEnchants[j]),
                    reflectEnchants[i] + " should conflict with " + reflectEnchants[j]);
            }
        }
    }
    
    // ============================================
    // Tool Enchantment Tests
    // ============================================
    
    @Test
    void testFortuneConflict() {
        String[] fortuneEnchants = {
            "fortune", "fortune_plus", "fortunes_grace", "treasure_hunter", "luck_of_the_sea"
        };
        
        for (int i = 0; i < fortuneEnchants.length; i++) {
            for (int j = i + 1; j < fortuneEnchants.length; j++) {
                assertTrue(manager.areConflicting(fortuneEnchants[i], fortuneEnchants[j]),
                    fortuneEnchants[i] + " should conflict with " + fortuneEnchants[j]);
            }
        }
    }
    
    @Test
    void testAutoSmeltConflict() {
        assertTrue(manager.areConflicting("auto_smelt", "smelting_touch"),
            "auto_smelt should conflict with smelting_touch");
    }
    
    @Test
    void testAreaMiningConflict() {
        String[] areaMiningEnchants = {
            "area_mining", "vein_miner", "excavation", "timber"
        };
        
        for (int i = 0; i < areaMiningEnchants.length; i++) {
            for (int j = i + 1; j < areaMiningEnchants.length; j++) {
                assertTrue(manager.areConflicting(areaMiningEnchants[i], areaMiningEnchants[j]),
                    areaMiningEnchants[i] + " should conflict with " + areaMiningEnchants[j]);
            }
        }
    }
    
    // ============================================
    // Special Enchantment Tests
    // ============================================
    
    @Test
    void testTeleportConflict() {
        String[] teleportEnchants = {
            "teleport", "phase", "void_reach"
        };
        
        for (int i = 0; i < teleportEnchants.length; i++) {
            for (int j = i + 1; j < teleportEnchants.length; j++) {
                assertTrue(manager.areConflicting(teleportEnchants[i], teleportEnchants[j]),
                    teleportEnchants[i] + " should conflict with " + teleportEnchants[j]);
            }
        }
    }
    
    @Test
    void testSummonConflict() {
        String[] summonEnchants = {
            "clone", "phantom_strike", "soul_reaper"
        };
        
        for (int i = 0; i < summonEnchants.length; i++) {
            for (int j = i + 1; j < summonEnchants.length; j++) {
                assertTrue(manager.areConflicting(summonEnchants[i], summonEnchants[j]),
                    summonEnchants[i] + " should conflict with " + summonEnchants[j]);
            }
        }
    }
    
    @Test
    void testMeteorStrikeConflict() {
        String[] specialEnchants = {
            "meteor_strike", "storm_caller", "dragon_breath", "phantom_strike"
        };
        
        for (int i = 0; i < specialEnchants.length; i++) {
            for (int j = i + 1; j < specialEnchants.length; j++) {
                assertTrue(manager.areConflicting(specialEnchants[i], specialEnchants[j]),
                    specialEnchants[i] + " should conflict with " + specialEnchants[j]);
            }
        }
    }
    
    // ============================================
    // Armor Enchantment Tests
    // ============================================
    
    @Test
    void testThornsConflict() {
        String[] thornsEnchants = {
            "reinforced_thorns", "thorns", "spikes"
        };
        
        for (int i = 0; i < thornsEnchants.length; i++) {
            for (int j = i + 1; j < thornsEnchants.length; j++) {
                assertTrue(manager.areConflicting(thornsEnchants[i], thornsEnchants[j]),
                    thornsEnchants[i] + " should conflict with " + thornsEnchants[j]);
            }
        }
    }
    
    @Test
    void testShieldConflict() {
        String[] shieldEnchants = {
            "barrier", "aegis_shield", "bastion"
        };
        
        for (int i = 0; i < shieldEnchants.length; i++) {
            for (int j = i + 1; j < shieldEnchants.length; j++) {
                assertTrue(manager.areConflicting(shieldEnchants[i], shieldEnchants[j]),
                    shieldEnchants[i] + " should conflict with " + shieldEnchants[j]);
            }
        }
    }
    
    @Test
    void testSlowConflict() {
        assertTrue(manager.areConflicting("adrenaline", "swift_sneak"),
            "adrenaline should conflict with swift_sneak");
    }
    
    // ============================================
    // Combat Enchantment Tests
    // ============================================
    
    @Test
    void testMomentumConflict() {
        assertTrue(manager.areConflicting("momentum", "frenzy"),
            "momentum should conflict with frenzy");
    }
    
    @Test
    void testRampageConflict() {
        assertTrue(manager.areConflicting("rampage", "bloodlust"),
            "rampage should conflict with bloodlust");
    }
    
    @Test
    void testInstantKillConflict() {
        assertTrue(manager.areConflicting("doom_blade", "annihilate"),
            "doom_blade should conflict with annihilate");
    }
    
    // ============================================
    // Edge Cases
    // ============================================
    
    @Test
    void testNullEnchantment() {
        assertFalse(manager.areConflicting(null, "critical_strike"),
            "null should not conflict with any enchantment");
        assertFalse(manager.areConflicting("critical_strike", null),
            "any enchantment should not conflict with null");
    }
    
    @Test
    void testNonExistentEnchantment() {
        assertFalse(manager.areConflicting("non_existent", "critical_strike"),
            "non-existent enchantment should not conflict");
        Set<String> conflicts = manager.getConflicts("non_existent");
        assertTrue(conflicts.isEmpty(), 
            "non-existent enchantment should have no conflicts");
    }
    
    @Test
    void testEmptyStringEnchantment() {
        assertFalse(manager.areConflicting("", "critical_strike"),
            "empty string should not conflict");
        assertFalse(manager.areConflicting("critical_strike", ""),
            "any enchantment should not conflict with empty string");
    }
    
    // ============================================
    // Performance Tests
    // ============================================
    
    @Test
    void testLargeConflictSetPerformance() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 1000; i++) {
            manager.areConflicting("critical_strike", "precision_strike");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(duration < 100, 
            "Conflict check should be fast (<100ms for 1000 checks), took: " + duration + "ms");
    }
    
    @Test
    void testGetAllConflictsPerformance() {
        long startTime = System.currentTimeMillis();
        
        Set<String> conflicts = manager.getConflicts("critical_strike");
        assertNotNull(conflicts);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(duration < 50, 
            "Get conflicts should be fast (<50ms), took: " + duration + "ms");
    }
    
    // ============================================
    // Integration Tests
    // ============================================
    
    @Test
    void testConflictRulesConsistency() {
        Map<String, Set<String>> rules = manager.getConflictRules();
        
        for (Map.Entry<String, Set<String>> entry : rules.entrySet()) {
            String enchantment = entry.getKey();
            Set<String> conflicts = entry.getValue();
            
            for (String conflict : conflicts) {
                assertTrue(manager.areConflicting(enchantment, conflict),
                    enchantment + " should conflict with " + conflict);
                assertTrue(manager.areConflicting(conflict, enchantment),
                    "Conflict should be bidirectional: " + conflict + " should conflict with " + enchantment);
            }
        }
    }
    
    @Test
    void testNoSelfConflict() {
        Map<String, Set<String>> rules = manager.getConflictRules();
        
        for (Map.Entry<String, Set<String>> entry : rules.entrySet()) {
            String enchantment = entry.getKey();
            Set<String> conflicts = entry.getValue();
            
            assertFalse(conflicts.contains(enchantment),
                enchantment + " should not conflict with itself");
        }
    }
}
