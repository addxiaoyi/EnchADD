package com.enadd.enchantments;

import com.enadd.core.api.IEnchantmentConfig;
import com.enadd.enchantments.Rarity;
import io.papermc.paper.enchantments.EnchantmentRarity;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import java.util.HashSet;
import java.util.Set;



/**
 * 附魔基础类 - 适配 1.21.4
 */
@SuppressWarnings({ "UnstableApiUsage", "unused" })
public abstract class BaseEnchantment extends Enchantment {

    protected final IEnchantmentConfig config;
    protected final String chineseName;
    protected final String chineseDescription;
    protected final NamespacedKey key;
    protected final Rarity rarity;
    protected final EnchantmentTarget target;
    protected final EquipmentSlot[] slots;

    protected BaseEnchantment(String name, String description, IEnchantmentConfig config) {
        this(Rarity.COMMON, null, new EquipmentSlot[0], config, name, description);
    }

    protected BaseEnchantment(Rarity rarity, EnchantmentTarget target, EquipmentSlot[] slots, IEnchantmentConfig config, String name, String description) {
        // 验证name参数
        String safeName = (name != null && !name.isEmpty()) ? name.toLowerCase().replace(" ", "_") : "unknown";

        // 添加异常处理并验证key
        NamespacedKey tempKey;
        try {
            tempKey = NamespacedKey.fromString("enadd:" + safeName);
            if (tempKey == null) {
                tempKey = NamespacedKey.fromString("enadd:unknown");
            }
        } catch (Exception e) {
            // 如果创建失败，使用默认key
            tempKey = NamespacedKey.fromString("enadd:unknown");
        }

        // 最终验证
        if (tempKey == null) {
            throw new IllegalStateException("Failed to create NamespacedKey");
        }
        this.key = tempKey;

        this.config = config;
        // 确保name和description不为null
        this.chineseName = name != null ? name : "Unknown";
        this.chineseDescription = description != null ? description : "No description";
        // 确保rarity不为null
        this.rarity = rarity != null ? rarity : Rarity.COMMON;
        this.target = target;
        // 确保slots不为null
        this.slots = slots != null ? slots : new EquipmentSlot[0];
    }

    protected BaseEnchantment(Rarity rarity, EquipmentSlot[] slots, IEnchantmentConfig config, String name, String description) {
        this(rarity, null, slots, config, name, description);
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        // 确保key不为null
        return key != null ? key : NamespacedKey.fromString("enadd:unknown");
    }

    @Override
    @Deprecated
    @SuppressWarnings("removal")
    public @NotNull String getName() {
        // 确保返回值不为null
        if (chineseName != null && !chineseName.isEmpty()) {
            return chineseName;
        }
        if (key != null) {
            return key.getKey();
        }
        return "Unknown";
    }

    public String getChineseName() {
        return chineseName != null ? chineseName : "Unknown";
    }

