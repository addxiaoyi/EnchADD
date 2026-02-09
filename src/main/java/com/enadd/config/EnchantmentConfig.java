package com.enadd.config;

import com.enadd.core.api.IEnchantmentConfig;
import static com.enadd.util.SecurityValidator.validatePositive;

/**
 * 附魔配置
 */
public final class EnchantmentConfig {
    
    private EnchantmentConfig() {}

    /**
     * 附魔基础配置
     */
    public static final class BaseConfig implements IEnchantmentConfig {
        private final com.enadd.core.config.EnchantmentConfig delegate;

        public BaseConfig(com.enadd.core.config.EnchantmentConfig delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getBaseCost(int level) {
            return delegate.getBaseCost(level);
        }

        @Override
        public int getCostPerLevel() {
            return delegate.getCostPerLevel();
        }

        @Override
        public int getMaxBonus() {
            return delegate.getMaxBonus();
        }

        @Override
        public int getMaxLevel() {
            return delegate.getMaxLevel();
        }

        @Override
        public String getRarity() {
            return delegate.getRarity();
        }

        @Override
        public boolean isTreasure() {
            return delegate.isTreasure();
        }

        @Override
        public int getMinLevel() {
            return delegate.getMinLevel();
        }

        @Override
        public int getWeight() {
            return delegate.getWeight();
        }
    }

    /**
     * 附魔粘合剂配置
     */
    public static final class BinderConfig implements IEnchantmentConfig {
        public static final BinderConfig INSTANCE = new BinderConfig();
        
        public static final int MAX_LEVEL = 3;
        public static final int CONFLICTS_PER_LEVEL = 2;
        public static final boolean IS_TREASURE = true;
        public static final boolean IS_TRADEABLE = false;
        public static final boolean IS_DISCOVERABLE = false;
        public static final int ANVIL_COST = 10;
        
        private BinderConfig() {}
        
        @Override
        public boolean isEnabled() {
            return true;
        }
        
        @Override
        public int getMaxLevel() {
            return MAX_LEVEL;
        }

        @Override
        public int getBaseCost(int level) {
            return 20 + (level - 1) * 10;
        }

        @Override
        public int getCostPerLevel() {
            return 10;
        }

        @Override
        public int getMaxBonus() {
            return 50;
        }

        @Override
        public String getRarity() {
            return "very_rare";
        }

        @Override
        public boolean isTreasure() {
            return IS_TREASURE;
        }
        
        public static void validate() {
            validatePositive(MAX_LEVEL, "BinderConfig.MAX_LEVEL");
            validatePositive(CONFLICTS_PER_LEVEL, "BinderConfig.CONFLICTS_PER_LEVEL");
            validatePositive(ANVIL_COST, "BinderConfig.ANVIL_COST");
        }
    }

    /**
     * 流血附魔配置
     */
    public static final class BleedingConfig implements IEnchantmentConfig {
        public static final BleedingConfig INSTANCE = new BleedingConfig();
        public static final double DAMAGE_PER_LEVEL = 0.5;
        public static final int DURATION_TICKS_PER_LEVEL = 60;
        public static final int MAX_DURATION_TICKS = 300;
        public static final double TRIGGER_CHANCE_BASE = 0.3;
        public static final double TRIGGER_CHANCE_PER_LEVEL = 0.1;

        @Override
        public int getMaxLevel() {
            return 5;
        }

        @Override
        public int getBaseCost(int level) {
            return 10 + (level - 1) * 5;
        }

        @Override
        public int getCostPerLevel() {
            return 5;
        }

        @Override
        public int getMaxBonus() {
            return 15;
        }

        @Override
        public String getRarity() {
            return "rare";
        }

        @Override
        public boolean isTreasure() {
            return false;
        }
    }

    /**
     * 暴击附魔配置
     */
    public static final class CriticalStrikeConfig implements IEnchantmentConfig {
        public static final CriticalStrikeConfig INSTANCE = new CriticalStrikeConfig();
        public static final double CHANCE_BASE = 0.05;
        public static final double CHANCE_PER_LEVEL = 0.03;
        public static final double MAX_CHANCE = 0.5;
        public static final double DAMAGE_MULTIPLIER_BASE = 1.5;
        public static final double DAMAGE_MULTIPLIER_PER_LEVEL = 0.25;

        @Override
        public int getMaxLevel() {
            return 5;
        }

        @Override
        public int getBaseCost(int level) {
            return 15 + (level - 1) * 7;
        }

        @Override
        public int getCostPerLevel() {
            return 7;
        }

        @Override
        public int getMaxBonus() {
            return 20;
        }

        @Override
        public String getRarity() {
            return "epic";
        }

        @Override
        public boolean isTreasure() {
            return false;
        }
    }

    /**
     * 生命偷取附魔配置
     */
    public static final class LifeStealConfig implements IEnchantmentConfig {
        public static final LifeStealConfig INSTANCE = new LifeStealConfig();
        
        public static final double HEAL_PERCENTAGE_BASE = 0.10;
        public static final double HEAL_PERCENTAGE_PER_LEVEL = 0.05;
        public static final double MAX_HEAL_PERCENTAGE = 0.50;

        private LifeStealConfig() {}

        @Override
        public int getMaxLevel() {
            return 5;
        }

        @Override
        public int getBaseCost(int level) {
            return 15 + (level - 1) * 10;
        }

        @Override
        public int getCostPerLevel() {
            return 10;
        }

        @Override
        public int getMaxBonus() {
            return 25;
        }

        @Override
        public String getRarity() {
            return "epic";
        }

        @Override
        public boolean isTreasure() {
            return false;
        }

        public static void validate() {
            validatePositive(HEAL_PERCENTAGE_BASE, "LifeStealConfig.HEAL_PERCENTAGE_BASE");
            validatePositive(HEAL_PERCENTAGE_PER_LEVEL, "LifeStealConfig.HEAL_PERCENTAGE_PER_LEVEL");
            validatePositive(MAX_HEAL_PERCENTAGE, "LifeStealConfig.MAX_HEAL_PERCENTAGE");
        }
    }

    /**
     * 缓存配置
     */
    public static final class CacheConfig {
        public static final int MAX_CACHE_SIZE = 1000;
        public static final long CLEANUP_INTERVAL_MS = 600000; // 10 minutes
        
        private CacheConfig() {}

        public static void validate() {
            validatePositive(MAX_CACHE_SIZE, "CacheConfig.MAX_CACHE_SIZE");
            validatePositive((int)CLEANUP_INTERVAL_MS, "CacheConfig.CLEANUP_INTERVAL_MS");
        }
    }

    /**
     * 事件处理器配置
     */
    public static final class EventConfig {
        public static final boolean USE_ASYNC_EVENTS = true;
        public static final int MAX_EVENTS_PER_TICK = 500;

        private EventConfig() {}

        public static void validate() {
            validatePositive(MAX_EVENTS_PER_TICK, "EventConfig.MAX_EVENTS_PER_TICK");
        }
    }
}
