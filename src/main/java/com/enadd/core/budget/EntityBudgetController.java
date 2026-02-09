package com.enadd.core.budget;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public final class EntityBudgetController {
    // Holder模式优化单例
    private static final class Holder {
        private static final EntityBudgetController INSTANCE = new EntityBudgetController();
    }

    // 预分配容量
    private final ConcurrentHashMap<BudgetCategory, AtomicLong> budgets = new ConcurrentHashMap<>(8);
    private final ConcurrentHashMap<BudgetCategory, AtomicLong> usage = new ConcurrentHashMap<>(8);
    private final ConcurrentHashMap<BudgetCategory, AtomicInteger> count = new ConcurrentHashMap<>(8);

    private final AtomicLong globalBudget = new AtomicLong(10000);
    private final AtomicLong globalUsage = new AtomicLong(0);
    private volatile boolean enforceLimits = true;

    private EntityBudgetController() {
        initializeBudgets();
    }

    private void initializeBudgets() {
        for (BudgetCategory category : BudgetCategory.values()) {
            budgets.put(category, new AtomicLong(category.getDefaultBudget()));
            usage.put(category, new AtomicLong(0));
            count.put(category, new AtomicInteger(0));
        }
    }

    public static EntityBudgetController getInstance() {
        return Holder.INSTANCE;
    }

    public boolean allocate(BudgetCategory category, long amount) {
        if (category == null) {
            return false;
        }

        if (!enforceLimits) {
            AtomicInteger catCount = count.get(category);
            if (catCount != null) {
                catCount.incrementAndGet();
            }
            return true;
        }

        AtomicLong budget = budgets.get(category);
        AtomicLong catUsage = usage.get(category);
        AtomicInteger catCount = count.get(category);

        // 防御性检查
        if (budget == null || catUsage == null || catCount == null) {
            return false;
        }

        long currentUsage = catUsage.get();
        long budgetLimit = budget.get();

        if (currentUsage + amount <= budgetLimit) {
            catUsage.addAndGet(amount);
            globalUsage.addAndGet(amount);
            catCount.incrementAndGet();
            return true;
        }
        return false;
    }

    public boolean free(BudgetCategory category, long amount) {
        if (category == null) {
            return false;
        }

        AtomicLong catUsage = usage.get(category);
        AtomicInteger catCount = count.get(category);

        // 防御性检查
        if (catUsage == null || catCount == null) {
            return false;
        }

        @SuppressWarnings("unused")
        long newUsage = Math.max(0, catUsage.addAndGet(-amount));
        @SuppressWarnings("unused")
        long newGlobal = Math.max(0, globalUsage.addAndGet(-amount));

        @SuppressWarnings("unused")
        int newCount = Math.max(0, catCount.decrementAndGet());

        return true;
    }

    public boolean canAllocate(BudgetCategory category, long amount) {
        if (category == null) {
            return false;
        }

        if (!enforceLimits) return true;

        AtomicLong catUsage = usage.get(category);
        AtomicLong budget = budgets.get(category);

        if (catUsage == null || budget == null) {
            return false;
        }

        return catUsage.get() + amount <= budget.get();
    }

    public boolean canAllocateGlobal(long amount) {
        if (!enforceLimits) return true;
        return globalUsage.get() + amount <= globalBudget.get();
    }

    public void setBudget(BudgetCategory category, long budget) {
        if (category == null) {
            return;
        }

        AtomicLong budgetAtomic = budgets.get(category);
        if (budgetAtomic != null) {
            budgetAtomic.set(budget);
        }
    }

    public void setGlobalBudget(long budget) {
        globalBudget.set(budget);
    }

    public long getBudget(BudgetCategory category) {
        if (category == null) {
            return 0;
        }

        AtomicLong budget = budgets.get(category);
        return budget != null ? budget.get() : 0;
    }

    public long getGlobalBudget() {
        return globalBudget.get();
    }

    public long getUsage(BudgetCategory category) {
        if (category == null) {
            return 0;
        }

        AtomicLong catUsage = usage.get(category);
        return catUsage != null ? catUsage.get() : 0;
    }

    public long getGlobalUsage() {
        return globalUsage.get();
    }

    public int getCount(BudgetCategory category) {
        if (category == null) {
            return 0;
        }

        AtomicInteger catCount = count.get(category);
        return catCount != null ? catCount.get() : 0;
    }

    public float getUsagePercent(BudgetCategory category) {
        if (category == null) {
            return 0;
        }

        AtomicLong budget = budgets.get(category);
        AtomicLong catUsage = usage.get(category);

        if (budget == null || catUsage == null) {
            return 0;
        }

        long budgetVal = budget.get();
        long usageVal = catUsage.get();
        return budgetVal > 0 ? (float) usageVal / budgetVal * 100 : 0;
    }

    public float getGlobalUsagePercent() {
        long budget = globalBudget.get();
        long used = globalUsage.get();
        return budget > 0 ? (float) used / budget * 100 : 0;
    }

    public void setEnforceLimits(boolean enforce) {
        this.enforceLimits = enforce;
    }

    public boolean isEnforcingLimits() {
        return enforceLimits;
    }

    public BudgetStatus getStatus() {
        long globalBudgetVal = globalBudget.get();
        long globalUsageVal = globalUsage.get();
        boolean globalHealthy = globalUsageVal < globalBudgetVal * 0.9;

        ConcurrentHashMap<BudgetCategory, Boolean> categoryStatus = new ConcurrentHashMap<>(8);
        for (BudgetCategory category : BudgetCategory.values()) {
            if (category == null) {
                continue;
            }

            AtomicLong budget = budgets.get(category);
            AtomicLong catUsage = usage.get(category);

            if (budget != null && catUsage != null) {
                long budgetVal = budget.get();
                long usageVal = catUsage.get();
                categoryStatus.put(category, usageVal < budgetVal * 0.9);
            } else {
                categoryStatus.put(category, true);
            }
        }

        return new BudgetStatus(globalHealthy, globalUsageVal, globalBudgetVal, categoryStatus);
    }

    public void resetAll() {
        usage.values().forEach(v -> v.set(0));
        count.values().forEach(v -> v.set(0));
        globalUsage.set(0);
    }

    public void shutdown() {
        resetAll();
        enforceLimits = false;
    }

    public enum BudgetCategory {
        PARTICLE("粒子效果", 1000),
        PROJECTILE("投射物", 500),
        AREA_EFFECT("区域效果", 200),
        SUMMONED("召唤物", 100),
        TEMPORARY("临时实体", 5000),
        DAMAGE("伤害实体", 1000),
        UTILITY("utility实体", 300);

        private final String displayName;
        private final long defaultBudget;

        BudgetCategory(String displayName, long defaultBudget) {
            this.displayName = displayName;
            this.defaultBudget = defaultBudget;
        }

        public String getDisplayName() {
            return displayName;
        }

        public long getDefaultBudget() {
            return defaultBudget;
        }
    }

    public static final class BudgetStatus {
        private final boolean globalHealthy;
        private final long globalUsage;
        private final long globalBudget;
        private final ConcurrentHashMap<BudgetCategory, Boolean> categoryStatus;

        public BudgetStatus(boolean globalHealthy, long globalUsage, long globalBudget,
                          ConcurrentHashMap<BudgetCategory, Boolean> categoryStatus) {
            this.globalHealthy = globalHealthy;
            this.globalUsage = globalUsage;
            this.globalBudget = globalBudget;
            this.categoryStatus = categoryStatus;
        }

        public boolean isGlobalHealthy() { return globalHealthy; }
        public long getGlobalUsage() { return globalUsage; }
        public long getGlobalBudget() { return globalBudget; }
        public float getGlobalUsagePercent() { return globalBudget > 0 ? (float) globalUsage / globalBudget * 100 : 0; }
        public boolean isHealthy(BudgetCategory category) { return categoryStatus.getOrDefault(category, true); }
    }
}
