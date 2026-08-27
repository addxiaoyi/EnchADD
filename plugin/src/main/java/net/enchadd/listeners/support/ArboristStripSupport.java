package net.enchadd.listeners.support;

import net.enchadd.enchants.ArboristEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ArboristStripSupport {

    private static final Map<Material, Material> STRIPPED_VARIANTS = buildStrippedVariants();

    private final ArboristEnchant config;

    public ArboristStripSupport(@NotNull ArboristEnchant config) {
        this.config = config;
    }

    public boolean bypassWhenSneaking() {
        return config.isBypassWhenSneaking();
    }

    public int extraBlocks() {
        return config.getExtraBlocks();
    }

    public @Nullable Material resolveStrippedType(@NotNull Material clickedType) {
        return STRIPPED_VARIANTS.get(clickedType);
    }

    public @NotNull List<Block> collectTargets(@NotNull Block center, @NotNull Material clickedType) {
        int extraBlocks = config.getExtraBlocks();
        List<Block> targets = PerformanceUtils.newArrayListWithCapacity(extraBlocks);
        for (int offset = 1; offset <= extraBlocks; offset++) {
            Block target = center.getRelative(BlockFace.UP, offset);
            if (target.getType() != clickedType) {
                break;
            }
            targets.add(target);
        }
        return targets;
    }

    public void applyStrippedMaterial(@NotNull Block block, @NotNull Material strippedType) {
        BlockData originalData = block.getBlockData();
        BlockData strippedData = strippedType.createBlockData();
        if (originalData instanceof Orientable originalOrientable && strippedData instanceof Orientable strippedOrientable) {
            strippedOrientable.setAxis(originalOrientable.getAxis());
        }
        block.setBlockData(strippedData, false);
    }

    private static @NotNull Map<Material, Material> buildStrippedVariants() {
        Map<Material, Material> variants = new EnumMap<>(Material.class);
        putVariant(variants, "OAK_LOG", "STRIPPED_OAK_LOG");
        putVariant(variants, "OAK_WOOD", "STRIPPED_OAK_WOOD");
        putVariant(variants, "SPRUCE_LOG", "STRIPPED_SPRUCE_LOG");
        putVariant(variants, "SPRUCE_WOOD", "STRIPPED_SPRUCE_WOOD");
        putVariant(variants, "BIRCH_LOG", "STRIPPED_BIRCH_LOG");
        putVariant(variants, "BIRCH_WOOD", "STRIPPED_BIRCH_WOOD");
        putVariant(variants, "JUNGLE_LOG", "STRIPPED_JUNGLE_LOG");
        putVariant(variants, "JUNGLE_WOOD", "STRIPPED_JUNGLE_WOOD");
        putVariant(variants, "ACACIA_LOG", "STRIPPED_ACACIA_LOG");
        putVariant(variants, "ACACIA_WOOD", "STRIPPED_ACACIA_WOOD");
        putVariant(variants, "DARK_OAK_LOG", "STRIPPED_DARK_OAK_LOG");
        putVariant(variants, "DARK_OAK_WOOD", "STRIPPED_DARK_OAK_WOOD");
        putVariant(variants, "MANGROVE_LOG", "STRIPPED_MANGROVE_LOG");
        putVariant(variants, "MANGROVE_WOOD", "STRIPPED_MANGROVE_WOOD");
        putVariant(variants, "CHERRY_LOG", "STRIPPED_CHERRY_LOG");
        putVariant(variants, "CHERRY_WOOD", "STRIPPED_CHERRY_WOOD");
        putVariant(variants, "PALE_OAK_LOG", "STRIPPED_PALE_OAK_LOG");
        putVariant(variants, "PALE_OAK_WOOD", "STRIPPED_PALE_OAK_WOOD");
        putVariant(variants, "CRIMSON_STEM", "STRIPPED_CRIMSON_STEM");
        putVariant(variants, "CRIMSON_HYPHAE", "STRIPPED_CRIMSON_HYPHAE");
        putVariant(variants, "WARPED_STEM", "STRIPPED_WARPED_STEM");
        putVariant(variants, "WARPED_HYPHAE", "STRIPPED_WARPED_HYPHAE");
        putVariant(variants, "BAMBOO_BLOCK", "STRIPPED_BAMBOO_BLOCK");
        return variants;
    }

    private static void putVariant(@NotNull Map<Material, Material> variants,
                                   @NotNull String sourceName,
                                   @NotNull String strippedName) {
        try {
            variants.put(Material.valueOf(sourceName), Material.valueOf(strippedName));
        } catch (IllegalArgumentException ignored) {
        }
    }
}
