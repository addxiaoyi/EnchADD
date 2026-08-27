package net.enchadd.listeners.support;

import net.enchadd.enchants.IrrigationEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Farmland;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class IrrigationHydrationSupport {

    private final IrrigationEnchant config;

    public IrrigationHydrationSupport(@NotNull IrrigationEnchant config) {
        this.config = config;
    }

    public int effectiveRadius(boolean sneaking) {
        if (config.isBypassWhenSneaking() && sneaking) {
            return 0;
        }
        return config.getRadius();
    }

    public @Nullable Block resolveFarmlandTarget(@NotNull Block clickedBlock) {
        if (clickedBlock.getBlockData() instanceof Farmland) {
            return clickedBlock;
        }
        Block below = clickedBlock.getRelative(BlockFace.DOWN);
        return below.getBlockData() instanceof Farmland ? below : null;
    }

    public @NotNull List<Block> collectTargets(@NotNull Block center, int radius) {
        int clampedRadius = Math.max(0, radius);
        int diameter = clampedRadius * 2 + 1;
        List<Block> targets = PerformanceUtils.newArrayListWithCapacity(diameter * diameter);
        for (int dx = -clampedRadius; dx <= clampedRadius; dx++) {
            for (int dz = -clampedRadius; dz <= clampedRadius; dz++) {
                Block target = center.getRelative(dx, 0, dz);
                if (needsHydration(target)) {
                    targets.add(target);
                }
            }
        }
        return targets;
    }

    public void hydrate(@NotNull Block block) {
        if (!(block.getBlockData() instanceof Farmland farmland)) {
            return;
        }
        farmland.setMoisture(farmland.getMaximumMoisture());
        block.setBlockData(farmland, false);
    }

    private boolean needsHydration(@NotNull Block block) {
        if (!(block.getBlockData() instanceof Farmland farmland)) {
            return false;
        }
        return farmland.getMoisture() < farmland.getMaximumMoisture();
    }
}