    public String getChineseDescription() {
        return chineseDescription != null ? chineseDescription : "No description";
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    @Deprecated
    @SuppressWarnings("removal")
    public @NotNull String translationKey() {
        if (key != null) {
            return "enchantment.enadd." + key.getKey();
        }
        return "enchantment.enadd.unknown";
    }

    @Override
    @Deprecated
    @SuppressWarnings("removal")
    public @NotNull String getTranslationKey() {
        return translationKey();
    }

    @Override
    public @NotNull Component displayName(int level) {
        String name = chineseName != null ? chineseName : (key != null ? key.getKey() : "Unknown");
        return Component.text(name);
    }

    @Override
    public @NotNull Component description() {
        String desc = chineseDescription != null ? chineseDescription : "No description";
        return Component.text(desc);
    }

    public @NotNull Rarity getLocalRarity() {
        return rarity != null ? rarity : Rarity.COMMON;
    }

    @Override
    @Deprecated
    @SuppressWarnings("removal")
    public @NotNull EnchantmentRarity getRarity() {
        try {
            Rarity local = getLocalRarity();
            if (local == Rarity.LEGENDARY || local == Rarity.EPIC || local == Rarity.VERY_RARE) {
                return EnchantmentRarity.VERY_RARE;
            } else if (local == Rarity.RARE) {
                return EnchantmentRarity.RARE;
            } else if (local == Rarity.UNCOMMON) {
                return EnchantmentRarity.UNCOMMON;
            } else {
                return EnchantmentRarity.COMMON;
            }
        } catch (Exception e) {
            // 异常时返回默认值
            return EnchantmentRarity.COMMON;
        }
    }

    @Override
    public int getWeight() {
        try {
            if (config != null) {
                int weight = config.getWeight();
                if (weight > 0) return weight;
            }

            Rarity local = getLocalRarity();
            switch (local) {
                case LEGENDARY:
                case EPIC:
                case VERY_RARE:
                    return 1; // VERY_RARE weight
                case RARE:
                    return 2; // RARE weight
                case UNCOMMON:
                    return 5; // UNCOMMON weight
                default:
                    return 10; // COMMON weight
            }
        } catch (Exception e) {
            // 异常时返回默认权重
            return 10;
        }
    }

    @Override
    public int getAnvilCost() {
        return 1;
    }

    @Override
    @Deprecated
    @SuppressWarnings("removal")
    public float getDamageIncrease(int level, @NotNull EntityType entityType) {
        // level范围验证
        if (level < 1) {
            return 0;
        }
        return 0;
    }

    @Override
    @Deprecated
    @SuppressWarnings("removal")
    public float getDamageIncrease(int level, @NotNull EntityCategory entityCategory) {
        // level范围验证
        if (level < 1) {
            return 0;
        }
        return 0;
    }

    @Override
    public @NotNull Set<EquipmentSlotGroup> getActiveSlotGroups() {
        try {
            return Set.of(EquipmentSlotGroup.ANY);
        } catch (Exception e) {
            // 异常时返回空集合
            return new HashSet<>();
        }
    }

    @Override
    public @NotNull RegistryKeySet<Enchantment> getExclusiveWith() {
        try {
            return RegistrySet.keySet(RegistryKey.ENCHANTMENT);
        } catch (Exception e) {
            // 异常时返回空集合
            return RegistrySet.keySet(RegistryKey.ENCHANTMENT);
        }
    }

    @Override
    public @NotNull RegistryKeySet<ItemType> getPrimaryItems() {
        try {
            return RegistrySet.keySet(RegistryKey.ITEM);
        } catch (Exception e) {
            // 异常时返回空集合
            return RegistrySet.keySet(RegistryKey.ITEM);
        }
    }

    @Override
    public @NotNull RegistryKeySet<ItemType> getSupportedItems() {
        try {
            return RegistrySet.keySet(RegistryKey.ITEM);
        } catch (Exception e) {
            // 异常时返回空集合
            return RegistrySet.keySet(RegistryKey.ITEM);
        }
    }

    public @NotNull RegistryKeySet<ItemType> getFallbackItems() {
        try {
            return RegistrySet.keySet(RegistryKey.ITEM);
        } catch (Exception e) {
            // 异常时返回空集合
            return RegistrySet.keySet(RegistryKey.ITEM);
        }
    }

    @Override
    public int getMaxLevel() {
        try {
            if (config != null) {
                int maxLevel = config.getMaxLevel();
                // 确保maxLevel在合理范围内
                return Math.max(1, Math.min(255, maxLevel));
            }
            return 1;
        } catch (Exception e) {
            // 异常时返回默认值
            return 1;
        }
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    @Deprecated
    @SuppressWarnings("removal")
    public @NotNull EnchantmentTarget getItemTarget() {
        return target != null ? target : EnchantmentTarget.BREAKABLE;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment other) {
        try {
            // 验证key不为null
            if (this.key == null || other.getKey() == null) {
                return false;
            }

            // 使用冲突管理器检查冲突
            String thisId = this.key.getKey();
            String otherId = other.getKey().getKey();

            // 标准化ID：移除命名空间前缀
            if (thisId.contains(":")) {
                thisId = thisId.substring(thisId.indexOf(":") + 1);
            }
            if (otherId.contains(":")) {
                otherId = otherId.substring(otherId.indexOf(":") + 1);
            }

            // 同一个附魔不冲突
            if (thisId.equals(otherId)) {
                return false;
            }

            return com.enadd.core.conflict.EnchantmentConflictManager.getInstance()
                .areConflicting(thisId, otherId);
        } catch (Exception e) {
            // 异常时返回false（不冲突）
            return false;
        }
    }

    @Override
    public boolean canEnchantItem(@NotNull org.bukkit.inventory.ItemStack item) {
        try {
            // 如果没有设置target，允许所有物品
            if (target == null) {
                return true;
            }

            // 使用EnchantmentTarget检查物品类型
            @SuppressWarnings("removal")
            boolean canEnchant = target.includes(item);
            return canEnchant;
        } catch (Exception e) {
            // 异常时返回false
            return false;
        }
    }

    @Override
    public int getMinModifiedCost(int level) {
        // level范围验证
        if (level < 1) {
            level = 1;
        }

        try {
            if (config != null) {
                return config.getBaseCost(level);
            }
            return 10 + (level - 1) * 5;
        } catch (Exception e) {
            // 异常时返回默认值
            return 10 + (level - 1) * 5;
        }
    }

    @Override
    public int getMaxModifiedCost(int level) {
        try {
            return getMinModifiedCost(level) + 15;
        } catch (Exception e) {
            // 异常时返回默认值
            return 25;
        }
    }

    @Override
    @Deprecated
    @SuppressWarnings("removal")
    public boolean isTreasure() {
        try {
            return config != null && config.isTreasure();
        } catch (Exception e) {
            // Bug #628: 异常时返回false
            return false;
        }
    }

    /**
     * 获取支持的物品Tag条目（用于Bootstrap注册）
     */
    public Set<TagEntry<ItemType>> getSupportedItemTagEntries() {
        try {
            Set<TagEntry<ItemType>> entries = new HashSet<>();

            if (target == null) {
                // 如果没有指定target，支持所有可附魔物品
                entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_ARMOR));
                entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_WEAPON));
                entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_SWORD));
                entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_MINING));
                return entries;
            }

            // 根据EnchantmentTarget添加对应的Minecraft tag
            @SuppressWarnings("removal")
            EnchantmentTarget t = target;

            switch (t) {
                case ARMOR:
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_ARMOR));
                    break;
                case ARMOR_FEET:
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_FOOT_ARMOR));
                    break;
                case ARMOR_LEGS:
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_LEG_ARMOR));
                    break;
                case ARMOR_TORSO:
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_CHEST_ARMOR));
                    break;
                case ARMOR_HEAD:
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR));
                    break;
                case WEAPON:
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_WEAPON));
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_SWORD));
                    break;
                case TOOL:
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_MINING));
                    break;
                case BOW:
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_BOW));
                    break;
                case FISHING_ROD:
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_FISHING));
                    break;
                case TRIDENT:
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_TRIDENT));
                    break;
                case CROSSBOW:
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_CROSSBOW));
                    break;
                case BREAKABLE:
                case VANISHABLE:
                case WEARABLE:
                default:
                    // 对于通用类型，添加所有可附魔物品
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_ARMOR));
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_WEAPON));
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_SWORD));
                    entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_MINING));
                    break;
            }

            return entries;
        } catch (Exception e) {
            // 异常时返回默认集合
            Set<TagEntry<ItemType>> entries = new HashSet<>();
            entries.add(TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_ARMOR));
            return entries;
        }
    }

    /**
     * 获取附魔Tag键（用于Bootstrap注册）
     * 决定附魔在哪里可以获得（附魔台、宝箱等）
     */
    public Set<TagKey<Enchantment>> getEnchantmentTagKeys() {
        try {
            Set<TagKey<Enchantment>> tags = new HashSet<>();

            // 如果是诅咒，添加诅咒tag
            if (isCursed()) {
                tags.add(EnchantmentTagKeys.CURSE);
            }

            // 如果是宝藏附魔，添加宝藏tag
            if (isTreasure()) {
                tags.add(EnchantmentTagKeys.TREASURE);
            } else {
                // 非宝藏附魔可以在附魔台获得
                tags.add(EnchantmentTagKeys.IN_ENCHANTING_TABLE);
            }

            // 所有附魔都可以交易获得
            tags.add(EnchantmentTagKeys.TRADEABLE);

            return tags;
        } catch (Exception e) {
            // 异常时返回默认集合
            Set<TagKey<Enchantment>> tags = new HashSet<>();
            tags.add(EnchantmentTagKeys.TRADEABLE);
            return tags;
        }
    }
}



