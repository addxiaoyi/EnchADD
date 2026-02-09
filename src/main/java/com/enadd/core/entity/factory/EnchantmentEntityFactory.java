package com.enadd.core.entity.factory;

import com.enadd.core.budget.EntityBudgetController;
import com.enadd.core.entity.pool.EntityPool;
import com.enadd.core.entity.EnchantmentEntity;
import com.enadd.core.entity.EntityLifecycleManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;


public final class EnchantmentEntityFactory {
    // Holder模式实现线程安全单例
    private static final class Holder {
        private static final EnchantmentEntityFactory INSTANCE = new EnchantmentEntityFactory();
    }

    // 预分配容量，减少扩容开销
    private final ConcurrentHashMap<String, Supplier<? extends EnchantmentEntity>> entityFactories = new ConcurrentHashMap<>(32);
    private final ConcurrentHashMap<String, EntityPool<?>> entityPools = new ConcurrentHashMap<>(32);

    // 使用AtomicLong生成ID，避免字符串拼接
    private final AtomicLong idGenerator = new AtomicLong(0);

    private volatile boolean poolingEnabled = true;

    private EnchantmentEntityFactory() {
        registerDefaultFactories();
    }

    public static EnchantmentEntityFactory getInstance() {
        return Holder.INSTANCE;
    }

    private void registerDefaultFactories() {
    }

    public <T extends EnchantmentEntity> void registerFactory(String typeId, Supplier<T> factory) {
        entityFactories.put(typeId, factory);

        if (poolingEnabled) {
            EntityPool<T> pool = EntityPool.create(factory, 10, 100);
            entityPools.put(typeId, pool);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends EnchantmentEntity> T createEntity(String typeId, String enchantmentId, int level, String playerId) {
        Supplier<? extends EnchantmentEntity> factory = entityFactories.get(typeId);
        if (factory == null) {
            return null;
        }

        EntityBudgetController.BudgetCategory category = getCategory(typeId);
        if (!EntityBudgetController.getInstance().allocate(category, 1)) {
            return null;
        }

        EnchantmentEntity entity;
        if (poolingEnabled) {
            EntityPool<?> pool = entityPools.get(typeId);
            if (pool != null) {
                entity = (EnchantmentEntity) pool.borrowEntity();
                if (entity == null) {
                    entity = factory.get();
                }
            } else {
                entity = factory.get();
            }
        } else {
            entity = factory.get();
        }

        if (entity != null) {
            entity.setEntityId(generateEntityId());
            entity.setLifecycleManager(EntityLifecycleManager.getInstance());
            entity.activate();
        }

        return (T) entity;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void returnEntity(EnchantmentEntity entity) {
        if (entity == null) return;

        EntityBudgetController.getInstance().free(getCategory(entity.getEntityType().name()), 1);

        if (poolingEnabled) {
            EntityPool pool = entityPools.get(entity.getEntityType().name());
            if (pool != null) {
                pool.returnEntity(entity);
                return;
            }
        }

        entity.destroy();
    }

    @SuppressWarnings("unchecked")
    public <T extends EnchantmentEntity> EntityPool<T> getPool(String typeId) {
        return (EntityPool<T>) entityPools.get(typeId);
    }

    private String generateEntityId() {
        // 优化：使用AtomicLong避免时间戳拼接，提升性能
        return "ENCHANT_" + idGenerator.incrementAndGet();
    }

    private EntityBudgetController.BudgetCategory getCategory(String typeId) {
        return switch (typeId) {
            case "PARTICLE" -> EntityBudgetController.BudgetCategory.PARTICLE;
            case "PROJECTILE" -> EntityBudgetController.BudgetCategory.PROJECTILE;
            case "AREA_EFFECT" -> EntityBudgetController.BudgetCategory.AREA_EFFECT;
            case "SUMMONED" -> EntityBudgetController.BudgetCategory.SUMMONED;
            case "DAMAGE" -> EntityBudgetController.BudgetCategory.DAMAGE;
            default -> EntityBudgetController.BudgetCategory.TEMPORARY;
        };
    }

    public void setPoolingEnabled(boolean enabled) {
        this.poolingEnabled = enabled;
    }

    public boolean isPoolingEnabled() {
        return poolingEnabled;
    }

    public void shutdown() {
        entityPools.values().forEach(pool -> pool.shutdown());
        entityPools.clear();
        entityFactories.clear();
    }
}
