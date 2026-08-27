package net.enchadd.listeners.support;

import net.enchadd.enchants.FreshcatchEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FreshcatchRestoreSupport {

    private final FreshcatchEnchant config;

    public FreshcatchRestoreSupport(@NotNull FreshcatchEnchant config) {
        this.config = config;
    }

    public void handleFish(@NotNull PlayerFishEvent event, @NotNull Enchantment enchant) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        if (!(event.getCaught() instanceof Item itemEntity)) {
            return;
        }

        ItemStack caught = itemEntity.getItemStack();
        if (caught == null || !caught.getType().isEdible()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack rod = getRodWithEnchant(player, enchant);
        if (rod == null) {
            return;
        }

        int level = PerformanceUtils.getEnchantLevel(rod, enchant);
        if (level <= 0) {
            return;
        }

        int newFood = Math.min(20, player.getFoodLevel() + config.getFoodLevelPerLevel() * level);
        player.setFoodLevel(newFood);

        float newSaturation = (float) Math.min(newFood, player.getSaturation() + config.getSaturationPerLevel() * level);
        player.setSaturation(newSaturation);
    }

    private static @Nullable ItemStack getRodWithEnchant(@NotNull Player player, @NotNull Enchantment enchant) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (!main.getType().isAir() && PerformanceUtils.getEnchantLevel(main, enchant) > 0) {
            return main;
        }
        ItemStack off = player.getInventory().getItemInOffHand();
        if (!off.getType().isAir() && PerformanceUtils.getEnchantLevel(off, enchant) > 0) {
            return off;
        }
        return null;
    }
}
