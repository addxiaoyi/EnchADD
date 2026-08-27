package net.enchadd.listeners;

import net.enchadd.listeners.support.GluttonySanitizerSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.enchants.GluttonyEnchant;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class GluttonyListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private Enchantment enchant = registry.get(GluttonyEnchant.KEY);
    private GluttonySanitizerSupport sanitizerSupport = new GluttonySanitizerSupport(enchant);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEnchantItem(EnchantItemEvent event) {
        GluttonySanitizerSupport sanitizerSupport = resolveSanitizerSupport();
        if (!sanitizerSupport.hasEnchant()) return;
        ItemStack item = event.getItem();
        Map<Enchantment, Integer> toAdd = event.getEnchantsToAdd();
        if (sanitizerSupport.shouldSanitizeEnchantRoll(item, toAdd)) {
            sanitizerSupport.sanitizeEnchantRoll(item, toAdd);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        // Prevent exploit: use guard check to ensure both anvil slots have items
        AnvilInventory anvil = event.getInventory();
        ItemStack left = anvil.getFirstItem();
        ItemStack right = anvil.getSecondItem();
        if (!GluttonySanitizerSupport.add_guard_check(left, right)) {
            return;
        }

        ItemStack result = event.getResult();
        if (resolveSanitizerSupport().sanitizeIfGluttonous(result)) {
            event.setResult(result);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        // Prevent exploit: only process when both smithing slots have items
        ItemStack[] contents = event.getInventory().getContents();
        boolean slot0HasItem = contents.length > 0 && contents[0] != null && !contents[0].getType().isAir();
        boolean slot1HasItem = contents.length > 1 && contents[1] != null && !contents[1].getType().isAir();
        if (!slot0HasItem || !slot1HasItem) {
            return;
        }

        ItemStack result = event.getResult();
        if (resolveSanitizerSupport().sanitizeIfGluttonous(result)) {
            event.setResult(result);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        // Prevent exploit: only process Gluttony logic when both grindstone slots have items.
        // Single-slot input should return null (no result) to avoid triggering
        // Paper's GUI sync bug that can cause infinite XP farming.
        GrindstoneInventory grindstone = event.getInventory();
        ItemStack[] contents = grindstone.getContents();
        boolean slot0HasItem = contents.length > 0 && contents[0] != null && !contents[0].getType().isAir();
        boolean slot1HasItem = contents.length > 1 && contents[1] != null && !contents[1].getType().isAir();
        if (!slot0HasItem || !slot1HasItem) {
            return;
        }

        ItemStack result = event.getResult();
        if (resolveSanitizerSupport().sanitizeIfGluttonous(result)) {
            event.setResult(result);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        // Skip grindstone single-slot interactions to prevent XP exploit
        if (event.getInventory() instanceof GrindstoneInventory grindstone) {
            ItemStack[] contents = grindstone.getContents();
            boolean slot0HasItem = contents.length > 0 && contents[0] != null && !contents[0].getType().isAir();
            boolean slot1HasItem = contents.length > 1 && contents[1] != null && !contents[1].getType().isAir();
            if (!slot0HasItem || !slot1HasItem) {
                return;
            }
        }

        ItemStack current = event.getCurrentItem();
        GluttonySanitizerSupport sanitizerSupport = resolveSanitizerSupport();
        if (sanitizerSupport.sanitizeIfGluttonous(current)) {
            event.setCurrentItem(current);
        }

        ItemStack cursor = event.getCursor();
        if (sanitizerSupport.sanitizeIfGluttonous(cursor)) {
            event.setCursor(cursor);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        // Skip grindstone to prevent XP exploit
        if (event.getInventory() instanceof GrindstoneInventory grindstone) {
            ItemStack[] contents = grindstone.getContents();
            boolean slot0HasItem = contents.length > 0 && contents[0] != null && !contents[0].getType().isAir();
            boolean slot1HasItem = contents.length > 1 && contents[1] != null && !contents[1].getType().isAir();
            if (!slot0HasItem || !slot1HasItem) {
                return;
            }
        }

        GluttonySanitizerSupport sanitizerSupport = resolveSanitizerSupport();
        sanitizerSupport.sanitizeInventory(event.getInventory());
        HumanEntity player = event.getPlayer();
        if (player != null) {
            sanitizerSupport.sanitizeInventory(player.getInventory());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        resolveSanitizerSupport().sanitizeInventory(event.getPlayer().getInventory());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        ItemStack stack = event.getItem().getItemStack();
        if (resolveSanitizerSupport().sanitizeIfGluttonous(stack)) {
            event.getItem().setItemStack(stack);
        }
    }

    private GluttonySanitizerSupport resolveSanitizerSupport() {
        if (sanitizerSupport == null || (enchant != null && !sanitizerSupport.hasEnchant())) {
            sanitizerSupport = new GluttonySanitizerSupport(enchant);
        }
        return sanitizerSupport;
    }
}
