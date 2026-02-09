package com.enadd.enchantments.decorative;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Projectile;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;









public final class EnchantmentTriggerManager implements Listener {

    private final JavaPlugin plugin;
    private final ParticleEffectRenderer renderer;
    private final Map<UUID, Set<DecorativeEnchantment>> playerEnchantments;
    private final Map<UUID, Long> lastInteractionTime;

    public EnchantmentTriggerManager(JavaPlugin plugin, ParticleEffectRenderer renderer, ParticleEffectConfig config) {
        this.plugin = plugin;
        this.renderer = renderer;
        this.playerEnchantments = new ConcurrentHashMap<>();
        this.lastInteractionTime = new ConcurrentHashMap<>();
        registerEvents();
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        DecorativeEnchantment enchantment = getDecorativeEnchantment(item);
        if (enchantment == null) return;

        if (!enchantment.shouldTrigger()) return;

        if (!renderer.canTrigger(player)) return;

        handleTrigger(player, item, enchantment, TriggerType.INTERACTION);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) return;

        DecorativeEnchantment enchantment = getDecorativeEnchantment(item);
        if (enchantment == null) return;

        if (!enchantment.shouldTrigger()) return;

        if (!renderer.canTrigger(player)) return;

        handleTrigger(player, item, enchantment, TriggerType.ATTACK);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();

        if (bow == null) return;

        DecorativeEnchantment enchantment = getDecorativeEnchantment(bow);
        if (enchantment == null) return;

        if (!enchantment.shouldTrigger()) return;

        if (!renderer.canTrigger(player)) return;

        handleTrigger(player, bow, enchantment, TriggerType.SHOOT);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProjectileLaunch(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        if (!isProjectileWeapon(item)) return;

        DecorativeEnchantment enchantment = getDecorativeEnchantment(item);
        if (enchantment == null) return;

        if (!enchantment.shouldTrigger()) return;

        if (!renderer.canTrigger(player)) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            handleTrigger(player, item, enchantment, TriggerType.PROJECTILE);
        }, 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDurabilityChange(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        DecorativeEnchantment enchantment = getDecorativeEnchantment(item);
        if (enchantment == null) return;

        if (!enchantment.shouldTrigger()) return;

        if (!renderer.canTrigger(player)) return;

        handleTrigger(player, item, enchantment, TriggerType.DURABILITY_LOSS);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnvilRepair(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack brokenItem = event.getBrokenItem();

        if (brokenItem == null) return;

        DecorativeEnchantment enchantment = getDecorativeEnchantment(brokenItem);
        if (enchantment == null) return;

        handleTrigger(player, brokenItem, enchantment, TriggerType.DURABILITY_BREAK);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile)) return;

        Projectile projectile = (Projectile) event.getDamager();
        ProjectileSource source = projectile.getShooter();

        if (!(source instanceof Player)) return;

        Player player = (Player) source;
        ItemStack bow = player.getInventory().getItemInMainHand();

        if (bow == null || !isProjectileWeapon(bow)) return;

        DecorativeEnchantment enchantment = getDecorativeEnchantment(bow);
        if (enchantment == null) return;

        if (!enchantment.shouldTrigger()) return;

        if (!renderer.canTrigger(player)) return;

        handleTrigger(player, bow, enchantment, TriggerType.PROJECTILE_HIT);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null) return;

        DecorativeEnchantment enchantment = getDecorativeEnchantment(clickedItem);
        if (enchantment == null) return;

        if (!enchantment.shouldTrigger()) return;

        if (!renderer.canTrigger(player)) return;

        handleTrigger(player, clickedItem, enchantment, TriggerType.INVENTORY_INTERACT);
    }

    private void handleTrigger(Player player, ItemStack item, DecorativeEnchantment enchantment, TriggerType triggerType) {
        enchantment.recordTrigger();
        renderer.recordTrigger(player);

        renderer.startEffect(
            player.getLocation(),
            enchantment.getType(),
            player
        );

        lastInteractionTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private boolean isProjectileWeapon(ItemStack item) {
        Material type = item.getType();
        return type == Material.BOW ||
               type == Material.CROSSBOW ||
               type == Material.TRIDENT;
    }

    private DecorativeEnchantment getDecorativeEnchantment(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        if (!com.enadd.util.ItemMetaHelper.hasLore(meta)) return null;

        List<String> lore = com.enadd.util.ItemMetaHelper.getLoreAsStrings(meta);
        if (lore == null) return null;

        for (String line : lore) {
            if (line.contains("§5§l=== EnCh Add ===")) {
                return extractEnchantmentFromLore(line, lore);
            }
        }

        return null;
    }

    private DecorativeEnchantment extractEnchantmentFromLore(String triggerLine, List<String> fullLore) {
        for (String line : fullLore) {
            for (DecorativeEnchantmentType type : DecorativeEnchantmentType.values()) {
                if (line.contains(type.getDisplayName())) {
                    String levelPart = line.replace(type.getDisplayName(), "").trim();
                    int level = extractLevelFromRoman(levelPart);

                    return new DecorativeEnchantment(type, level);
                }
            }
        }
        return null;
    }

    private int extractLevelFromRoman(String roman) {
        Map<Character, Integer> romanValues = new HashMap<>();
        romanValues.put('I', 1);
        romanValues.put('V', 5);
        romanValues.put('X', 10);
        romanValues.put('L', 50);
        romanValues.put('C', 100);
        romanValues.put('D', 500);
        romanValues.put('M', 1000);

        int result = 0;
        int prevValue = 0;

        for (int i = roman.length() - 1; i >= 0; i--) {
            int value = romanValues.getOrDefault(roman.charAt(i), 0);
            if (value < prevValue) {
                result -= value;
            } else {
                result += value;
                prevValue = value;
            }
        }

        return Math.max(1, Math.min(10, result));
    }

    public void registerEnchantment(Player player, ItemStack item, DecorativeEnchantment enchantment) {
        UUID playerId = player.getUniqueId();
        playerEnchantments.computeIfAbsent(playerId, k -> new HashSet<>()).add(enchantment);
    }

    public void unregisterEnchantment(Player player, ItemStack item) {
        UUID playerId = player.getUniqueId();
        Set<UUID> toRemove = new HashSet<>();

        Set<DecorativeEnchantment> enchantments = playerEnchantments.get(playerId);
        if (enchantments != null) {
            for (DecorativeEnchantment enchant : enchantments) {
                toRemove.add(enchant.getId());
            }
        }

        toRemove.forEach(id -> {
            Set<DecorativeEnchantment> set = playerEnchantments.get(playerId);
            if (set != null) {
                set.removeIf(e -> e.getId().equals(id));
            }
        });
    }

    public void clearPlayerEnchantments(Player player) {
        playerEnchantments.remove(player.getUniqueId());
    }

    public boolean hasDecorativeEnchantment(ItemStack item) {
        return getDecorativeEnchantment(item) != null;
    }

    private enum TriggerType {
        INTERACTION,
        ATTACK,
        SHOOT,
        PROJECTILE,
        PROJECTILE_HIT,
        DURABILITY_LOSS,
        DURABILITY_BREAK,
        INVENTORY_INTERACT
    }
}




