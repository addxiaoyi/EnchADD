package net.enchadd.mod.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public final class ToolInteractionBehavior {

    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> ARBORIST = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "arborist"));
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> TRAILBLAZER = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "trailblazer"));
    private static final Map<Block, Block> STRIPPED = Map.ofEntries(
            Map.entry(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG), Map.entry(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD),
            Map.entry(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG), Map.entry(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD),
            Map.entry(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG), Map.entry(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD),
            Map.entry(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG), Map.entry(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD),
            Map.entry(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG), Map.entry(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD),
            Map.entry(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG), Map.entry(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD),
            Map.entry(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG), Map.entry(Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD),
            Map.entry(Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG), Map.entry(Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_WOOD),
            Map.entry(Blocks.PALE_OAK_LOG, Blocks.STRIPPED_PALE_OAK_LOG), Map.entry(Blocks.PALE_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_WOOD),
            Map.entry(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM), Map.entry(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE),
            Map.entry(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM), Map.entry(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE),
            Map.entry(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK)
    );

    private ToolInteractionBehavior() {
    }

    public static void arborist(Level level, ServerPlayer player, BlockPos clicked, BlockState state, ItemStack tool) {
        if (!isTool(tool, "_axe") || player.isShiftKeyDown() || EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ARBORIST), tool) <= 0) return;
        Block stripped = STRIPPED.get(state.getBlock());
        if (stripped == null) return;
        for (int offset = 1; offset <= 2; offset++) {
            BlockPos target = clicked.above(offset);
            if (level.getBlockState(target).getBlock() != state.getBlock()) break;
            BlockState source = level.getBlockState(target);
            BlockState replacement = stripped.defaultBlockState();
            if (replacement.hasProperty(RotatedPillarBlock.AXIS) && source.hasProperty(RotatedPillarBlock.AXIS)) {
                replacement = replacement.setValue(RotatedPillarBlock.AXIS, source.getValue(RotatedPillarBlock.AXIS));
            }
            level.setBlock(target, replacement, 3);
        }
    }

    public static void trailblazer(Level level, ServerPlayer player, BlockPos clicked, Direction face, ItemStack tool) {
        if (!isTool(tool, "_shovel") || player.isShiftKeyDown() || face != Direction.UP) return;
        if (EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TRAILBLAZER), tool) <= 0) return;
        BlockState center = level.getBlockState(clicked);
        if (!flattenable(center) || !level.getBlockState(clicked.above()).isAir()) return;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos target = clicked.offset(dx, 0, dz);
                if (flattenable(level.getBlockState(target)) && level.getBlockState(target.above()).isAir()) {
                    level.setBlock(target, Blocks.DIRT_PATH.defaultBlockState(), 3);
                }
            }
        }
    }

    private static boolean flattenable(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.COARSE_DIRT
                || block == Blocks.PODZOL || block == Blocks.MYCELIUM || block == Blocks.ROOTED_DIRT;
    }

    private static boolean isTool(ItemStack stack, String suffix) {
        if (stack.isEmpty()) return false;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith(suffix);
    }
}
