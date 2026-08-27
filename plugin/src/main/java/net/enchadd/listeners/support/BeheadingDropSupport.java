package net.enchadd.listeners.support;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class BeheadingDropSupport {

    public @Nullable ItemStack getHeadForEntity(@NotNull Entity entity, @NotNull Collection<ItemStack> drops) {
        return switch (entity.getType()) {
            case ZOMBIE -> createHeadIfMissing(drops, Material.ZOMBIE_HEAD);
            case PIGLIN -> createHeadIfMissing(drops, Material.PIGLIN_HEAD);
            case WITHER_SKELETON -> createHeadIfMissing(drops, Material.WITHER_SKELETON_SKULL);
            case SKELETON -> createHeadIfMissing(drops, Material.SKELETON_SKULL);
            case CREEPER -> createHeadIfMissing(drops, Material.CREEPER_HEAD);
            case ENDER_DRAGON -> createHeadIfMissing(drops, Material.DRAGON_HEAD);
            case PLAYER -> createPlayerHeadIfMissing(drops, (Player) entity);
            default -> null;
        };
    }

    private @Nullable ItemStack createHeadIfMissing(@NotNull Iterable<ItemStack> drops, @NotNull Material type) {
        if (listContainsItemType(drops, type)) {
            return null;
        }
        return new ItemStack(type);
    }

    private @Nullable ItemStack createPlayerHeadIfMissing(@NotNull Iterable<ItemStack> drops, @NotNull Player player) {
        if (listContainsItemType(drops, Material.PLAYER_HEAD)) {
            return null;
        }
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            head.setItemMeta(meta);
        }
        return head;
    }

    private boolean listContainsItemType(@NotNull Iterable<ItemStack> list, @NotNull Material type) {
        for (ItemStack item : list) {
            if (item != null && type.equals(item.getType())) {
                return true;
            }
        }
        return false;
    }
}
