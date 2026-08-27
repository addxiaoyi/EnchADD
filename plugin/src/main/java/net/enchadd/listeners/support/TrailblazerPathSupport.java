package net.enchadd.listeners.support;

import net.enchadd.enchants.TrailblazerEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public final class TrailblazerPathSupport {

    private static final Set<Material> FLATTENABLE = Set.of(
            Material.GRASS_BLOCK,
            Material.DIRT,
            Material.COARSE_DIRT,
            Material.PODZOL,
            Material.MYCELIUM,
            Material.ROOTED_DIRT
    );

    private final TrailblazerEnchant config;

    public TrailblazerPathSupport(@NotNull TrailblazerEnchant config) {
        this.config = config;
    }

    public boolean bypassWhenSneaking() {
        return config.isBypassWhenSneaking();
    }

    public int radius() {
        return config.getRadius();
    }

    public boolean canFlatten(@NotNull Block block) {
        return FLATTENABLE.contains(block.getType()) && block.getRelative(BlockFace.UP).getType().isAir();
    }

    public @NotNull List<Block> collectTargets(@NotNull Block center) {
        int clampedRadius = Math.max(0, config.getRadius());
        int diameter = clampedRadius * 2 + 1;
        List<Block> targets = PerformanceUtils.newArrayListWithCapacity(diameter * diameter);
        for (int dx = -clampedRadius; dx <= clampedRadius; dx++) {
            for (int dz = -clampedRadius; dz <= clampedRadius; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                Block target = center.getRelative(dx, 0, dz);
                if (canFlatten(target)) {
                    targets.add(target);
                }
            }
        }
        return targets;
    }

    public void flatten(@NotNull Block block) {
        block.setType(Material.DIRT_PATH, false);
    }
}
