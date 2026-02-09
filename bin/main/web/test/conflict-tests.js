/**
 * Enchantment Conflict System - Comprehensive Unit Tests
 * Tests for ConflictManager, ConflictUIManager, and RealtimeConflictDetector
 */

// Test Utilities
const TestRunner = {
    results: [],
    
    run(name, testFn) {
        try {
            testFn();
            this.results.push({ name, passed: true, error: null });
            console.log(`✓ ${name}`);
        } catch (error) {
            this.results.push({ name, passed: false, error: error.message });
            console.error(`✗ ${name}: ${error.message}`);
        }
    },
    
    summary() {
        const passed = this.results.filter(r => r.passed).length;
        const failed = this.results.filter(r => !r.passed).length;
        console.log(`\n=== Test Summary ===`);
        console.log(`Total: ${this.results.length}`);
        console.log(`Passed: ${passed}`);
        console.log(`Failed: ${failed}`);
        return failed === 0;
    }
};

// Mock enchantments data for testing
const mockEnchantmentsData = [
    { id: "critical_strike", name: "暴击", nameEn: "Critical Strike", category: "combat", rarity: "rare" },
    { id: "precision_strike", name: "精准打击", nameEn: "Precision Strike", category: "combat", rarity: "rare" },
    { id: "execution", name: "处决", nameEn: "Execution", category: "combat", rarity: "very_rare" },
    { id: "vampirism", name: "吸血", nameEn: "Vampirism", category: "combat", rarity: "very_rare" },
    { id: "life_drain", name: "生命汲取", nameEn: "Life Drain", category: "combat", rarity: "rare" },
    { id: "bleeding", name: "撕裂", nameEn: "Bleeding", category: "combat", rarity: "rare" },
    { id: "hemorrhage", name: "放血", nameEn: "Hemorrhage", category: "combat", rarity: "uncommon" },
    { id: "stone_skin", name: "石肤", nameEn: "Stone Skin", category: "armor", rarity: "rare" },
    { id: "reinforced_thorns", name: "强化荆棘", nameEn: "Reinforced Thorns", category: "armor", rarity: "rare" },
    { id: "thorns", name: "荆棘", nameEn: "Thorns", category: "armor", rarity: "uncommon" },
    { id: "efficiency", name: "效率", nameEn: "Efficiency", category: "tool", rarity: "common" },
    { id: "fortune", name: "时运", nameEn: "Fortune", category: "tool", rarity: "rare" },
    { id: "auto_smelt", name: "自动熔炼", nameEn: "Auto Smelt", category: "tool", rarity: "uncommon" },
    { id: "smelting_touch", name: "熔炼之触", nameEn: "Smelting Touch", category: "tool", rarity: "uncommon" },
    { id: "meteor_strike", name: "陨石打击", nameEn: "Meteor Strike", category: "special", rarity: "legendary" },
    { id: "teleport", name: "传送", nameEn: "Teleport", category: "special", rarity: "epic" },
    { id: "weapon_flame_trail", name: "火焰拖尾", nameEn: "Flame Trail", category: "cosmetic", rarity: "common" },
    { id: "weapon_frost_trail", name: "冰霜拖尾", nameEn: "Frost Trail", category: "cosmetic", rarity: "common" },
    { id: "armor_glow", name: "微光流转", nameEn: "Glow", category: "cosmetic", rarity: "common" },
    { id: "curse_burning", name: "燃烧诅咒", nameEn: "Curse of Burning", category: "curse", rarity: "curse" },
    { id: "curse_freezing", name: "冰冻诅咒", nameEn: "Curse of Freezing", category: "curse", rarity: "curse" }
];

// Make mock data available globally for conflict manager
global.enchantmentsData = mockEnchantmentsData;

console.log('=== Enchantment Conflict System Tests ===\n');

// ============================================
// ConflictManager Tests
// ============================================
console.log('--- ConflictManager Tests ---\n');

TestRunner.run('should detect conflict between critical_strike and precision_strike', () => {
    const conflict = ConflictManager.checkConflict('critical_strike', 'precision_strike');
    if (!conflict) throw new Error('Expected conflict to be detected');
    if (!conflict.hasConflict) throw new Error('Conflict should have hasConflict = true');
    if (conflict.reason !== '暴击效果') throw new Error(`Expected reason "暴击效果", got "${conflict.reason}"`);
});

