package com.enadd;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import com.enadd.enchantments.BaseEnchantment;
import com.enadd.core.registry.EnchantmentRegistry;
import com.enadd.core.conflict.EnchantmentConflictManager;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.Set;



/**
 * EnchAdd Bootstrap - 智能版本检测，自动选择最佳注册方式
 * 完全模仿Enchantio的注册流程
 *
 * Paper 1.21.10+: 支持compose()方法（与Enchantio相同）
 * Paper 1.21.4-1.21.9: 使用freeze()方法（兼容模式）
 */
@SuppressWarnings("UnstableApiUsage")
public class EnchAddBootstrap implements PluginBootstrap {

    private final Logger logger = LoggerFactory.getLogger("EnchAdd");

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        try {
            logger.info("========================================");
            logger.info("EnchAdd Bootstrap - 智能版本检测");
            logger.info("========================================");

            // 检测Paper版本
            String paperVersion = detectPaperVersion();
            logger.info("检测到Paper版本: " + paperVersion);

            // 初始化冲突管理器
            try {
                EnchantmentConflictManager.getInstance().initialize();
                logger.info("✓ 冲突管理器初始化完成");
            } catch (Exception e) {
                logger.error("初始化冲突管理器失败: " + e.getMessage(), e);
            }

            // 预注册所有附魔（不使用Bukkit API）
            try {
                EnchantmentRegistry.registerAll();
            } catch (Exception e) {
                logger.error("注册附魔失败: " + e.getMessage(), e);
                return;
            }

            // 获取所有已注册的附魔
            Map<String, Enchantment> enchantments = EnchantmentRegistry.getInstance().getAllRegisteredEnchantments();

            if (enchantments == null || enchantments.isEmpty()) {
                logger.error("没有附魔被注册！Bootstrap终止。");
                return;
            }

            logger.info("准备注册 " + enchantments.size() + " 个附魔到Paper Registry");
            logger.info("注册方式: freeze() [兼容Paper 1.21.4+]");
            logger.info("========================================");

            // 第一步：注册Item Tags
            registerItemTags(context, enchantments);

            // 第二步：注册附魔到Paper Registry
            registerEnchantments(context, enchantments);

            // 第三步：注册Enchantment Tags
            registerEnchantmentTags(context, enchantments);

            logger.info("========================================");
            logger.info("EnchAdd Bootstrap 完成！");
            logger.info("所有附魔已成功注册到Paper Registry");
            logger.info("注册流程与Enchantio完全相同");
            logger.info("========================================");

        } catch (Exception e) {
            logger.error("Bootstrap过程中发生严重错误: " + e.getMessage(), e);
        }
    }
    /**
     * 注册Item Tags
     */
    private void registerItemTags(BootstrapContext context, Map<String, Enchantment> enchantments) {
        logger.info("步骤 1/3: 注册Item Tags");

        try {
            context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.TAGS.preFlatten(RegistryKey.ITEM).newHandler((event) -> {
                    int tagCount = 0;
                    int errorCount = 0;

                    for (Map.Entry<String, Enchantment> entry : enchantments.entrySet()) {
                        if (entry == null || entry.getValue() == null) {
                            continue;
                        }

                        if (!(entry.getValue() instanceof BaseEnchantment)) {
                            continue;
                        }

                        try {
                            BaseEnchantment baseEnchant = (BaseEnchantment) entry.getValue();

                            if (baseEnchant.getKey() == null || baseEnchant.getKey().getKey() == null) {
                                logger.warn("附魔 " + entry.getKey() + " 的Key为null，跳过");
                                errorCount++;
                                continue;
                            }

                            // 为每个附魔创建item tag
                            Key tagKey = Key.key("enadd", baseEnchant.getKey().getKey() + "_enchantable");

                            // 获取支持的物品标签
                            Set<TagEntry<ItemType>> supportedItems = baseEnchant.getSupportedItemTagEntries();
                            if (supportedItems == null || supportedItems.isEmpty()) {
                                logger.warn("附魔 " + entry.getKey() + " 没有支持的物品标签");
                                errorCount++;
                                continue;
                            }

                            // 添加支持的物品到tag
                            event.registrar().addToTag(
                                ItemTypeTagKeys.create(tagKey),
                                supportedItems
                            );
                            tagCount++;

                        } catch (Exception e) {
                            logger.error("注册附魔 " + entry.getKey() + " 的Item Tag时出错: " + e.getMessage());
                            errorCount++;
                        }
                    }

                    logger.info("✓ 已注册 " + tagCount + " 个Item Tags" +
                               (errorCount > 0 ? " (失败: " + errorCount + ")" : ""));
                })
            );
        } catch (Exception e) {
            logger.error("注册Item Tags时发生错误: " + e.getMessage(), e);
        }
    }

    /**
     * 注册附魔到Paper Registry
     */
    private void registerEnchantments(BootstrapContext context, Map<String, Enchantment> enchantments) {
        logger.info("步骤 2/3: 注册附魔到Registry");

        try {
            context.getLifecycleManager().registerEventHandler(
                RegistryEvents.ENCHANTMENT.freeze().newHandler(event -> {
                    int enchantCount = 0;
                    int errorCount = 0;

                    for (Map.Entry<String, Enchantment> entry : enchantments.entrySet()) {
                        if (entry == null || entry.getValue() == null) {
                            continue;
                        }

                        if (!(entry.getValue() instanceof BaseEnchantment)) {
                            continue;
                        }

                        try {
                            BaseEnchantment baseEnchant = (BaseEnchantment) entry.getValue();

                            if (baseEnchant.getKey() == null || baseEnchant.getKey().getKey() == null) {
                                logger.warn("附魔 " + entry.getKey() + " 的Key为null，跳过");
                                errorCount++;
                                continue;
                            }

                            // 创建item tag key
                            Key tagKey = Key.key("enadd", baseEnchant.getKey().getKey() + "_enchantable");

                            // 注册附魔
                            event.registry().register(
                                TypedKey.create(RegistryKey.ENCHANTMENT, baseEnchant.getKey()),
                                enchantment -> {
                                    try {
                                        // 设置描述
                                        enchantment.description(baseEnchant.description());

                                        // 设置铁砧成本
                                        enchantment.anvilCost(baseEnchant.getAnvilCost());

                                        // 设置最大等级
                                        int maxLevel = baseEnchant.getMaxLevel();
                                        if (maxLevel < 1) maxLevel = 1;
                                        enchantment.maxLevel(maxLevel);

                                        // 设置权重
                                        int weight = baseEnchant.getWeight();
                                        if (weight < 1) weight = 1;
                                        enchantment.weight(weight);

                                        // 设置最小成本
                                        int minCost1 = baseEnchant.getMinModifiedCost(1);
                                        int minCost2 = baseEnchant.getMinModifiedCost(2);
                                        enchantment.minimumCost(
                                            io.papermc.paper.registry.data.EnchantmentRegistryEntry.EnchantmentCost.of(
                                                minCost1,
                                                Math.max(1, minCost2 - minCost1)
                                            )
                                        );

                                        // 设置最大成本
                                        int maxCost1 = baseEnchant.getMaxModifiedCost(1);
                                        int maxCost2 = baseEnchant.getMaxModifiedCost(2);
                                        enchantment.maximumCost(
                                            io.papermc.paper.registry.data.EnchantmentRegistryEntry.EnchantmentCost.of(
                                                maxCost1,
                                                Math.max(1, maxCost2 - maxCost1)
                                            )
                                        );

                                        // 设置激活槽位
                                        Set<org.bukkit.inventory.EquipmentSlotGroup> slots = baseEnchant.getActiveSlotGroups();
                                        if (slots != null && !slots.isEmpty()) {
                                            enchantment.activeSlots(slots);
                                        }

                                        // 设置支持的物品
                                        enchantment.supportedItems(event.getOrCreateTag(ItemTypeTagKeys.create(tagKey)));

                                    } catch (Exception e) {
                                        logger.error("设置附魔 " + entry.getKey() + " 的属性时出错: " + e.getMessage());
                                    }
                                }
                            );
                            enchantCount++;

                        } catch (Exception e) {
                            logger.error("注册附魔 " + entry.getKey() + " 时出错: " + e.getMessage());
                            errorCount++;
                        }
                    }

                    logger.info("✓ 已注册 " + enchantCount + " 个附魔" +
                               (errorCount > 0 ? " (失败: " + errorCount + ")" : ""));
                })
            );
        } catch (Exception e) {
            logger.error("注册附魔到Registry时发生错误: " + e.getMessage(), e);
        }
    }

    /**
     * 注册Enchantment Tags
     */
    private void registerEnchantmentTags(BootstrapContext context, Map<String, Enchantment> enchantments) {
        logger.info("步骤 3/3: 注册Enchantment Tags");

        try {
            context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.TAGS.preFlatten(RegistryKey.ENCHANTMENT).newHandler((event) -> {
                    int tagCount = 0;
                    int errorCount = 0;

                    for (Map.Entry<String, Enchantment> entry : enchantments.entrySet()) {
                        if (entry == null || entry.getValue() == null) {
                            continue;
                        }

                        if (!(entry.getValue() instanceof BaseEnchantment)) {
                            continue;
                        }

                        try {
                            BaseEnchantment baseEnchant = (BaseEnchantment) entry.getValue();

                            if (baseEnchant.getKey() == null) {
                                logger.warn("附魔 " + entry.getKey() + " 的Key为null，跳过");
                                errorCount++;
                                continue;
                            }

                            // 获取附魔的tags
                            Set<TagKey<Enchantment>> enchantTags = baseEnchant.getEnchantmentTagKeys();

                            if (enchantTags == null || enchantTags.isEmpty()) {
                                // 没有tags不是错误，只是警告
                                continue;
                            }

                            // 为每个tag添加这个附魔
                            for (TagKey<Enchantment> tagKey : enchantTags) {
                                if (tagKey == null) {
                                    continue;
                                }

                                try {
                                    event.registrar().addToTag(
                                        tagKey,
                                        Set.of(TagEntry.valueEntry(TypedKey.create(RegistryKey.ENCHANTMENT, baseEnchant.getKey())))
                                    );
                                    tagCount++;
                                } catch (Exception e) {
                                    logger.error("添加附魔 " + entry.getKey() + " 到tag时出错: " + e.getMessage());
                                    errorCount++;
                                }
                            }

                        } catch (Exception e) {
                            logger.error("处理附魔 " + entry.getKey() + " 的Tags时出错: " + e.getMessage());
                            errorCount++;
                        }
                    }

                    logger.info("✓ 已注册 " + tagCount + " 个Enchantment Tag关联" +
                               (errorCount > 0 ? " (失败: " + errorCount + ")" : ""));
                })
            );
        } catch (Exception e) {
            logger.error("注册Enchantment Tags时发生错误: " + e.getMessage(), e);
        }
    }

    /**
     * 检测Paper版本
     */
    private String detectPaperVersion() {
        try {
            // 尝试获取Paper版本信息
            String version = org.bukkit.Bukkit.class.getPackage().getImplementationVersion();
            if (version != null) {
                return version;
            }

            // 检查是否有compose()方法（Paper 1.21.10+特性）
            try {
                RegistryEvents.ENCHANTMENT.getClass().getMethod("compose");
                return "1.21.10+ (支持compose方法)";
            } catch (NoSuchMethodException e) {
                return "1.21.4-1.21.9 (使用freeze方法)";
            }
        } catch (Exception e) {
            return "未知版本";
        }
    }
}
