package com.enadd.enchantments.conflict;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import com.enadd.core.conflict.EnchantmentConflictManager;
import java.util.*;











public final class EnchantmentCompatibilityChecker {

    private EnchantmentCompatibilityChecker() {}

    public static CompatibilityResult checkCompatibility(Enchantment ench1, Enchantment ench2) {
        if (ench1 == null || ench2 == null) {
            return CompatibilityResult.INVALID;
        }

        if (ench1 == ench2) {
            return CompatibilityResult.SAME;
        }

        if (EnchantmentConflictManager.isIncompatibleWith(ench1, ench2)) {
            return CompatibilityResult.INCOMPATIBLE;
        }

        if (EnchantmentConflictManager.conflictsWith(ench1, ench2)) {
            return CompatibilityResult.CONFLICTING;
        }

        return CompatibilityResult.COMPATIBLE;
    }

    public static CompatibilityResult checkCompatibilityWithBinder(Enchantment enchantment, int binderLevel) {
        if (enchantment == null) {
            return CompatibilityResult.INVALID;
        }

        if (binderLevel <= 0) {
            return CompatibilityResult.NO_BINDER;
        }

        if (binderLevel > 3) {
            binderLevel = 3;
        }

        int maxConflicts = getMaxConflictsWithLevel(binderLevel);

        if (maxConflicts >= 6) {
            return CompatibilityResult.BINDER_STRONG;
        } else if (maxConflicts >= 4) {
            return CompatibilityResult.BINDER_MEDIUM;
        } else if (maxConflicts >= 2) {
            return CompatibilityResult.BINDER_WEAK;
        } else {
            return CompatibilityResult.BINDER_NONE;
        }
    }

    private static int getMaxConflictsWithLevel(int level) {
        return level * 3;
    }

    public static boolean canApplyTogether(Enchantment ench1, Enchantment ench2) {
        return checkCompatibility(ench1, ench2) == CompatibilityResult.COMPATIBLE;
    }

    public static boolean canApplyWithBinder(Enchantment enchantment, int binderLevel, Collection<Enchantment> existing) {
        if (enchantment == null || existing == null) {
            return false;
        }

        Set<Enchantment> existingSet = new HashSet<>(existing);
        existingSet.add(enchantment);

        return EnchantmentConflictManager.canCombineWithBinder(enchantment, binderLevel, existingSet);
    }

    public static boolean canCombineAll(Collection<Enchantment> enchantments) {
        if (enchantments == null || enchantments.size() < 2) {
            return true;
        }

        return !EnchantmentConflictManager.hasConflicts(enchantments);
    }

    public static boolean canCombineAllWithBinder(Collection<Enchantment> enchantments, int binderLevel) {
        if (enchantments == null || enchantments.isEmpty()) {
            return true;
        }

        return EnchantmentConflictManager.canApplyTogether(enchantments, binderLevel);
    }

    public static Map<Enchantment, Set<Enchantment>> getConflictMap(Collection<Enchantment> enchantments) {
        return EnchantmentConflictManager.getAllConflicts(enchantments);
    }

    public static List<Set<Enchantment>> getConflictGroups(Collection<Enchantment> enchantments) {
        return EnchantmentConflictManager.getConflictGroups(enchantments);
    }