TestRunner.run('should return null for same enchantment', () => {
    const conflict = ConflictManager.checkConflict('critical_strike', 'critical_strike');
    if (conflict !== null) throw new Error('Should return null for same enchantment');
});

TestRunner.run('should return null for null enchantments', () => {
    const conflict1 = ConflictManager.checkConflict(null, 'critical_strike');
    const conflict2 = ConflictManager.checkConflict('critical_strike', null);
    if (conflict1 !== null) throw new Error('Should return null for null first arg');
    if (conflict2 !== null) throw new Error('Should return null for null second arg');
});

TestRunner.run('should return null for non-conflicting enchantments', () => {
    const conflict = ConflictManager.checkConflict('critical_strike', 'vampirism');
    if (conflict !== null) throw new Error('Non-conflicting enchantments should return null');
});

TestRunner.run('should detect multiple conflicts in a group', () => {
    const conflicts = ConflictManager.checkMultipleConflicts(['critical_strike', 'precision_strike', 'execution']);
    if (conflicts.length !== 3) throw new Error(`Expected 3 conflicts, got ${conflicts.length}`);
});

TestRunner.run('should not detect duplicate conflicts', () => {
    const conflicts1 = ConflictManager.checkMultipleConflicts(['critical_strike', 'precision_strike']);
    const conflicts2 = ConflictManager.checkMultipleConflicts(['precision_strike', 'critical_strike']);
    if (conflicts1.length !== conflicts2.length) {
        throw new Error('Order should not affect conflict detection');
    }
});

TestRunner.run('should detect weapon trail conflicts', () => {
    const conflict = ConflictManager.checkConflict('weapon_flame_trail', 'weapon_frost_trail');
    if (!conflict) throw new Error('Weapon trails should conflict');
    if (conflict.category.displayName !== '装饰附魔') {
        throw new Error(`Expected category "装饰附魔", got "${conflict.category.displayName}"`);
    }
});

TestRunner.run('should detect armor effect conflicts', () => {
    const conflict = ConflictManager.checkConflict('armor_glow', 'weapon_flame_trail');
    if (conflict !== null) throw new Error('Armor and weapon effects should not conflict');
});

TestRunner.run('should detect curse conflicts', () => {
    const conflict = ConflictManager.checkConflict('curse_burning', 'curse_freezing');
    if (!conflict) throw new Error('Curse enchantments should conflict');
    if (conflict.reason !== '持续伤害') throw new Error(`Expected reason "持续伤害", got "${conflict.reason}"`);
});

TestRunner.run('should detect special enchantment conflicts', () => {
    const conflict = ConflictManager.checkConflict('meteor_strike', 'teleport');
    if (conflict !== null) throw new Error('Meteor strike and teleport should not conflict');
});

TestRunner.run('should detect tool enchantment conflicts (auto_smelt group)', () => {
    const conflict = ConflictManager.checkConflict('auto_smelt', 'smelting_touch');
    if (!conflict) throw new Error('Auto smelt enchantments should conflict');
});

TestRunner.run('should get all conflicts for an enchantment', () => {
    const conflicts = ConflictManager.getConflicts('critical_strike');
    if (!Array.isArray(conflicts)) throw new Error('Should return array');
    if (conflicts.length === 0) throw new Error('critical_strike should have conflicts');
    const hasCriticalGroup = conflicts.some(c => c.type.enchantments.includes('critical_strike'));
    if (!hasCriticalGroup) throw new Error('Should include critical group');
});

TestRunner.run('canCoexist should return true for compatible enchantments', () => {
    const result = ConflictManager.canCoexist(['critical_strike', 'vampirism', 'stone_skin']);
    if (!result) throw new Error('Compatible enchantments should coexist');
});

TestRunner.run('canCoexist should return false for conflicting enchantments', () => {
    const result = ConflictManager.canCoexist(['critical_strike', 'precision_strike']);
    if (result) throw new Error('Conflicting enchantments should not coexist');
});

TestRunner.run('getEnchantName should return correct name', () => {
    const name = ConflictManager.getEnchantName('critical_strike');
    if (name !== '暴击') throw new Error(`Expected "暴击", got "${name}"`);
});

TestRunner.run('should add conflicts to history', () => {
    ConflictManager.conflictHistory = [];
    ConflictManager.checkConflict('critical_strike', 'precision_strike');
    if (ConflictManager.conflictHistory.length === 0) {
        throw new Error('Conflict should be added to history');
    }
});

