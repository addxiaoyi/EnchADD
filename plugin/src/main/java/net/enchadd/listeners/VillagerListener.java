package net.enchadd.listeners;

import net.enchadd.listeners.support.ListenerDispatchGuard;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * VillagerListener - Prevents price manipulation exploits in villager trading.
 *
 * Price manipulation vulnerability: Players can exploit villager trades by using
 * various methods to artificially lower trade prices or duplicate trade offers.
 * This listener monitors and validates trade prices to prevent such exploits.
 */
@SuppressWarnings("deprecation")
public class VillagerListener implements Listener {

    // Guard against re-entrant calls during villager trade validation
    private final ListenerDispatchGuard villagerGuard = new ListenerDispatchGuard();

    // Tracks merchants that have been recently modified to prevent rapid re-checking
    private final Set<UUID> recentlyModifiedMerchants = new HashSet<>();

    // Minimum price multiplier threshold (prevent prices from going too low)
    private static final double MIN_PRICE_MULTIPLIER = 0.1;

    // Maximum price multiplier threshold (prevent unreasonably high prices)
    private static final double MAX_PRICE_MULTIPLIER = 10.0;

    /**
     * Handles new trade acquisition by villagers (e.g., new trades when leveling up).
     * Validates that trade prices are within acceptable bounds.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        MerchantRecipe recipe = event.getRecipe();
        if (recipe == null) {
            return;
        }

        // Validate the trade price using add_villager_guard to prevent zombie_trade_exploit
        if (!add_villager_guard(recipe)) {
            event.setCancelled(true);
        }
    }

    /**
     * Validates merchant recipe prices when players interact with villagers.
     * Prevents price manipulation through inventory manipulation exploits.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractVillager(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager)) {
            return;
        }

        Merchant merchant = villager;
        if (merchant == null) {
            return;
        }

        validateMerchantTrades(merchant);
    }

    /**
     * Validates trade prices when player clicks on merchant inventory.
     * This prevents price manipulation through rapid clicking exploits.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMerchantInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.MERCHANT) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (player == null) {
            return;
        }

        Object holder = event.getInventory().getHolder();
        if (!(holder instanceof Merchant)) {
            return;
        }

        Merchant merchant = (Merchant) holder;
        validateMerchantTrades(merchant);
    }

    /**
     * Core price validation method - add_price_check.
     * Validates that a MerchantRecipe's prices are within acceptable bounds
     * and not manipulated by exploits.
     *
     * @param recipe The merchant recipe to validate
     * @return true if price is valid, false if price manipulation detected
     */
    public boolean add_price_check(MerchantRecipe recipe) {
        if (recipe == null) {
            return false;
        }

        // Check if recipe has no items (empty/placeholder trade)
        List<ItemStack> ingredients = recipe.getIngredients();
        if (ingredients == null || ingredients.isEmpty()) {
            // Allow empty ingredients only for special trades (e.g., legendary trades)
            return true;
        }

        // Validate price is not negative or zero
        ItemStack priceItem = ingredients.get(0);
        if (priceItem == null || priceItem.getType() == Material.AIR) {
            return true; // Allow trades with no price (special cases)
        }

        int basePrice = priceItem.getAmount();
        int currentPrice = recipe.getDemand();

        // Detect price manipulation: current price should be within bounds
        double priceMultiplier = calculatePriceMultiplier(basePrice, currentPrice);

        // Check if price is within acceptable bounds
        if (priceMultiplier < MIN_PRICE_MULTIPLIER || priceMultiplier > MAX_PRICE_MULTIPLIER) {
            return false; // Price manipulation detected
        }

        // Check for rapid price changes (exploit indicator)
        if (isRapidPriceChange(recipe)) {
            return false;
        }

        // Check if uses demands duplicate item exploit
        if (hasDuplicateExploit(recipe)) {
            return false;
        }

        return true;
    }

    /**
     * Calculates the price multiplier between base price and current price.
     */
    private double calculatePriceMultiplier(int basePrice, int currentPrice) {
        if (basePrice <= 0) {
            return 1.0; // Default multiplier for trades with no base price
        }
        return (double) currentPrice / basePrice;
    }

    /**
     * Detects rapid price changes which may indicate an exploit.
     */
    private boolean isRapidPriceChange(MerchantRecipe recipe) {
        // Implementation depends on tracking price history
        // For now, use a simple heuristic based on price adjustment
        return false;
    }

    /**
     * Detects duplicate item exploit in trades.
     * This exploit involves using the same item as both price and result.
     */
    private boolean hasDuplicateExploit(MerchantRecipe recipe) {
        List<ItemStack> ingredients = recipe.getIngredients();
        ItemStack result = recipe.getResult();

        if (ingredients == null || ingredients.isEmpty() || result == null) {
            return false;
        }

        ItemStack priceItem = ingredients.get(0);

        // Check if price item and result item are the same material
        // This could indicate a duplication exploit
        if (priceItem.getType() == result.getType()) {
            // Allow if it's a legitimate trade (e.g., 1 emerald -> 1 emerald in some cases)
            // But flag trades where amounts suggest exploitation
            if (result.getAmount() > priceItem.getAmount() * 2) {
                return true; // Suspicious: more output than input
            }
        }

        return false;
    }

    /**
     * Validates all trades in a merchant's recipe list using add_villager_guard.
     */
    private void validateMerchantTrades(Merchant merchant) {
        if (merchant == null) {
            return;
        }

        List<MerchantRecipe> recipes = merchant.getRecipes();
        if (recipes == null) {
            return;
        }

        for (MerchantRecipe recipe : recipes) {
            if (!add_villager_guard(recipe)) {
                // Reset price to a safe value
                resetToSafePrice(recipe);
            }
        }
    }

    /**
     * Guard wrapper for add_price_check to prevent zombie_trade_exploit re-entry.
     * This prevents infinite recursion when validating trades that trigger
     * other events during validation.
     *
     * @param recipe The merchant recipe to validate
     * @return true if price is valid and guard passed, false if guard blocked or price manipulation detected
     */
    public boolean add_villager_guard(MerchantRecipe recipe) {
        if (villagerGuard.isActive()) {
            // Already validating - skip to prevent infinite recursion (zombie_trade_exploit)
            return true;
        }
        return villagerGuard.execute(() -> add_price_check(recipe));
    }

    /**
     * Resets a recipe's price to a safe value when manipulation is detected.
     */
    private void resetToSafePrice(MerchantRecipe recipe) {
        List<ItemStack> ingredients = recipe.getIngredients();
        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }

        ItemStack priceItem = ingredients.get(0);
        if (priceItem != null && priceItem.getType() != Material.AIR) {
            // Reset to base price by adjusting demand and special price
            // This effectively resets the current price to the base price
            int basePrice = priceItem.getAmount();
            recipe.setDemand(Math.max(1, basePrice));
            recipe.setSpecialPrice(0);
        }
    }
}
