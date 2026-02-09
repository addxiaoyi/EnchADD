package com.enadd.achievements;

import com.enadd.i18n.MessageManager;
import com.enadd.memory.MemoryLeakDetector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;



/**
 * 成就系统管理器
 *
 * 修复：
 * - 内存泄露：正确注销监听器
 * - 硬编码：使用MessageManager
 * - 原版集成：使用VanillaAchievementRegistry
 */
public final class AchievementManager implements Listener {

    private static volatile AchievementManager instance;
    private static final Object LOCK = new Object();
    private final JavaPlugin plugin;
    private final VanillaAchievementRegistry vanillaRegistry;

    private final Map<UUID, PlayerAchievementData> playerData = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> pendingChecks = new ConcurrentHashMap<>();
    private final Set<UUID> dirtyPlayers = ConcurrentHashMap.newKeySet();
    
    // 速率限制 - 防止刷成就
    private final Map<UUID, Long> lastTreeCut = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastSniperShot = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastBossKill = new ConcurrentHashMap<>();
    private static final long MIN_TREE_CUT_INTERVAL = 500; // 0.5秒
    private static final long MIN_SNIPER_SHOT_INTERVAL = 1000; // 1秒
    private static final long MIN_BOSS_KILL_INTERVAL = 2000; // 2秒

    private final Map<String, Achievement> achievements = new HashMap<>();

    private final AtomicBoolean batchProcessing = new AtomicBoolean(false);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    private AchievementManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.vanillaRegistry = new VanillaAchievementRegistry(plugin);
        
        // 初始化成就检查器注册表
        com.enadd.achievements.checker.AchievementCheckerRegistry.initializeDefaultCheckers();
        
        initializeAchievements();

