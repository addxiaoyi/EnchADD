package net.enchadd.listeners.support;

import net.enchadd.enchants.FreshcatchEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
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
        if (event.isCancelled() || event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
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

        int newFood = NutritionSupport.food(player.getFoodLevel(), level, config.getMaxLevel(),
                config.getFoodLevelPerLevel());
        if (newFood != player.getFoodLevel()) player.setFoodLevel(newFood);
        float saturation = player.getSaturation();
        float restored = NutritionSupport.saturation(saturation, newFood, level, config.getMaxLevel(),
                config.getSaturationPerLevel());
        if (Float.compare(restored, saturation) != 0) player.setSaturation(restored);
    }

    private static @Nullable ItemStack getRodWithEnchant(@NotNull Player player, @NotNull Enchantment enchant) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main.getType() == Material.FISHING_ROD && PerformanceUtils.getEnchantLevel(main, enchant) > 0) {
            return main;
        }
        ItemStack off = player.getInventory().getItemInOffHand();
        if (off.getType() == Material.FISHING_ROD && PerformanceUtils.getEnchantLevel(off, enchant) > 0) {
            return off;
        }
        return null;
    }
}
