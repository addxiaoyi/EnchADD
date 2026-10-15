package net.enchadd.mod.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public final class TelepathyBehavior {

    private TelepathyBehavior() {
    }

    public static void collectFreshDrops(ServerLevel level, BlockPos origin, ServerPlayer player) {
        for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(origin).inflate(1.35), item -> item.isAlive() && item.getAge() <= 2)) {
            collect(drop, player);
        }
    }

    public static void collect(ItemEntity drop, ServerPlayer player) {
        ItemStack stack = drop.getItem();
        if (stack.isEmpty()) return;
        drop.setPickUpDelay(0);
        drop.setTarget(player.getUUID());
        drop.setPos(player.getX(), player.getY() + 0.25, player.getZ());
    }
}
