package com.enadd.core.conflict;

import com.enadd.core.api.IEnchantmentConflictRules;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.enchantments.Enchantment;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public final class EnchantmentConflictManager implements IEnchantmentConflictRules {

    private static final EnchantmentConflictManager INSTANCE = new EnchantmentConflictManager();
    private static final Logger LOGGER = Logger.getLogger(EnchantmentConflictManager.class.getName());
    
    private final Map<String, Set<String>> conflictRules = new ConcurrentHashMap<>();
    // BUG FIX #4: 添加反向索引提高性能
    private final Map<String, String> enchantmentToCategory = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    private EnchantmentConflictManager() {
    }

    public static EnchantmentConflictManager getInstance() {
        return INSTANCE;
    }

    public synchronized void initialize() {
        if (!initialized) {
            try {
                // BUG FIX #11: 添加异常处理和回滚
                initializeConflictRules();
                initialized = true;
                // BUG FIX #10: 添加日志
                LOGGER.info("EnchantmentConflictManager initialized with " + conflictRules.size() + " enchantments");
                LOGGER.info("Total conflict rules: " + getTotalConflictCount());
            } catch (Exception e) {
                LOGGER.severe("Failed to initialize EnchantmentConflictManager: " + e.getMessage());
                e.printStackTrace();
                // 回滚
                conflictRules.clear();
                enchantmentToCategory.clear();
                initialized = false;
            }
        }
    }

    public synchronized void shutdown() {
        if (initialized) {
            // BUG FIX #10: 添加日志
            LOGGER.info("Shutting down EnchantmentConflictManager");
            conflictRules.clear();
            enchantmentToCategory.clear();
            initialized = false;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    private void initializeConflictRules() {
        // BUG FIX #6: 添加原版附魔冲突规则
        addConflictGroup("vanilla_protection", Arrays.asList(
            "minecraft:protection", "minecraft:blast_protection", 
            "minecraft:fire_protection", "minecraft:projectile_protection"
        ));
        
        addConflictGroup("vanilla_damage", Arrays.asList(
            "minecraft:sharpness", "minecraft:smite", "minecraft:bane_of_arthropods"
        ));
        
        addConflictGroup("vanilla_tool", Arrays.asList(
            "minecraft:fortune", "minecraft:silk_touch"
        ));
        
        addConflictGroup("vanilla_bow", Arrays.asList(
            "minecraft:infinity", "minecraft:mending"
        ));
        
        addConflictGroup("vanilla_depth", Arrays.asList(
            "minecraft:depth_strider", "minecraft:frost_walker"
        ));
        
        addConflictGroup("vanilla_crossbow", Arrays.asList(
            "minecraft:multishot", "minecraft:piercing"
        ));
        
        // ========== 战斗附魔冲突规则 ==========
        addConflictGroup("combat", Arrays.asList(
            "critical_strike", "precision_strike", "execution", "execute"
        ));
        addConflictGroup("combat", Arrays.asList(
            "vampirism", "life_drain", "leech"
        ));
        addConflictGroup("combat", Arrays.asList(
            "crippling", "cripple", "frost_bite"
        ));
        addConflictGroup("combat", Arrays.asList(
            "bleeding", "hemorrhage", "wound", "eviscerate"
        ));
        addConflictGroup("combat", Arrays.asList(
            "backstab", "eviscerate"
        ));
        addConflictGroup("combat", Arrays.asList(
            "momentum", "frenzy"
        ));
        addConflictGroup("combat", Arrays.asList(
            "rampage", "bloodlust"
        ));
        addConflictGroup("combat", Arrays.asList(
            "doom_blade", "annihilate"
        ));

        // ========== 护甲附魔冲突规则 ==========
        addConflictGroup("armor", Arrays.asList(
            "stone_skin", "reinforced_thorns"
        ));
        addConflictGroup("armor", Arrays.asList(
            "dodge", "evasive"
        ));
        addConflictGroup("armor", Arrays.asList(
            "reinforced_thorns", "thorns", "spikes"
        ));
        addConflictGroup("armor", Arrays.asList(
            "barrier", "aegis_shield", "bastion"
        ));
        addConflictGroup("armor", Arrays.asList(
            "adrenaline", "swift_sneak"
        ));

        // ========== 工具附魔冲突规则 ==========
        addConflictGroup("tool", Arrays.asList(
            "efficiency", "efficiency_plus", "miner", "strong_draw"
        ));
        addConflictGroup("tool", Arrays.asList(
            "fortune", "fortune_plus", "fortunes_grace", "treasure_hunter", "luck_of_the_sea"
        ));
        addConflictGroup("tool", Arrays.asList(
            "mending", "experience_boost", "auto_repair"
        ));
        addConflictGroup("tool", Arrays.asList(
            "area_mining", "vein_miner", "excavation", "timber"
        ));
        addConflictGroup("tool", Arrays.asList(
            "auto_smelt", "smelting_touch"
        ));
        addConflictGroup("tool", Arrays.asList(
            "homing", "triple_shot"
        ));
        addConflictGroup("tool", Arrays.asList(
            "quick_draw", "strong_draw"
        ));

        // ========== 防御附魔冲突规则 ==========
        addConflictGroup("defense", Arrays.asList(
            "elemental_resist", "fire_protection", "frost_protection",
            "lightning_ward", "poison_ward"
        ));
        addConflictGroup("defense", Arrays.asList(
            "reflect", "thorns", "spikes", "retaliate"
        ));
        addConflictGroup("defense", Arrays.asList(
            "protection", "blast_protection", "projectile_protection",
            "fire_ward", "frost_ward"
        ));
        addConflictGroup("defense", Arrays.asList(
            "energy_shield", "magic_barrier", "physical_barrier"
        ));

        // ========== 装饰附魔冲突规则 ==========
        addConflictGroup("cosmetic", Arrays.asList(
            "weapon_flame_trail", "weapon_frost_trail", "weapon_lightning_trail",
            "weapon_poison_trail", "weapon_shadow_trail", "weapon_holy_trail"
        ));
        addConflictGroup("cosmetic", Arrays.asList(
            "armor_glow", "armor_aura", "armor_sparkle",
            "armor_shimmer", "armor_pulse", "armor_ripple"
        ));

        // ========== 特殊附魔冲突规则 ==========
        addConflictGroup("special", Arrays.asList(
            "meteor_strike", "storm_caller", "dragon_breath", "phantom_strike"
        ));
        addConflictGroup("special", Arrays.asList(
            "teleport", "phase", "void_reach"
        ));
        addConflictGroup("special", Arrays.asList(
            "clone", "phantom_strike", "soul_reaper"
        ));
    }

    private void addConflictGroup(String category, List<String> enchantments) {
        // BUG FIX #5: 统一空字符串处理
        if (category == null || category.trim().isEmpty() || enchantments == null || enchantments.isEmpty()) {
            return;
        }

        for (int i = 0; i < enchantments.size(); i++) {
            String enchant1 = enchantments.get(i);
            if (enchant1 == null || enchant1.trim().isEmpty()) {
                continue;
            }
            
            // BUG FIX #7: 标准化附魔ID
            enchant1 = normalizeEnchantmentId(enchant1);

            if (!conflictRules.containsKey(enchant1)) {
                conflictRules.put(enchant1, ConcurrentHashMap.newKeySet());
            }
            
            // BUG FIX #4: 添加到反向索引
            enchantmentToCategory.put(enchant1, category);

            Set<String> conflicts = conflictRules.get(enchant1);
            if (conflicts == null) {
                continue;
            }

            for (int j = 0; j < enchantments.size(); j++) {
                if (i != j) {
                    String enchant2 = enchantments.get(j);
                    if (enchant2 != null && !enchant2.trim().isEmpty()) {
                        // BUG FIX #7: 标准化附魔ID
                        enchant2 = normalizeEnchantmentId(enchant2);
                        conflicts.add(enchant2);
                    }
                }
            }
        }
    }
    
    /**
     * BUG FIX #7: 标准化附魔ID处理
     * 确保所有附魔ID都有命名空间
     */
    private String normalizeEnchantmentId(String enchantmentId) {
        if (enchantmentId == null || enchantmentId.trim().isEmpty()) {
            return "";
        }
        
        enchantmentId = enchantmentId.trim().toLowerCase();
        
        // 如果没有命名空间，添加enadd命名空间
        if (!enchantmentId.contains(":")) {
            return "enadd:" + enchantmentId;
        }
        
        return enchantmentId;
    }

    @Override
    public Map<String, Set<String>> getConflictRules() {
        // BUG FIX #8: 深度不可修改包装
        Map<String, Set<String>> unmodifiable = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : conflictRules.entrySet()) {
            unmodifiable.put(entry.getKey(), Collections.unmodifiableSet(new HashSet<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(unmodifiable);
    }

    @Override
    public boolean areConflicting(String enchantment1, String enchantment2) {
        // BUG FIX #5: 统一空字符串处理
        if (enchantment1 == null || enchantment1.trim().isEmpty() || 
            enchantment2 == null || enchantment2.trim().isEmpty()) {
            return false;
        }
        
        // BUG FIX #7: 标准化附魔ID
        enchantment1 = normalizeEnchantmentId(enchantment1);
        enchantment2 = normalizeEnchantmentId(enchantment2);

        // 同一个附魔不冲突
        if (enchantment1.equals(enchantment2)) {
            return false;
        }

        if (conflictRules.containsKey(enchantment1)) {
            Set<String> conflicts = conflictRules.get(enchantment1);
            if (conflicts != null && conflicts.contains(enchantment2)) {
                return true;
            }
        }

        if (conflictRules.containsKey(enchantment2)) {
            Set<String> conflicts = conflictRules.get(enchantment2);
            if (conflicts != null && conflicts.contains(enchantment1)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Set<String> getConflicts(String enchantmentId) {
        // BUG FIX #7: 标准化附魔ID
        enchantmentId = normalizeEnchantmentId(enchantmentId);
        Set<String> conflicts = conflictRules.get(enchantmentId);
        // BUG FIX #8: 返回不可修改的副本
        return conflicts != null ? Collections.unmodifiableSet(new HashSet<>(conflicts)) : Collections.emptySet();
    }

    @Override
    public String getCategory(String enchantmentId) {
        // BUG FIX #4: 使用反向索引提高性能
        // BUG FIX #7: 标准化附魔ID
        enchantmentId = normalizeEnchantmentId(enchantmentId);
        return enchantmentToCategory.get(enchantmentId);
    }
    
    // BUG FIX #12: 添加统计方法
    
    /**
     * 获取总冲突规则数量
     */
    public int getTotalConflictCount() {
        int total = 0;
        for (Set<String> conflicts : conflictRules.values()) {
            total += conflicts.size();
        }
        return total / 2; // 每对冲突被计算两次
    }
    
    /**
     * 获取附魔数量
     */
    public int getEnchantmentCount() {
        return conflictRules.size();
    }
    
    /**
     * 获取分类数量
     */
    public int getCategoryCount() {
        return new HashSet<>(enchantmentToCategory.values()).size();
    }
    
    /**
     * 获取所有分类
     */
    public Set<String> getAllCategories() {
        return Collections.unmodifiableSet(new HashSet<>(enchantmentToCategory.values()));
    }

    // ========== 静态工具方法 ==========

    public static boolean isIncompatibleWith(Enchantment ench1, Enchantment ench2) {
        if (ench1 == null || ench2 == null) return false;
        return INSTANCE.areConflicting(getEnchantmentId(ench1), getEnchantmentId(ench2));
    }

    public static boolean conflictsWith(Enchantment ench1, Enchantment ench2) {
        return isIncompatibleWith(ench1, ench2);
    }

    public static boolean hasConflicts(Collection<Enchantment> enchantments) {
        if (enchantments == null || enchantments.size() < 2) return false;
        List<Enchantment> list = new ArrayList<>(enchantments);
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (conflictsWith(list.get(i), list.get(j))) return true;
            }
        }
        return false;
    }

    public static boolean canCombineWithBinder(Enchantment enchantment, int binderLevel, Set<Enchantment> existing) {
        if (enchantment == null) return true;
        int conflicts = 0;
        for (Enchantment e : existing) {
            if (conflictsWith(enchantment, e)) {
                conflicts++;
            }
        }
        // 每级Binder允许额外3个冲突（根据CompatibilityChecker中的getMaxConflictsWithLevel）
        return conflicts <= binderLevel * 3;
    }

    public static boolean canApplyTogether(Collection<Enchantment> enchantments, int binderLevel) {
        if (enchantments == null || enchantments.size() < 2) return true;
        int totalConflicts = 0;
        List<Enchantment> list = new ArrayList<>(enchantments);
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (conflictsWith(list.get(i), list.get(j))) {
                    totalConflicts++;
                }
            }
        }
        return totalConflicts <= binderLevel * 3;
    }

    public static Map<Enchantment, Set<Enchantment>> getAllConflicts(Collection<Enchantment> enchantments) {
        Map<Enchantment, Set<Enchantment>> allConflicts = new HashMap<>();
        List<Enchantment> list = new ArrayList<>(enchantments);
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (conflictsWith(list.get(i), list.get(j))) {
                    allConflicts.computeIfAbsent(list.get(i), k -> new HashSet<>()).add(list.get(j));
                    allConflicts.computeIfAbsent(list.get(j), k -> new HashSet<>()).add(list.get(i));
                }
            }
        }
        return allConflicts;
    }

    public static List<Set<Enchantment>> getConflictGroups(Collection<Enchantment> enchantments) {
        List<Set<Enchantment>> groups = new ArrayList<>();
        Set<Enchantment> processed = new HashSet<>();
        List<Enchantment> list = new ArrayList<>(enchantments);

        for (int i = 0; i < list.size(); i++) {
            Enchantment e1 = list.get(i);
            if (processed.contains(e1)) continue;

            Set<Enchantment> group = new HashSet<>();
            group.add(e1);
            processed.add(e1);

            for (int j = i + 1; j < list.size(); j++) {
                Enchantment e2 = list.get(j);
                if (conflictsWith(e1, e2)) {
                    group.add(e2);
                    processed.add(e2);
                }
            }

            if (group.size() > 1) {
                groups.add(group);
            }
        }
        return groups;
    }

    public static int calculateRequiredBinderSlots(Set<Enchantment> enchantments) {
        if (enchantments == null || enchantments.size() < 2) return 0;
        int conflicts = 0;
        List<Enchantment> list = new ArrayList<>(enchantments);
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (conflictsWith(list.get(i), list.get(j))) {
                    conflicts++;
                }
            }
        }
        return conflicts;
    }

    public static ResourceLocation getEnchantmentLocation(Enchantment enchantment) {
        if (enchantment == null) return null;
        String key = enchantment.getKey().toString();
        return ResourceLocation.tryParse(key);
    }

    public static Set<ResourceLocation> getConflictsFor(ResourceLocation loc) {
        if (loc == null) return Collections.emptySet();
        Set<String> conflicts = INSTANCE.getConflicts(loc.toString());
        return conflicts.stream()
            .map(ResourceLocation::tryParse)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private static String getEnchantmentId(Enchantment enchantment) {
        if (enchantment == null) return "";
        return enchantment.getKey().toString();
    }
}