    public static int countConflicts(Collection<Enchantment> enchantments) {
        if (enchantments == null || enchantments.size() < 2) {
            return 0;
        }

        int count = 0;
        List<Enchantment> list = new ArrayList<>(enchantments);

        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (EnchantmentConflictManager.conflictsWith(list.get(i), list.get(j))) {
                    count++;
                }
            }
        }

        return count;
    }

    public static int calculateRequiredBinderSlots(Collection<Enchantment> enchantments) {
        return EnchantmentConflictManager.calculateRequiredBinderSlots(new HashSet<>(enchantments));
    }

    public static int getMinBinderLevelForCombination(Collection<Enchantment> enchantments) {
        if (enchantments == null || enchantments.isEmpty()) {
            return 0;
        }

        if (!EnchantmentConflictManager.hasConflicts(enchantments)) {
            return 0;
        }

        int requiredSlots = calculateRequiredBinderSlots(enchantments);

        for (int level = 1; level <= 3; level++) {
            if (level * 2 >= requiredSlots) {
                return level;
            }
        }

        return 3;
    }

    public static boolean canApplyToItem(ItemStack item, Enchantment enchantment) {
        if (item == null || item.isEmpty() || enchantment == null) {
            return false;
        }

        return true;
    }

    public static CompatibilityReport generateReport(Collection<Enchantment> enchantments) {
        CompatibilityReport report = new CompatibilityReport();

        if (enchantments == null || enchantments.isEmpty()) {
            report.isCompatible = true;
            report.conflictCount = 0;
            report.conflictGroups = new ArrayList<>();
            return report;
        }

        Set<Enchantment> enchantmentSet = new HashSet<>(enchantments);
        report.conflictCount = countConflicts(enchantments);
        report.conflictGroups = getConflictGroups(enchantments);
        report.conflictMap = getConflictMap(enchantmentSet);
        report.requiredBinderSlots = calculateRequiredBinderSlots(enchantmentSet);
        report.minBinderLevel = getMinBinderLevelForCombination(enchantmentSet);
        report.isCompatible = (report.conflictCount == 0) || (report.requiredBinderSlots <= report.minBinderLevel * 2);

        return report;
    }

    public static String getConflictDescription(Enchantment ench1, Enchantment ench2) {
        CompatibilityResult result = checkCompatibility(ench1, ench2);

        if (result == CompatibilityResult.COMPATIBLE) {
            return "这两个附魔可以共存";
        } else if (result == CompatibilityResult.CONFLICTING) {
            return "这两个附魔存在冲突，需要使用粘合附魔来解除限制";
        } else if (result == CompatibilityResult.INCOMPATIBLE) {
            return "这两个附魔完全不兼容，无法同时使用";
        } else if (result == CompatibilityResult.SAME) {
            return "这是同一个附魔";
        } else {
            return "无法确定兼容性";
        }
    }

    public static class CompatibilityResult {
        public static final CompatibilityResult COMPATIBLE = new CompatibilityResult("compatible", true, false);
        public static final CompatibilityResult CONFLICTING = new CompatibilityResult("conflicting", false, true);
        public static final CompatibilityResult INCOMPATIBLE = new CompatibilityResult("incompatible", false, false);
        public static final CompatibilityResult SAME = new CompatibilityResult("same", true, false);
        public static final CompatibilityResult INVALID = new CompatibilityResult("invalid", false, false);

        public static final CompatibilityResult BINDER_STRONG = new CompatibilityResult("binder_strong", true, true);
        public static final CompatibilityResult BINDER_MEDIUM = new CompatibilityResult("binder_medium", true, true);
        public static final CompatibilityResult BINDER_WEAK = new CompatibilityResult("binder_weak", true, true);
        public static final CompatibilityResult BINDER_NONE = new CompatibilityResult("binder_none", false, true);
        public static final CompatibilityResult NO_BINDER = new CompatibilityResult("no_binder", false, true);

        private final String status;
        private final boolean canCombine;
        private final boolean needsBinder;

        private CompatibilityResult(String status, boolean canCombine, boolean needsBinder) {
            this.status = status;
            this.canCombine = canCombine;
            this.needsBinder = needsBinder;
        }

        public String getStatus() {
            return status;
        }

        public boolean canCombine() {
            return canCombine;
        }

        public boolean needsBinder() {
            return needsBinder;
        }

        public boolean isCompatible() {
            return canCombine && !needsBinder;
        }

        @Override
        public String toString() {
            return "CompatibilityResult{" +
                   "status='" + status + '\'' +
                   ", canCombine=" + canCombine +
                   ", needsBinder=" + needsBinder +
                   '}';
        }
    }

    public static class CompatibilityReport {
        public boolean isCompatible;
        public int conflictCount;
        public List<Set<Enchantment>> conflictGroups;
        public Map<Enchantment, Set<Enchantment>> conflictMap;
        public int requiredBinderSlots;
        public int minBinderLevel;

        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("兼容性报告:\n");
            sb.append("总冲突数: ").append(conflictCount).append("\n");
            sb.append("冲突组数: ").append(conflictGroups.size()).append("\n");
            sb.append("所需粘合槽位: ").append(requiredBinderSlots).append("\n");
            sb.append("最低粘合等级: ").append(minBinderLevel).append("\n");
            sb.append("是否兼容: ").append(isCompatible ? "是" : "否");
            return sb.toString();
        }

        public String getDetailedSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("详细兼容性报告\n");
            sb.append("================\n");
            sb.append("总附魔数: ").append(conflictMap.size()).append("\n");
            sb.append("冲突数: ").append(conflictCount).append("\n");
            sb.append("冲突组:\n");

            for (int i = 0; i < conflictGroups.size(); i++) {
                Set<Enchantment> group = conflictGroups.get(i);
                sb.append("  组 ").append(i + 1).append(": ");

                List<String> names = new ArrayList<>();
                for (Enchantment ench : group) {
                    String name = getEnchantmentName(ench);
                    names.add(name);
                }
                sb.append(String.join(", ", names)).append("\n");
            }

            sb.append("所需粘合等级: ").append(minBinderLevel).append(" (槽位: ").append(requiredBinderSlots).append(")\n");
            sb.append("最终兼容: ").append(isCompatible ? "是" : "否");

            return sb.toString();
        }

        private String getEnchantmentName(Enchantment ench) {
            if (ench == null) return "Unknown";
            return ench.toString().replace("Enchantment[", "").replace("]", "");
        }
    }
}




