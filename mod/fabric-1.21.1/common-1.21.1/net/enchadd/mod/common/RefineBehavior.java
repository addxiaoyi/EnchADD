package net.enchadd.mod.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public final class RefineBehavior {

    private RefineBehavior() {
    }

    public static void apply(ServerLevel level, BlockPos origin, RandomSource random, int enchantmentLevel) {
        if (enchantmentLevel <= 0) return;
        for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(origin).inflate(1.35), item -> item.isAlive() && item.getAge() <= 2)) {
            ItemStack stack = drop.getItem();
            String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            if (!RefineRules.isOreDrop(path) || stack.getCount() >= stack.getMaxStackSize()) continue;
            if (RefineRules.shouldTrigger(random, enchantmentLevel)) stack.grow(1);
        }
    }
}
