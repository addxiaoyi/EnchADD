package net.enchadd.mod.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class IrrigationBehavior {

    private static final ResourceKey<Enchantment> IRRIGATION = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath("enchadd", "irrigation")
    );
    private static final int DEFAULT_RADIUS = 1;
    private static final int MAX_RADIUS = 8;

    private IrrigationBehavior() {
    }

    public static void hydrate(Level level, ServerPlayer player, BlockPos clicked, ItemStack tool) {
        if (level.isClientSide() || player.isShiftKeyDown() || !isHoe(tool)) return;
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        if (EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(IRRIGATION), tool) <= 0) return;

        BlockPos center = farmlandTarget(level, clicked);
        if (center == null) return;
        int radius = Math.min(MAX_RADIUS, DEFAULT_RADIUS);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                hydrateOne(level, center.offset(dx, 0, dz));
            }
        }
    }

    private static void hydrateOne(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(BlockStateProperties.MOISTURE)) return;
        int maximum = 7;
        if (state.getValue(BlockStateProperties.MOISTURE) >= maximum) return;
        level.setBlock(pos, state.setValue(BlockStateProperties.MOISTURE, maximum), 3);
    }

    private static BlockPos farmlandTarget(Level level, BlockPos clicked) {
        BlockState state = level.getBlockState(clicked);
        if (state.hasProperty(BlockStateProperties.MOISTURE)) return clicked;
        BlockPos below = clicked.below();
        return level.getBlockState(below).hasProperty(BlockStateProperties.MOISTURE) ? below : null;
    }

    private static boolean isHoe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_hoe");
    }
}
