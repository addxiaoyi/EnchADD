package net.enchadd.commands;

import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.List;

final class EnchantListOptions {
    String sort = "name";
    String order = "asc";
    EquipmentSlotGroup slot;
    EnchantListDisplayMetadataSupport.SourceTier sourceTier;
    Integer minWeight;
    Integer maxWeight;
    Integer minLevel;
    Integer maxLevel;
    String namespace;
    List<String> keywords;
}
