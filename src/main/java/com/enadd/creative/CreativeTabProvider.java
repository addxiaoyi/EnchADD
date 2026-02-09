package com.enadd.creative;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class CreativeTabProvider implements Listener {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
    private final JavaPlugin plugin;
    private final Map<PlayerData, List<CreativeItem>> tabItems = new ConcurrentHashMap<>();
    private boolean initialized = false;

    private static class PlayerData {
        final UUID playerId;

        PlayerData(UUID playerId, String playerName) {
            this.playerId = playerId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PlayerData that = (PlayerData) o;
            return playerId.equals(that.playerId);
        }

        @Override
        public int hashCode() {
            return playerId.hashCode();
        }
    }

    public CreativeTabProvider(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (initialized) {
            return;
        }

        try {
            generateTabItems();
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            initialized = true;
            plugin.getLogger().info("Creative tab provider initialized successfully");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize creative tab provider: " + e.getMessage());
        }
    }

    private void generateTabItems() {
        try {
            List<CreativeItem> allItems = new ArrayList<>();

            allItems.addAll(generateWeaponEnchantments());
            allItems.addAll(generateArmorEnchantments());
            allItems.addAll(generateToolEnchantments());
            allItems.addAll(generateBowEnchantments());
            allItems.addAll(generateUtilityEnchantments());
            allItems.addAll(generateCurseEnchantments());
            allItems.addAll(generateSpecialEnchantments());

            for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                PlayerData data = new PlayerData(player.getUniqueId(), player.getName());
                tabItems.put(data, new ArrayList<>(allItems));
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to generate tab items: " + e.getMessage());
        }
    }

    private List<CreativeItem> generateWeaponEnchantments() {
        List<CreativeItem> items = new ArrayList<>();

        items.add(createEnchantmentItem(
            "sharpness",
            "锋利",
            "对准所有类型的生物造成额外伤害，是最通用的武器附魔。",
            Material.DIAMOND_SWORD,
            5,
            10,
            "§c武器"
        ));

        items.add(createEnchantmentItem(
            "smite",
            "亡灵杀手",
            "对准亡灵生物（僵尸、骷髅等）造成额外神圣伤害。",
            Material.IRON_SWORD,
            5,
            8,
            "§c武器"
        ));

        items.add(createEnchantmentItem(
            "bane_of_arthropods",
            "节肢杀手",
            "对准节肢生物（蜘蛛、蠹虫等）造成额外伤害并附加缓慢效果。",
            Material.SPIDER_EYE,
            5,
            8,
            "§c武器"
        ));

        items.add(createEnchantmentItem(
            "knockback",
            "击退",
            "攻击时将目标击退更远的距离。",
            Material.PISTON,
            2,
            10,
            "§c武器"
        ));

        items.add(createEnchantmentItem(
            "fire_aspect",
            "火焰附加",
            "使武器带有火焰，可点燃目标造成持续伤害。",
            Material.FIRE_CHARGE,
            2,
            5,
            "§c武器"
        ));

        items.add(createEnchantmentItem(
            "looting",
            "抢夺",
            "击杀生物时增加掉落物数量和获取稀有物品的机会。",
            Material.GOLD_INGOT,
            3,
            3,
            "§c武器"
        ));

        items.add(createEnchantmentItem(
            "sweeping_edge",
            "横扫之刃",
            "增加横扫攻击的伤害范围和伤害值。",
            Material.ENCHANTED_BOOK,
            3,
            5,
            "§c武器"
        ));

        items.add(createEnchantmentItem(
            "unbreaking",
            "耐久",
            "增加工具、武器和盔甲的使用寿命，减少耐久损耗。",
            Material.DIAMOND,
            3,
            15,
            "§c武器"
        ));

        items.add(createEnchantmentItem(
            "mending",
            "经验修补",
            "使用经验球修复装备的耐久度。",
            Material.EXPERIENCE_BOTTLE,
            1,
            2,
            "§c武器"
        ));

        items.add(createEnchantmentItem(
            "bleeding",
            "流血",
            "攻击时有几率使目标进入流血状态，持续损失生命值。",
            Material.REDSTONE,
            3,
            6,
            "§c武器"
        ));

        items.add(createEnchantmentItem(
            "armor_pierce",
            "穿甲",
            "攻击时忽视目标的护甲值，直接造成伤害。",
            Material.IRON_CHESTPLATE,
            3,
            4,
            "§c武器"
        ));

        items.add(createEnchantmentItem(
            "critical_strike",
            "暴击",
            "增加造成暴击伤害的几率，暴击造成额外50%伤害。",
            Material.GOLDEN_SWORD,
            3,
            5,
            "§c武器"
        ));

        items.add(createEnchantmentItem(
            "life_steal",
            "生命偷取",
            "攻击时有几率恢复等同于造成伤害一定比例的生命值。",
            Material.POTION,
            3,
            3,
            "§c武器"
        ));

        items.add(createEnchantmentItem(
            "berserker_rage",
            "狂战士之怒",
            "生命值越低，造成的伤害越高。",
            Material.NETHER_WART,
            3,
            2,
            "§c武器"
        ));

        items.add(createEnchantmentItem(
            "backstab",
            "背刺",
            "从背后攻击时造成额外伤害并击退目标。",
            Material.STONE_SWORD,
            3,
            4,
            "§c武器"
        ));

        return items;
    }

    private List<CreativeItem> generateArmorEnchantments() {
        List<CreativeItem> items = new ArrayList<>();

        items.add(createEnchantmentItem(
            "protection",
            "保护",
            "减少所有类型的伤害，是最通用的护甲附魔。",
            Material.DIAMOND_CHESTPLATE,
            4,
            10,
            "§9护甲"
        ));

        items.add(createEnchantmentItem(
            "fire_protection",
            "火焰保护",
            "专门减少火焰和熔岩造成的伤害。",
            Material.MAGMA_BLOCK,
            4,
            8,
            "§9护甲"
        ));

        items.add(createEnchantmentItem(
            "blast_protection",
            "爆炸保护",
            "减少爆炸和苦力怕造成的伤害。",
            Material.TNT,
            4,
            5,
            "§9护甲"
        ));

        items.add(createEnchantmentItem(
            "projectile_protection",
            "弹射物保护",
            "减少箭矢和其他弹射物造成的伤害。",
            Material.ARROW,
            4,
            8,
            "§9护甲"
        ));

        items.add(createEnchantmentItem(
            "feather_falling",
            "摔落保护",
            "显著减少掉落伤害，保护玩家免受高空坠落的致命伤害。",
            Material.FEATHER,
            4,
            10,
            "§9护甲"
        ));

        items.add(createEnchantmentItem(
            "thorns",
            "荆棘",
            "攻击穿戴者时，反弹部分伤害给攻击者。",
            Material.CACTUS,
            3,
            3,
            "§9护甲"
        ));

        items.add(createEnchantmentItem(
            "depth_strider",
            "深度行走",
            "显著提高在水中行走的速度。",
            Material.PRISMARINE_SHARD,
            3,
            8,
            "§9护甲"
        ));

        items.add(createEnchantmentItem(
            "frost_walker",
            "冰霜行者",
            "在水面结冰，允许玩家在水上行走。",
            Material.BLUE_ICE,
            2,
            3,
            "§9护甲"
        ));

        items.add(createEnchantmentItem(
            "soul_speed",
            "灵魂速度",
            "在灵魂沙或灵魂土上移动速度大幅提升。",
            Material.SOUL_SAND,
            3,
            3,
            "§9护甲"
        ));

        items.add(createEnchantmentItem(
            "swift_sneak",
            "快速潜行",
            "潜行时移动速度大幅提升。",
            Material.CHAIN,
            3,
            2,
            "§9护甲"
        ));

        items.add(createEnchantmentItem(
            "stone_skin",
            "石肤",
            "提供额外的物理伤害减免效果。",
            Material.STONE,
            3,
            5,
            "§9护甲"
        ));

        items.add(createEnchantmentItem(
            "dodge",
            "闪避",
            "有一定几率完全闪避近战攻击。",
            Material.LEATHER_CHESTPLATE,
            3,
            4,
            "§9护甲"
        ));

        items.add(createEnchantmentItem(
            "barrier",
            "屏障",
            "受到伤害时产生护盾效果，可吸收一定量的伤害。",
            Material.SHIELD,
            3,
            3,
            "§9护甲"
        ));

        items.add(createEnchantmentItem(
            "endurance",
            "耐力",
            "减少因攻击或移动消耗的饥饿值。",
            Material.COOKED_BEEF,
            3,
            6,
            "§9护甲"
        ));

        return items;
    }

    private List<CreativeItem> generateToolEnchantments() {
        List<CreativeItem> items = new ArrayList<>();

        items.add(createEnchantmentItem(
            "efficiency",
            "效率",
            "大幅提升工具的采集速度，是所有工具的核心附魔。",
            Material.DIAMOND_PICKAXE,
            5,
            15,
            "§e工具"
        ));

        items.add(createEnchantmentItem(
            "silk_touch",
            "精准采集",
            "使方块以完整形式掉落，而非其掉落物形式。",
            Material.EMERALD,
            1,
            2,
            "§e工具"
        ));

        items.add(createEnchantmentItem(
            "fortune",
            "时运",
            "大幅增加方块的掉落概率和数量。",
            Material.GOLD_INGOT,
            3,
            1,
            "§e工具"
        ));

        items.add(createEnchantmentItem(
            "unbreaking",
            "耐久",
            "增加工具的使用寿命，减少耐久损耗。",
            Material.DIAMOND,
            3,
            15,
            "§e工具"
        ));

        items.add(createEnchantmentItem(
            "mending",
            "经验修补",
            "使用经验球修复工具的耐久度。",
            Material.EXPERIENCE_BOTTLE,
            1,
            3,
            "§e工具"
        ));

        items.add(createEnchantmentItem(
            "miner",
            "矿工",
            "提升挖掘矿石时的效率和掉落。",
            Material.DIAMOND_PICKAXE,
            3,
            8,
            "§e工具"
        ));

        items.add(createEnchantmentItem(
            "auto_smelt",
            "自动冶炼",
            "挖掘矿物时自动冶炼成对应的锭。",
            Material.FURNACE,
            1,
            5,
            "§e工具"
        ));

        items.add(createEnchantmentItem(
            "magnetic",
            "磁力",
            "自动吸引附近的可拾取物品。",
            Material.HOPPER,
            3,
            4,
            "§e工具"
        ));

        items.add(createEnchantmentItem(
            "vein_miner",
            "矿脉挖掘",
            "挖掘一个矿石时会同时挖掘相连的同类型矿石。",
            Material.NETHER_GOLD_ORE,
            2,
            2,
            "§e工具"
        ));

        items.add(createEnchantmentItem(
            "smelting_touch",
            "冶炼之触",
            "手持工具挖掘方块时直接冶炼成最终产物。",
            Material.BLAST_FURNACE,
            1,
            4,
            "§e工具"
        ));

        items.add(createEnchantmentItem(
            "titan_strength",
            "泰坦之力",
            "增加工具挖掘方块时的范围和破坏力。",
            Material.NETHERITE_PICKAXE,
            3,
            3,
            "§e工具"
        ));

        items.add(createEnchantmentItem(
            "builder",
            "建筑师",
            "更快速地放置和破坏建筑方块。",
            Material.BRICKS,
            3,
            6,
            "§e工具"
        ));

        return items;
    }

    private List<CreativeItem> generateBowEnchantments() {
        List<CreativeItem> items = new ArrayList<>();

        items.add(createEnchantmentItem(
            "power",
            "力量",
            "大幅增加弓的伤害输出，是弓的核心附魔。",
            Material.BOW,
            5,
            10,
            "§a弓"
        ));

        items.add(createEnchantmentItem(
            "punch",
            "冲击",
            "击退弓箭命中的目标，使其后退更远。",
            Material.LEVER,
            2,
            8,
            "§a弓"
        ));

        items.add(createEnchantmentItem(
            "flame",
            "火矢",
            "使箭矢带有火焰，点燃目标造成持续伤害。",
            Material.FIRE_CHARGE,
            2,
            5,
            "§a弓"
        ));

        items.add(createEnchantmentItem(
            "infinity",
            "无限",
            "允许玩家无限使用箭矢，只需一根箭即可。",
            Material.SPECTRAL_ARROW,
            1,
            2,
            "§a弓"
        ));

        items.add(createEnchantmentItem(
            "unbreaking",
            "耐久",
            "增加弓的使用寿命。",
            Material.DIAMOND,
            3,
            10,
            "§a弓"
        ));

        items.add(createEnchantmentItem(
            "mending",
            "经验修补",
            "使用经验修复弓的耐久度。",
            Material.EXPERIENCE_BOTTLE,
            1,
            2,
            "§a弓"
        ));

        items.add(createEnchantmentItem(
            "multishot",
            "多重射击",
            "一次发射多支箭矢（3支）。",
            Material.ARROW,
            1,
            2,
            "§a弓"
        ));

        items.add(createEnchantmentItem(
            "piercing",
            "穿透",
            "使箭矢能够穿透多个目标。",
            Material.ARROW,
            4,
            4,
            "§a弓"
        ));

        return items;
    }

    private List<CreativeItem> generateUtilityEnchantments() {
        List<CreativeItem> items = new ArrayList<>();

        items.add(createEnchantmentItem(
            "night_vision",
            "夜视",
            "在黑暗环境中提供夜视能力。",
            Material.GOLDEN_CARROT,
            1,
            8,
            "§butility"
        ));

        items.add(createEnchantmentItem(
            "invisibility",
            "隐身",
            "使穿戴者对其他生物不可见。",
            Material.ENDER_PEARL,
            1,
            3,
            "§butility"
        ));

        items.add(createEnchantmentItem(
            "water_breathing",
            "水下呼吸",
            "提供水下呼吸能力，防止窒息。",
            Material.PUFFERFISH,
            1,
            8,
            "§bUtility"
        ));

        items.add(createEnchantmentItem(
            "slow_falling",
            "缓慢降落",
            "减缓玩家的降落速度，防止摔落伤害。",
            Material.FEATHER,
            1,
            6,
            "§butility"
        ));

        items.add(createEnchantmentItem(
            "soul_bound",
            "灵魂绑定",
            "死亡时物品不会掉落，而是保留在玩家身上。",
            Material.GHAST_TEAR,
            1,
            2,
            "§butility"
        ));

        items.add(createEnchantmentItem(
            "auto_repair",
            "自动修复",
            "使用经验自动修复装备耐久。",
            Material.ANVIL,
            1,
            2,
            "§butility"
        ));

        items.add(createEnchantmentItem(
            "dolphins_grace",
            "海豚的恩惠",
            "大幅提高水下游泳速度。",
            Material.COD,
            1,
            5,
            "§butility"
        ));

        items.add(createEnchantmentItem(
            "luck",
            "幸运",
            "提升在各种活动中获得好运的几率。",
            Material.EMERALD,
            1,
            4,
            "§butility"
        ));

        items.add(createEnchantmentItem(
            "unluck",
            "厄运",
            "降低敌人和陷阱的命中率（双刃剑附魔）。",
            Material.POISONOUS_POTATO,
            1,
            3,
            "§butility"
        ));

        return items;
    }

    private List<CreativeItem> generateCurseEnchantments() {
        List<CreativeItem> items = new ArrayList<>();

        items.add(createEnchantmentItem(
            "curse_of_fragility",
            "脆弱诅咒",
            "减少装备的耐久度上限。",
            Material.BONE,
            1,
            0,
            "§5诅咒"
        ));

        items.add(createEnchantmentItem(
            "curse_of_binding",
            "绑定诅咒",
            "使装备在死亡后无法被取回。",
            Material.ROTTEN_FLESH,
            1,
            0,
            "§5诅咒"
        ));

        items.add(createEnchantmentItem(
            "curse_of_vanishing",
            "消失诅咒",
            "在退出游戏后物品会消失。",
            Material.PHANTOM_MEMBRANE,
            1,
            0,
            "§5诅咒"
        ));

        items.add(createEnchantmentItem(
            "curse_of_weakness",
            "虚弱诅咒",
            "减少穿戴者的攻击伤害。",
            Material.ZOMBIE_HEAD,
            1,
            0,
            "§5诅咒"
        ));

        return items;
    }

    private List<CreativeItem> generateSpecialEnchantments() {
        List<CreativeItem> items = new ArrayList<>();

        items.add(createEnchantmentItem(
            "channeling",
            "引雷",
            "在雨天或水边召唤闪电击中目标。",
            Material.TRIDENT,
            1,
            2,
            "§d特殊"
        ));

        items.add(createEnchantmentItem(
            "loyalty",
            "忠诚",
            "使三叉戟在投掷后自动飞回。",
            Material.NAUTILUS_SHELL,
            3,
            4,
            "§d特殊"
        ));

        items.add(createEnchantmentItem(
            "impaling",
            "穿刺",
            "对水生生物造成额外伤害。",
            Material.PRISMARINE_CRYSTALS,
            5,
            3,
            "§d特殊"
        ));

        items.add(createEnchantmentItem(
            "riptide",
            "激流",
            "在雨天或水下将玩家向前推动。",
            Material.DRIED_KELP,
            3,
            3,
            "§d特殊"
        ));

        items.add(createEnchantmentItem(
            "quick_charge",
            "快速装填",
            "大幅缩短弩的装填时间。",
            Material.CROSSBOW,
            3,
            5,
            "§d特殊"
        ));

        items.add(createEnchantmentItem(
            "multishot",
            "多重射击",
            "一次发射多支弩箭。",
            Material.CROSSBOW,
            1,
            2,
            "§d特殊"
        ));

        items.add(createEnchantmentItem(
            "piercing",
            "穿透",
            "使弩箭能够穿透多个目标。",
            Material.CROSSBOW,
            4,
            4,
            "§d特殊"
        ));

        items.add(createEnchantmentItem(
            "looting",
            "抢夺",
            "使用钓鱼竿钓鱼时增加获得宝藏的几率。",
            Material.FISHING_ROD,
            3,
            4,
            "§d特殊"
        ));

        items.add(createEnchantmentItem(
            "lure",
            "诱饵",
            "减少等待鱼上钩的时间。",
            Material.FISHING_ROD,
            3,
            6,
            "§d特殊"
        ));

        items.add(createEnchantmentItem(
            "luck_of_the_sea",
            "海洋幸运",
            "增加钓鱼时获得宝藏和鱼类的几率。",
            Material.TROPICAL_FISH,
            3,
            3,
            "§d特殊"
        ));

        return items;
    }

    private CreativeItem createEnchantmentItem(String id, String name, String description,
                                               Material baseMaterial, int maxLevel,
                                               int weight, String category) {
        return new CreativeItem(id, name, description, baseMaterial, maxLevel, weight, category);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player)) return;

        if (event.getSlotType() != org.bukkit.event.inventory.InventoryType.SlotType.RESULT) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        // Use modern Adventure API instead of deprecated method
        List<String> lore = com.enadd.util.ItemMetaHelper.getLoreAsStrings(meta);
        if (lore == null) return;

        for (String line : lore) {
            if (line != null && line.contains("enchadd:")) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public List<CreativeItem> getTabItems(org.bukkit.entity.Player player) {
        PlayerData data = new PlayerData(player.getUniqueId(), player.getName());
        return tabItems.getOrDefault(data, new ArrayList<>());
    }

    public void refreshPlayerTab(org.bukkit.entity.Player player) {
        PlayerData data = new PlayerData(player.getUniqueId(), player.getName());
        List<CreativeItem> items = new ArrayList<>();

        items.addAll(generateWeaponEnchantments());
        items.addAll(generateArmorEnchantments());
        items.addAll(generateToolEnchantments());
        items.addAll(generateBowEnchantments());
        items.addAll(generateUtilityEnchantments());
        items.addAll(generateCurseEnchantments());
        items.addAll(generateSpecialEnchantments());

        tabItems.put(data, items);
    }

    public static class CreativeItem {
        private final String id;
        private final String name;
        private final String description;
        private final Material baseMaterial;
        private final int maxLevel;
        private final int weight;
        private final String category;

        public CreativeItem(String id, String name, String description, Material baseMaterial,
                          int maxLevel, int weight, String category) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.baseMaterial = baseMaterial;
            this.maxLevel = maxLevel;
            this.weight = weight;
            this.category = category;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Material getBaseMaterial() { return baseMaterial; }
        public int getMaxLevel() { return maxLevel; }
        public int getWeight() { return weight; }
        public String getCategory() { return category; }

        public ItemStack createItemStack() {
            ItemStack stack = new ItemStack(baseMaterial);
            ItemMeta meta = stack.getItemMeta();

            if (meta != null) {
                net.kyori.adventure.text.Component displayNameComponent =
                    LEGACY_SERIALIZER.deserialize("§r" + category + " " + name + " §7(I)");
                meta.displayName(displayNameComponent);

                List<String> lore = new ArrayList<>();
                lore.add("§7" + description);
                lore.add("");
                lore.add("§8enchadd:" + id);
                lore.add("§8ID: " + id);
                lore.add("");
                lore.add("§7最大等级: §e" + maxLevel);
                lore.add("§7权重: §e" + weight);
                lore.add("");
                lore.add("§8附魔权重越高越容易获得");

                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(LEGACY_SERIALIZER.deserialize(line).decoration(TextDecoration.ITALIC, false));
                }
                meta.lore(loreComponents);

                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DYE);
            }

            stack.setItemMeta(meta);
            return stack;
        }
    }
}