        // 注册到原版系统
        vanillaRegistry.registerAllAchievements();
    }

    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    AchievementManager temp = new AchievementManager(plugin);
                    Bukkit.getPluginManager().registerEvents(temp, plugin);

                    // 注册到内存泄露检测器
                    if (MemoryLeakDetector.getInstance() != null) {
                        MemoryLeakDetector.getInstance().registerListener(temp);
                    }

                    instance = temp;
                    instance.startMaintenanceTask();
                }
            }
        }
    }

    public static AchievementManager getInstance() {
        return instance;
    }

    public static void shutdown() {
        if (instance != null) {
            synchronized (LOCK) {
                if (instance != null) {
                    instance.performShutdown();
                    instance = null;
                }
            }
        }
    }

    private void startMaintenanceTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (shutdown.get()) return;

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID playerId = player.getUniqueId();
                PlayerAchievementData data = playerData.get(playerId);
                if (data == null) continue;

                // 1. Curse Master: Survive 1 hour with 10 curses
                checkCurseMaster(player, data);

                // 2. Treasure Hunter: Find 50 treasure enchantments
                checkTreasureHunter(player, data);
            }
        }, 1200L, 1200L); // Every 1 minute
    }

    private void checkCurseMaster(Player player, PlayerAchievementData data) {
        if (data.hasAchievement("curse_master")) return;

        int curseCount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                for (Enchantment ench : item.getEnchantments().keySet()) {
                    if (ench.getKey().getKey().startsWith("curse_")) {
                        curseCount++;
                    }
                }
            }
        }

        if (curseCount >= 10) {
            long startTime = data.getCurseStartTime();
            if (startTime == 0) {
                data.setCurseStartTime(System.currentTimeMillis());
            } else if (System.currentTimeMillis() - startTime >= 3600000) { // 1 hour
                Bukkit.getScheduler().runTask(plugin, () -> awardAchievement(player, "curse_master"));
            }
        } else {
            data.setCurseStartTime(0);
        }
    }

    @SuppressWarnings("deprecation")
    private void checkTreasureHunter(Player player, PlayerAchievementData data) {
        if (data.hasAchievement("treasure_hunter")) return;

        Set<String> treasures = new HashSet<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                for (Enchantment ench : item.getEnchantments().keySet()) {
                    // 使用 Paper API 的新方式检查是否为宝藏附魔
                    if (ench.getKey().getNamespace().equals("minecraft") && 
                        (ench.getKey().getKey().equals("mending") || 
                         ench.getKey().getKey().equals("frost_walker") || 
                         ench.getKey().getKey().equals("swift_sneak") ||
                         ench.getKey().getKey().equals("soul_speed"))) {
                        treasures.add(ench.getKey().getKey());
                    } else if (ench instanceof com.enadd.enchantments.BaseEnchantment && ((com.enadd.enchantments.BaseEnchantment)ench).isTreasure()) {
                        treasures.add(ench.getKey().getKey());
                    }
                }
            }
        }

        if (treasures.size() > 0) {
            int current = data.getCounter("treasures_found");
            if (treasures.size() > current) {
                data.setCounter("treasures_found", treasures.size());
                if (treasures.size() >= 50) {
                    Bukkit.getScheduler().runTask(plugin, () -> awardAchievement(player, "treasure_hunter"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (shutdown.get()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getEnchantments().isEmpty()) return;

        UUID playerId = player.getUniqueId();
        Map<Enchantment, Integer> enchants = new HashMap<>(item.getEnchantments());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!shutdown.get()) {
                for (Enchantment ench : enchants.keySet()) {
                    trackSeenEnchantment(player, ench);
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (shutdown.get()) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            // Potential fatal hit
            ItemStack[] armor = player.getInventory().getArmorContents();
            boolean hasLastStand = false;
            for (ItemStack item : armor) {
                if (item != null) {
                    Enchantment lastStand = Registry.ENCHANTMENT.get(TypedKey.create(RegistryKey.ENCHANTMENT, NamespacedKey.fromString("enadd:last_stand")));
                    if (lastStand != null && item.containsEnchantment(lastStand)) {
                        hasLastStand = true;
                        break;
                    }
                }
            }

            if (hasLastStand) {
                UUID playerId = player.getUniqueId();
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    PlayerAchievementData data = playerData.get(playerId);
                    if (data != null && !data.hasAchievement("immortal")) {
                        int count = data.incrementCounter("last_stand_saves");
                        if (count >= 100) {
                            Bukkit.getScheduler().runTask(plugin, () -> awardAchievement(player, "immortal"));
                        }
                    }
                });
            }
        }
    }

    public void trackSeenEnchantment(Player player, String key) {
        if (shutdown.get() || player == null || key == null) return;
        UUID playerId = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerAchievementData data = playerData.get(playerId);
            if (data != null && !data.hasAchievement("enchantment_scholar")) {
                data.trackSeenEnchantment(key);
                if (data.getSeenEnchantmentsCount() >= 226) {
                    Bukkit.getScheduler().runTask(plugin, () -> awardAchievement(player, "enchantment_scholar"));
                }
            }
        });
    }

    public void trackSeenEnchantment(Player player, Enchantment enchantment) {
        if (enchantment == null) return;
        trackSeenEnchantment(player, enchantment.getKey().getKey());
    }

    private void performShutdown() {
        try {
            shutdown.set(true);

            // 修复内存泄露：正确注销所有事件监听器
            HandlerList.unregisterAll(this);

            // 通知内存泄露检测器
            if (MemoryLeakDetector.getInstance() != null) {
                MemoryLeakDetector.getInstance().unregisterListener(this);
            }

            // 清理所有数据
            playerData.clear();
            pendingChecks.clear();
            dirtyPlayers.clear();
            achievements.clear();

        } catch (Exception e) {
            plugin.getLogger().warning("Error during AchievementManager shutdown: " + e.getMessage());
        }
    }

    private void initializeAchievements() {
        // BUG FIX #13: 添加null检查
        MessageManager msg = MessageManager.getInstance();
        if (msg == null) {
            plugin.getLogger().warning("MessageManager not initialized, using default messages");
            msg = MessageManager.getInstance(); // 尝试再次获取
            if (msg == null) {
                plugin.getLogger().severe("Failed to initialize MessageManager, achievements may not work correctly");
                return;
            }
        }

        // 附魔大师
        achievements.put("enchantment_master", new Achievement(
            "enchantment_master",
            msg.getMessage("achievement.enchantment_master.title"),
            msg.getMessage("achievement.enchantment_master.desc"),
            AchievementType.COLLECTION,
            51
        ));

        // 诅咒战士
        achievements.put("cursed_warrior", new Achievement(
            "cursed_warrior",
            msg.getMessage("achievement.cursed_warrior.title"),
            msg.getMessage("achievement.cursed_warrior.desc"),
            AchievementType.CHALLENGE,
            1
        ));

        // 一击必杀
        achievements.put("one_shot_kill", new Achievement(
            "one_shot_kill",
            msg.getMessage("achievement.one_shot_kill.title"),
            msg.getMessage("achievement.one_shot_kill.desc"),
            AchievementType.COMBAT,
            1
        ));

        // 伐木工
        achievements.put("lumberjack", new Achievement(
            "lumberjack",
            msg.getMessage("achievement.lumberjack.title"),
            msg.getMessage("achievement.lumberjack.desc"),
            AchievementType.MINING,
            1000
        ));

        // 神射手
        achievements.put("marksman", new Achievement(
            "marksman",
            msg.getMessage("achievement.marksman.title"),
            msg.getMessage("achievement.marksman.desc"),
            AchievementType.COMBAT,
            100
        ));

        // 新增10个成就
        achievements.put("binder_master", new Achievement(
            "binder_master",
            "Binder Master",
            "Use Enchantment Binder to combine 6 conflicting enchantments",
            AchievementType.CHALLENGE,
            1
        ));

        achievements.put("conflict_breaker", new Achievement(
            "conflict_breaker",
            "Conflict Breaker",
            "Combine Sharpness, Smite, and Bane of Arthropods on one weapon",
            AchievementType.CHALLENGE,
            1
        ));

        achievements.put("ultimate_warrior", new Achievement(
            "ultimate_warrior",
            "Ultimate Warrior",
            "Defeat 1000 enemies with enchanted weapons",
            AchievementType.COMBAT,
            1000
        ));

        achievements.put("treasure_hunter", new Achievement(
            "treasure_hunter",
            "Treasure Hunter",
            "Find 50 treasure enchantments",
            AchievementType.COLLECTION,
            50
        ));

        achievements.put("enchantment_scholar", new Achievement(
            "enchantment_scholar",
            "Enchantment Scholar",
            "Read all enchantment descriptions",
            AchievementType.COLLECTION,
            226
        ));

        achievements.put("curse_master", new Achievement(
            "curse_master",
            "Curse Master",
            "Survive 1 hour with 10 curses active",
            AchievementType.CHALLENGE,
            1
        ));

        achievements.put("legendary_smith", new Achievement(
            "legendary_smith",
            "Legendary Smith",
            "Use anvil 500 times to combine enchantments",
            AchievementType.COLLECTION,
            500
        ));

        achievements.put("elemental_master", new Achievement(
            "elemental_master",
            "Elemental Master",
            "Use Fire, Ice, Lightning, and Poison enchantments",
            AchievementType.CHALLENGE,
            1
        ));

        achievements.put("immortal", new Achievement(
            "immortal",
            "Immortal",
            "Survive 100 fatal hits with Last Stand enchantment",
            AchievementType.CHALLENGE,
            100
        ));

        achievements.put("god_slayer", new Achievement(
            "god_slayer",
            "God Slayer",
            "Defeat Ender Dragon, Wither, and Warden with enchanted weapons",
            AchievementType.CHALLENGE,
            1
        ));

        // 附魔收藏家
        achievements.put("collector", new Achievement(
            "collector",
            "Enchantment Collector",
            "Collect all 239 enchantments",
            AchievementType.COLLECTION,
            239
        ));

        // 完美附魔
        achievements.put("perfect_enchantment", new Achievement(
            "perfect_enchantment",
            "Perfect Enchantment",
            "Create an item with 10+ level X enchantments",
            AchievementType.CHALLENGE,
            1
        ));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (shutdown.get()) return;

        ItemStack result = event.getResult();
        if (result == null || result.getEnchantments().isEmpty()) return;

        if (!(event.getView().getPlayer() instanceof Player)) return;
        Player player = (Player) event.getView().getPlayer();
        UUID playerId = player.getUniqueId();
        Map<Enchantment, Integer> enchants = new HashMap<>(result.getEnchantments());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!shutdown.get()) {
                checkAnvilAchievements(playerId, enchants);
            }
        });
    }

    private void checkAnvilAchievements(UUID playerId, Map<Enchantment, Integer> enchants) {
        try {
            PlayerAchievementData data = playerData.get(playerId);
            if (data == null) return;

            // 1. Binder Master: 粘合剂组合了6个冲突附魔
            if (!data.hasAchievement("binder_master")) {
                int binderLevel = 0;
                for (Enchantment e : enchants.keySet()) {
                    if (e.getKey().getKey().equals("enchantment_binder")) {
                        binderLevel = enchants.get(e);
                        break;
                    }
                }

                if (binderLevel >= 3) {
                    int conflicts = com.enadd.enchantments.conflict.EnchantmentCompatibilityChecker.countConflicts(enchants.keySet());
                    if (conflicts >= 6) {
                        Player player = Bukkit.getPlayer(playerId);
                        if (player != null && player.isOnline()) {
                            awardAchievement(player, "binder_master");
                        }
                    }
                }
            }

            // 2. Conflict Breaker: 同时拥有 锋利、亡灵杀手、节肢杀手
            if (!data.hasAchievement("conflict_breaker")) {
                Set<String> keys = new HashSet<>();
                enchants.keySet().forEach(e -> keys.add(e.getKey().getKey()));
                if (keys.contains("sharpness") && keys.contains("smite") && keys.contains("bane_of_arthropods")) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        awardAchievement(player, "conflict_breaker");
                    }
                }
            }

            // 3. Perfect Enchantment: 10个以上的 X 级附魔
            if (!data.hasAchievement("perfect_enchantment")) {
                int countX = 0;
                for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    if (entry.getValue() >= 10) {
                        countX++;
                    }
                }
                if (countX >= 10) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        awardAchievement(player, "perfect_enchantment");
                    }
                }
            }

            // 4. Legendary Smith: 使用铁砧500次
            if (!data.hasAchievement("legendary_smith")) {
                int count = data.incrementCounter("anvil_uses");
                if (count >= 500) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        awardAchievement(player, "legendary_smith");
                    }
                }
            }

        } catch (Exception e) {
            // Silent
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (shutdown.get()) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (!playerData.containsKey(playerId)) {
            playerData.put(playerId, new PlayerAchievementData());
        }
        pendingChecks.putIfAbsent(playerId, ConcurrentHashMap.newKeySet());

        // Capture data on main thread
        Map<String, Integer> enchants = capturePlayerEnchantments(player);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!shutdown.get()) {
                checkEnchantmentMaster(playerId, enchants);
                
                // Track seen enchantments for Enchantment Scholar
                PlayerAchievementData data = playerData.get(playerId);
                if (data != null) {
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && !item.getEnchantments().isEmpty()) {
                            for (Enchantment ench : item.getEnchantments().keySet()) {
                                trackSeenEnchantment(player, ench);
                            }
                        }
                    }
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (shutdown.get()) return;

        UUID playerId = event.getPlayer().getUniqueId();
        pendingChecks.remove(playerId);
        dirtyPlayers.remove(playerId);
        
        // BUG FIX #8: 清理速率限制Map，防止内存泄露
        lastTreeCut.remove(playerId);
        lastSniperShot.remove(playerId);
        lastBossKill.remove(playerId);

        // Clean up player data after 5 minutes to prevent memory leaks
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                playerData.remove(playerId);
            }
        }, 6000L); // 5 minutes
    }

    public void markPlayerDirty(UUID playerId) {
        dirtyPlayers.add(playerId);
        scheduleBatchCheck();
    }

    private void scheduleBatchCheck() {
        if (batchProcessing.compareAndSet(false, true)) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::processDirtyPlayers);
        }
    }

    private void processDirtyPlayers() {
        try {
            // BUG FIX #12: 使用快照副本避免并发修改
            Set<UUID> currentDirty = new HashSet<>(dirtyPlayers);
            dirtyPlayers.removeAll(currentDirty);

            for (UUID playerId : currentDirty) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    Set<String> checks = pendingChecks.remove(playerId);
                    if (checks != null) {
                        for (String check : checks) {
                            processPendingCheck(player, check);
                        }
                    }
                }
            }
        } finally {
            batchProcessing.set(false);
        }
    }

    private void processPendingCheck(Player player, String checkType) {
        switch (checkType) {
            case "enchantment":
                checkEnchantmentMaster(player.getUniqueId(), capturePlayerEnchantments(player));
                break;
            case "equipment":
                checkEquipmentAchievements(player);
                break;
        }
    }

    private void checkEquipmentAchievements(Player player) {
        PlayerAchievementData data = playerData.get(player.getUniqueId());
        if (data == null) return;

        // 使用注册表检查诅咒战士成就
        if (com.enadd.achievements.checker.AchievementCheckerRegistry.checkAchievement(player, data, "cursed_warrior")) {
            awardAchievement(player, "cursed_warrior");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (shutdown.get()) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getEnchantments().isEmpty()) return;

        // Capture data on main thread
        UUID killerId = killer.getUniqueId();
        Map<Enchantment, Integer> enchants = new HashMap<>(weapon.getEnchantments());
        org.bukkit.entity.Entity victim = event.getEntity();
        EntityType victimType = victim.getType();

        // Check achievements (async to avoid TPS impact)
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!shutdown.get()) {
                checkExecutionKill(killerId, victim, enchants);
                checkUltimateWarrior(killerId);
                checkGodSlayer(killerId, victimType);
            }
        });
    }

    private void checkUltimateWarrior(UUID playerId) {
        PlayerAchievementData data = playerData.get(playerId);
        if (data != null && !data.hasAchievement("ultimate_warrior")) {
            int count = data.incrementCounter("ultimate_warrior_kills");
            if (count >= 1000) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    Bukkit.getScheduler().runTask(plugin, () -> awardAchievement(player, "ultimate_warrior"));
                }
            }
        }
    }

    private void checkGodSlayer(UUID playerId, EntityType victimType) {
        PlayerAchievementData data = playerData.get(playerId);
        if (data != null && !data.hasAchievement("god_slayer")) {
            if (victimType == EntityType.ENDER_DRAGON || victimType == EntityType.WITHER || victimType == EntityType.WARDEN) {
                data.trackBossKill(victimType.name());
                if (data.hasKilledBoss(EntityType.ENDER_DRAGON.name()) &&
                    data.hasKilledBoss(EntityType.WITHER.name()) &&
                    data.hasKilledBoss(EntityType.WARDEN.name())) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        Bukkit.getScheduler().runTask(plugin, () -> awardAchievement(player, "god_slayer"));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (shutdown.get()) return;

        Player player = event.getPlayer();
        if (player == null) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || tool.getEnchantments().isEmpty()) return;

        // Check for tree feller achievement (async)
        if (isWoodBlock(event.getBlock().getType())) {
            UUID playerId = player.getUniqueId();
            PlayerAchievementData data = playerData.get(playerId);
            if (data != null && !data.hasAchievement("lumberjack")) {
                Map<Enchantment, Integer> enchants = new HashMap<>(tool.getEnchantments());

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    if (!shutdown.get()) {
                        checkTreeFellerProgress(playerId, enchants);
                        
                        // 使用注册表统一检查
                        if (com.enadd.achievements.checker.AchievementCheckerRegistry.checkAchievement(player, data, "lumberjack")) {
                            awardAchievement(player, "lumberjack");
                        }
                    }
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (shutdown.get()) return;

        if (event.getDamager() instanceof Player) {
            Player shooter = (Player) event.getDamager();
            ItemStack weapon = shooter.getInventory().getItemInMainHand();
            if (weapon == null || weapon.getEnchantments().isEmpty()) return;

            UUID shooterId = shooter.getUniqueId();
            Map<Enchantment, Integer> enchants = new HashMap<>(weapon.getEnchantments());

            // Check for sniper achievement
            double distance = shooter.getLocation().distance(event.getEntity().getLocation());

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                if (!shutdown.get()) {
                    checkSniperShot(shooterId, distance, enchants);
                    checkElementalMaster(shooterId, enchants);
                    
                    // 使用注册表统一检查 marksman
                    PlayerAchievementData data = playerData.get(shooterId);
                    if (data != null && com.enadd.achievements.checker.AchievementCheckerRegistry.checkAchievement(shooter, data, "marksman")) {
                        awardAchievement(shooter, "marksman");
                    }
                }
            });
        }
    }

    private void checkElementalMaster(UUID playerId, Map<Enchantment, Integer> enchants) {
        PlayerAchievementData data = playerData.get(playerId);
        if (data != null && !data.hasAchievement("elemental_master")) {
            for (Enchantment ench : enchants.keySet()) {
                String key = ench.getKey().getKey().toLowerCase();
                if (key.contains("fire") || key.contains("ice") || key.contains("lightning") || key.contains("poison")) {
                    if (key.contains("fire")) data.trackUsedElement("fire");
                    if (key.contains("ice")) data.trackUsedElement("ice");
                    if (key.contains("lightning")) data.trackUsedElement("lightning");
                    if (key.contains("poison")) data.trackUsedElement("poison");
                }
            }

            if (data.hasUsedElement("fire") && data.hasUsedElement("ice") &&
                data.hasUsedElement("lightning") && data.hasUsedElement("poison")) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    Bukkit.getScheduler().runTask(plugin, () -> awardAchievement(player, "elemental_master"));
                }
            }
        }
    }

    private void checkEnchantmentMaster(UUID playerId, Map<String, Integer> uniqueEnchantments) {
        try {
            PlayerAchievementData data = playerData.get(playerId);
            if (data == null || data.hasAchievement("enchantment_master")) return;

            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) return;

            // 使用注册表检查附魔大师成就
            if (com.enadd.achievements.checker.AchievementCheckerRegistry.checkAchievement(player, data, "enchantment_master")) {
                awardAchievement(player, "enchantment_master");
            }

            // 检查收藏家成就 (226+ 个附魔)
            if (!data.hasAchievement("collector")) {
                if (uniqueEnchantments.size() >= 226) {
                    awardAchievement(player, "collector");
                }
            }

        } catch (Exception e) {
            // Silent error handling to prevent TPS impact
        }
    }

    private Map<String, Integer> capturePlayerEnchantments(Player player) {
        Map<String, Integer> enchants = new HashMap<>();

        // Check all equipment slots
        ItemStack[] equipment = {
            player.getInventory().getHelmet(),
            player.getInventory().getChestplate(),
            player.getInventory().getLeggings(),
            player.getInventory().getBoots(),
            player.getInventory().getItemInMainHand(),
            player.getInventory().getItemInOffHand()
        };

        for (ItemStack item : equipment) {
            if (item != null) {
                item.getEnchantments().keySet().forEach(enchant -> {
                    String key = enchant.getKey().toString();
                    if (key.startsWith("enadd:")) {
                        enchants.put(key, item.getEnchantmentLevel(enchant));
                    }
                });
            }
        }

        // Check inventory for enchanted books/items
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                item.getEnchantments().keySet().forEach(enchant -> {
                    String key = enchant.getKey().toString();
                    if (key.startsWith("enadd:")) {
                        enchants.put(key, item.getEnchantmentLevel(enchant));
                    }
                });
            }
        }
        return enchants;
    }

    public void resetAchievements(Player player) {
        UUID playerId = player.getUniqueId();
        playerData.put(playerId, new PlayerAchievementData());
        pendingChecks.put(playerId, ConcurrentHashMap.newKeySet());
        dirtyPlayers.remove(playerId);
    }
    
    /**
     * 重置玩家成就（带权限检查）
     * 
     * @param player 目标玩家
     * @param requester 请求者（用于权限检查）
     * @return 是否成功重置
     */
    public boolean resetAchievementsWithPermission(Player player, CommandSender requester) {
        // 权限检查
        if (requester != null && !requester.hasPermission("enchadd.achievements.admin")) {
            return false;
        }
        
        resetAchievements(player);
        return true;
    }

    private void checkExecutionKill(UUID killerId, org.bukkit.entity.Entity victim, Map<Enchantment, Integer> enchants) {
        try {
            PlayerAchievementData data = playerData.get(killerId);
            if (data == null || data.hasAchievement("one_shot_kill")) return;

            // 速率限制检查
            Long lastTime = lastBossKill.get(killerId);
            long currentTime = System.currentTimeMillis();
            
            if (lastTime != null && (currentTime - lastTime) < MIN_BOSS_KILL_INTERVAL) {
                return; // 太快了，可能在刷
            }

            // Check if weapon has execution enchantment and victim is a boss
            boolean hasExecution = enchants.keySet().stream()
                .anyMatch(enchant -> enchant.getKey().toString().equals("enadd:execution"));

            if (hasExecution && isBoss(victim)) {
                lastBossKill.put(killerId, currentTime);
                
                Player killer = Bukkit.getPlayer(killerId);
                if (killer != null && killer.isOnline()) {
                    awardAchievement(killer, "one_shot_kill");
                }
            }

        } catch (Exception e) {
            // Silent error handling
        }
    }

    private void checkTreeFellerProgress(UUID playerId, Map<Enchantment, Integer> enchants) {
        try {
            PlayerAchievementData data = playerData.get(playerId);
            if (data == null || data.hasAchievement("lumberjack")) return;

            boolean hasTreeFeller = enchants.keySet().stream()
                .anyMatch(enchant -> enchant.getKey().toString().equals("enadd:arbor_master"));

            if (hasTreeFeller) {
                // 速率限制检查
                Long lastTime = lastTreeCut.get(playerId);
                long currentTime = System.currentTimeMillis();
                
                if (lastTime != null && (currentTime - lastTime) < MIN_TREE_CUT_INTERVAL) {
                    return; // 太快了，可能在刷
                }
                
                lastTreeCut.put(playerId, currentTime);
                
                int newCount = data.incrementCounter("trees_cut");
                if (newCount >= 1000) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        awardAchievement(player, "lumberjack");
                    }
                }
            }

        } catch (Exception e) {
            // Silent error handling
        }
    }

    private void checkSniperShot(UUID shooterId, double distance, Map<Enchantment, Integer> enchants) {
        try {
            PlayerAchievementData data = playerData.get(shooterId);
            if (data == null || data.hasAchievement("marksman")) return;

            boolean hasSniper = enchants.keySet().stream()
                .anyMatch(enchant -> enchant.getKey().toString().equals("enadd:sniper"));

            if (hasSniper) {
                if (distance >= 50.0) {
                    // 速率限制检查
                    Long lastTime = lastSniperShot.get(shooterId);
                    long currentTime = System.currentTimeMillis();
                    
                    if (lastTime != null && (currentTime - lastTime) < MIN_SNIPER_SHOT_INTERVAL) {
                        return; // 太快了，可能在刷
                    }
                    
                    lastSniperShot.put(shooterId, currentTime);
                    
                    int newCount = data.incrementCounter("sniper_shots");
                    if (newCount >= 100) {
                        Player shooter = Bukkit.getPlayer(shooterId);
                        if (shooter != null && shooter.isOnline()) {
                            awardAchievement(shooter, "marksman");
                        }
                    }
                }
            }

        } catch (Exception e) {
            // Silent error handling
        }
    }

    private void awardAchievement(Player player, String achievementId) {
        try {
            PlayerAchievementData data = playerData.get(player.getUniqueId());
            if (data == null) return;

            data.awardAchievement(achievementId);
            Achievement achievement = achievements.get(achievementId);

            if (achievement != null) {
                // BUG FIX #14: 添加null检查
                if (vanillaRegistry != null) {
                    // 授予原版成就
                    vanillaRegistry.grantAchievement(player, achievementId);
                }

                // Run on main thread for player interaction
                Bukkit.getScheduler().runTask(plugin, () -> {
                    showAchievementNotification(player, achievement);
                    broadcastAchievement(player, achievement);
                });
            }

        } catch (Exception e) {
            // Silent error handling
        }
    }

    private void showAchievementNotification(Player player, Achievement achievement) {
        try {
            MessageManager msg = MessageManager.getInstance();

            // Purple achievement toast using Adventure API
            Component title = Component.text(msg.getMessage("achievement.unlocked"))
                .color(TextColor.fromHexString("#FF55FF"));
            Component subtitle = Component.text(achievement.getTitle())
                .color(TextColor.fromHexString("#AA55FF"));

            @SuppressWarnings("deprecation")
            Title adventureTitle = Title.title(title, subtitle,
                Title.Times.of(
                    java.time.Duration.ofMillis(500),
                    java.time.Duration.ofMillis(3500),
                    java.time.Duration.ofMillis(1000)
                ));
            player.showTitle(adventureTitle);

            // Achievement sound effect
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

            // Chat message with hover effect using Adventure API
            Component chatMessage = Component.text("[").color(TextColor.fromHexString("#AA55FF"))
                .append(Component.text("✦").color(TextColor.fromHexString("#FF55FF")))
                .append(Component.text("] ").color(TextColor.fromHexString("#AA55FF")))
                .append(Component.text(achievement.getTitle()).color(TextColor.fromHexString("#AA55FF")))
                .append(Component.text(": ").color(TextColor.fromHexString("#777777")))
                .append(Component.text(achievement.getDescription()).color(TextColor.fromHexString("#AAAAAA")));
            player.sendMessage(chatMessage);

        } catch (Exception e) {
            // Fallback to simple message
            com.enadd.util.ErrorHandler.handleException(null, "Achievement notification", e);
            player.sendMessage(Component.text("Achievement Unlocked: ").color(TextColor.fromHexString("#FF55FF"))
                .append(Component.text(achievement.getTitle()).color(TextColor.fromHexString("#AA55FF"))));
        }
    }

    private void broadcastAchievement(Player player, Achievement achievement) {
        try {
            MessageManager msg = MessageManager.getInstance();

            Component broadcastComponent = Component.text("[").color(TextColor.fromHexString("#FF55FF"))
                .append(Component.text("✦").color(TextColor.fromHexString("#AA55FF")))
                .append(Component.text("] ").color(TextColor.fromHexString("#FF55FF")))
                .append(Component.text(player.getName()).color(TextColor.fromHexString("#55AAFF")))
                .append(Component.text(" " + msg.getMessage("achievement.earned") + " ").color(TextColor.fromHexString("#AAAAAA")))
                .append(Component.text(achievement.getTitle()).color(TextColor.fromHexString("#FF55FF")));
            Bukkit.broadcast(broadcastComponent);

            // Play sound for all players
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (!p.equals(player)) {
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
                }
            });

        } catch (Exception e) {
            // Silent error handling
        }
    }

    // Utility methods
    private boolean isWoodBlock(org.bukkit.Material material) {
        return material.name().contains("LOG") || material.name().contains("WOOD");
    }

    private boolean isBoss(org.bukkit.entity.Entity entity) {
        return entity instanceof org.bukkit.entity.EnderDragon ||
               entity instanceof org.bukkit.entity.Wither ||
               entity instanceof org.bukkit.entity.ElderGuardian ||
               entity instanceof org.bukkit.entity.Warden;
    }

    // Public API for checking achievements
    public boolean hasAchievement(Player player, String achievementId) {
        PlayerAchievementData data = playerData.get(player.getUniqueId());
        return data != null && data.hasAchievement(achievementId);
    }

    public int getProgress(Player player, String counter) {
        PlayerAchievementData data = playerData.get(player.getUniqueId());
        return data != null ? data.getCounter(counter) : 0;
    }

    // Clean up data when player leaves (memory management)
    public void cleanupPlayerData(UUID playerId) {
        // Keep data for offline players, but could implement cleanup logic here
        // For now, keep data persistent for achievement tracking
    }

}
