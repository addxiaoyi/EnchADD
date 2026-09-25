package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.TrawlerEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import net.enchadd.utils.PerformanceUtils;

public class TrawlerListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(TrawlerEnchant.KEY);
    private final NamespacedKey cooldownKey = PerformanceUtils.namespacedKey(TrawlerEnchant.KEY);

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item itemEntity)) return;
        Player player = event.getPlayer();
        if (enchant == null) return;
        ItemStack rod = getRodWithEnchant(player);
        if (rod == null) return;
        ItemStack stack = itemEntity.getItemStack();
        if (stack.getAmount() <= 0) return;
        if (!(EnchADDConfig.ENCHANTS.get(TrawlerEnchant.KEY) instanceof TrawlerEnchant trawler)) return;
        Entity hook = event.getHook();
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(hook);
        if (pdc == null) return;
        if (PerformanceUtils.isOnCooldown(pdc, cooldownKey, trawler.getCooldownTicks())) return;
        int level = net.enchadd.utils.EnchantCache.getLevel(rod, enchant);
        if (!shouldTrigger(level, trawler)) return;
        int max = stack.getMaxStackSize();
        int extra = level * trawler.getExtraItemsPerLevel();
        if (!canIncreaseAmount(stack, max, extra)) return;
        int newAmount = Math.min(max, stack.getAmount() + extra);
        stack.setAmount(newAmount);
        itemEntity.setItemStack(stack);
        PerformanceUtils.setCooldown(pdc, cooldownKey);
    }

    private ItemStack getRodWithEnchant(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (!main.getType().isAir() && net.enchadd.utils.EnchantCache.getLevel(main, enchant) > 0) {
            return main;
        }
        ItemStack off = player.getInventory().getItemInOffHand();
        if (!off.getType().isAir() && net.enchadd.utils.EnchantCache.getLevel(off, enchant) > 0) {
            return off;
        }
        return null;
    }

    private boolean shouldTrigger(int level, TrawlerEnchant trawler) {
        if (level <= 0) return false;
        double chance = trawler.getTriggerChance() * level;
        if (!Double.isFinite(chance)) return false;
        chance = Math.min(0.75d, Math.max(0.0d, Math.min(trawler.getMaxTriggerChance(), chance)));
        if (chance <= 0) return false;
        return PerformanceUtils.rollChance(chance);
    }

    private boolean canIncreaseAmount(ItemStack stack, int max, int extra) {
        if (extra <= 0) return false;
        if (max <= 1) return false;
        return stack.getAmount() < max;
    }
}
