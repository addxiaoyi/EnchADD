package net.enchadd.mod.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class FurrowBehavior {

    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> FURROW = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "furrow"));
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> REPLANTING = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "replanting"));
    private static final int RADIUS = 1;

    private FurrowBehavior() {
    }

    public static void harvest(ServerLevel level, ServerPlayer player, BlockPos center, BlockState centerState, ItemStack tool) {
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        if (EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(FURROW), tool) <= 0) return;
        boolean replant = EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(REPLANTING), tool) > 0;
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                if (dx == 0 && dz == 0) continue;
                harvestOne(level, player, center.offset(dx, 0, dz), tool, replant);
            }
        }
    }

    private static void harvestOne(ServerLevel level, ServerPlayer player, BlockPos pos, ItemStack tool, boolean replant) {
        BlockState state = level.getBlockState(pos);
        if (ReplantingBehavior.seedFor(state) == null || !ReplantingBehavior.isMature(state)) return;
        var drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, tool);
        level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        if (replant) {
            ReplantingBehavior.replant(level, pos, state, player, drops);
        }
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) continue;
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, drop.copy()));
        }
    }
}
