package net.enchadd.listeners.support;

import net.enchadd.enchants.FurrowEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class FurrowHarvestSupport {

    private final Tag<Material> cropTag;
    private final FurrowEnchant config;

    public FurrowHarvestSupport(@NotNull Tag<Material> cropTag, @NotNull FurrowEnchant config) {
        this.cropTag = cropTag;
        this.config = config;
    }

    public boolean bypassWhenSneaking() {
        return config.isBypassWhenSneaking();
    }

    public boolean isHarvestable(@NotNull Block block) {
        Material type = block.getType();
        if (!cropTag.isTagged(type) && type != Material.NETHER_WART && type != Material.SWEET_BERRY_BUSH && type != Material.COCOA) {
            return false;
        }
        if (!(block.getBlockData() instanceof Ageable ageable)) {
            return false;
        }
        return !config.isMatureOnly() || ageable.getAge() >= ageable.getMaximumAge();
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
                Block candidate = center.getRelative(dx, 0, dz);
                if (isHarvestable(candidate)) {
                    targets.add(candidate);
                }
            }
        }
        return targets;
    }

    public void harvest(@NotNull Block block,
                 @NotNull Player player,
                 @NotNull ItemStack tool,
                 boolean shouldDropItems,
                 boolean shouldReplant) {
        if (!(block.getBlockData() instanceof Ageable ageable)) {
            return;
        }

        List<ItemStack> drops = shouldDropItems ? snapshotDrops(block, tool, player) : new ArrayList<>();
        boolean replanted = shouldReplant && tryReplant(block, ageable, drops, player);
        if (!replanted) {
            block.setType(Material.AIR, false);
        }

        dropStacks(block, drops);
    }

    private boolean tryReplant(@NotNull Block block,
                               @NotNull Ageable originalAgeable,
                               @NotNull List<ItemStack> drops,
                               @NotNull Player player) {
        Material placementMaterial = resolvePlacementMaterial(block);
        if (placementMaterial == null || placementMaterial.isAir()) {
            return false;
        }

        boolean shouldReplant = player.getGameMode() == GameMode.CREATIVE;
        if (!shouldReplant) {
            shouldReplant = consumeSeedFromInventory(player.getInventory(), placementMaterial);
        }
        if (!shouldReplant) {
            shouldReplant = consumeSeedFromDrops(drops, placementMaterial);
        }
        if (!shouldReplant) {
            return false;
        }

        Ageable replanted = (Ageable) originalAgeable.clone();
        replanted.setAge(0);
        block.setBlockData(replanted, false);
        return true;
    }

    private @Nullable Material resolvePlacementMaterial(@NotNull Block block) {
        try {
            Material placementMaterial = block.getBlockData().getPlacementMaterial();
            if (placementMaterial != null && !placementMaterial.isAir()) {
                return placementMaterial;
            }
        } catch (RuntimeException ignored) {
            // Some mock implementations do not expose placement material; fall back to known crop seeds.
        }

        return switch (block.getType()) {
            case WHEAT -> Material.WHEAT_SEEDS;
            case BEETROOTS -> Material.BEETROOT_SEEDS;
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            case NETHER_WART -> Material.NETHER_WART;
            case SWEET_BERRY_BUSH -> Material.SWEET_BERRIES;
            case COCOA -> Material.COCOA_BEANS;
            case TORCHFLOWER_CROP -> Material.TORCHFLOWER_SEEDS;
            case PITCHER_CROP -> Material.PITCHER_POD;
            default -> null;
        };
    }

    private @NotNull List<ItemStack> snapshotDrops(@NotNull Block block,
                                                   @NotNull ItemStack tool,
                                                   @NotNull Player player) {
        List<ItemStack> drops = new ArrayList<>();
        try {
            for (ItemStack stack : block.getDrops(tool, player)) {
                if (stack == null || stack.getType().isAir() || stack.getAmount() <= 0) {
                    continue;
                }
                drops.add(stack.clone());
            }
        } catch (UnsupportedOperationException ignored) {
            // Mock environments may not implement all crop drop paths.
        }
        return drops;
    }

    private void dropStacks(@NotNull Block block, @NotNull List<ItemStack> drops) {
        for (ItemStack stack : drops) {
            if (stack == null || stack.getType().isAir() || stack.getAmount() <= 0) {
                continue;
            }
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.2, 0.5), stack);
        }
    }

    private boolean consumeSeedFromInventory(@NotNull PlayerInventory inventory, @NotNull Material placementMaterial) {
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() != placementMaterial) {
                continue;
            }
            decrementStack(item);
            return true;
        }
        return false;
    }

    private boolean consumeSeedFromDrops(@NotNull List<ItemStack> drops, @NotNull Material placementMaterial) {
        for (ItemStack stack : drops) {
            if (stack == null || stack.getType() != placementMaterial) {
                continue;
            }
            decrementStack(stack);
            return true;
        }
        return false;
    }

    private void decrementStack(@NotNull ItemStack stack) {
        int amount = stack.getAmount();
        if (amount <= 1) {
            stack.setAmount(0);
            return;
        }
        stack.setAmount(amount - 1);
    }
}