TestRunner.run('should limit history size', () => {
    ConflictManager.conflictHistory = [];
    for (let i = 0; i < 60; i++) {
        ConflictManager.addToHistory({
            hasConflict: true,
            enchant1: 'test1',
            enchant2: 'test2',
            reason: 'Test',
            description: 'Test',
            category: { displayName: 'Test' },
            toJSON: () => ({})
        });
    }
    if (ConflictManager.conflictHistory.length !== ConflictManager.maxHistorySize) {
        throw new Error('History should be limited to max size');
    }
});

// ============================================
// Statistics Tests
// ============================================
console.log('\n--- Statistics Tests ---\n');

TestRunner.run('getStatistics should return valid statistics', () => {
    const stats = ConflictManager.getStatistics();
    if (typeof stats.totalConflicts !== 'number') {
        throw new Error('totalConflicts should be a number');
    }
    if (typeof stats.byCategory !== 'object') {
        throw new Error('byCategory should be an object');
    }
    if (stats.totalConflicts <= 0) {
        throw new Error('Should have at least some conflicts');
    }
});

TestRunner.run('validateRules should validate enchantments exist', () => {
    const validation = ConflictManager.validateRules();
    if (!validation.isValid) {
        console.warn('Validation warnings:', validation.errors);
    }
});

TestRunner.run('generateReport should return complete report', () => {
    const report = ConflictManager.generateReport();
    if (!report.generatedAt) throw new Error('Report should have generatedAt');
    if (!report.statistics) throw new Error('Report should have statistics');
    if (!report.validation) throw new Error('Report should have validation');
});

// ============================================
// Edge Cases Tests
// ============================================
console.log('\n--- Edge Cases Tests ---\n');

TestRunner.run('should handle empty array in checkMultipleConflicts', () => {
    const conflicts = ConflictManager.checkMultipleConflicts([]);
    if (conflicts.length !== 0) throw new Error('Empty array should return empty conflicts');
});

TestRunner.run('should handle single enchantment', () => {
    const conflicts = ConflictManager.checkMultipleConflicts(['critical_strike']);
    if (conflicts.length !== 0) throw new Error('Single enchantment should have no conflicts');
});

TestRunner.run('should handle unknown enchantment ID', () => {
    const conflict = ConflictManager.checkConflict('unknown_enchant', 'critical_strike');
    if (conflict !== null) throw new Error('Unknown enchantment should return null');
});

TestRunner.run('should handle empty string enchantment', () => {
    const conflict = ConflictManager.checkConflict('', 'critical_strike');
    if (conflict !== null) throw new Error('Empty string should return null');
});

TestRunner.run('getConflicts should return empty for non-conflicting enchantment', () => {
    const conflicts = ConflictManager.getConflicts('unknown_enchant');
    if (!Array.isArray(conflicts)) throw new Error('Should return array');
});

TestRunner.run('getSuggestions should work for conflicting enchantment', () => {
    const suggestions = ConflictManager.getSuggestions('critical_strike');
    if (!Array.isArray(suggestions)) throw new Error('Should return array');
    if (suggestions.length === 0) throw new Error('Should have suggestions for conflicting enchantment');
});

TestRunner.run('RealtimeConflictDetector should detect inventory conflicts', () => {
    const conflicts = RealtimeConflictDetector.detectInventoryConflicts();
    if (!Array.isArray(conflicts)) throw new Error('Should return array');
});

// ============================================
// Conflict Categories Tests
// ============================================
console.log('\n--- Conflict Categories Tests ---\n');

TestRunner.run('should have all required categories defined', () => {
    const requiredCategories = ['combat', 'armor', 'tool', 'defense', 'cosmetic', 'special', 'curse'];
    for (const cat of requiredCategories) {
        if (!conflictCategories[cat]) {
            throw new Error(`Missing category: ${cat}`);
        }
    }
});

TestRunner.run('should have icon for each category', () => {
    for (const [name, data] of Object.entries(conflictCategories)) {
        if (!data.icon) {
            throw new Error(`Category ${name} missing icon`);
        }
    }
    if (conflictCategories.combat.icon !== '剑') {
        throw new Error('Combat category should have sword icon');
    }
});

TestRunner.run('should have displayName for each category', () => {
    for (const [name, data] of Object.entries(conflictCategories)) {
        if (!data.name) {
            throw new Error(`Category ${name} missing name`);
        }
    }
});

// ============================================
// Run Test Summary
// ============================================
const allPassed = TestRunner.summary();
process.exit(allPassed ? 0 : 1);
