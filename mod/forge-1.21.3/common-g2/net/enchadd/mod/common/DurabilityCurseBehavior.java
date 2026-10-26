package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class DurabilityCurseBehavior {
    private static final ResourceKey<Enchantment> FRAGILITY = key("fragility");
    private static final ResourceKey<Enchantment> SHATTER = key("shatter");
    private static final int MAX_LEVEL = 3;
    private static final double FRAGILITY_PER_LEVEL = 0.50;
    private static final double FRAGILITY_MAX_MULTIPLIER = 2.0;
    private static final double SHATTER_CHANCE_PER_LEVEL = 0.30;
    private static final double SHATTER_MAX_CHANCE = 0.75;
    private static final int SHATTER_EXTRA_PER_LEVEL = 1;

    private DurabilityCurseBehavior() {}

    public static int adjust(ItemStack stack, LivingEntity owner, int amount) {
        if (stack == null || stack.isEmpty() || owner == null || amount <= 0 || !stack.isDamageableItem()) return amount;
        var lookup = owner.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> fragility = lookup.getOrThrow(FRAGILITY);
        Holder<Enchantment> shatter = lookup.getOrThrow(SHATTER);
        int fragilityLevel = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(fragility, stack));
        int shatterLevel = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(shatter, stack));
        long adjusted = amount;
        if (fragilityLevel > 0) {
            double multiplier = Math.min(FRAGILITY_MAX_MULTIPLIER, 1.0 + FRAGILITY_PER_LEVEL * fragilityLevel);
            adjusted = Math.round(adjusted * multiplier);
        }
        if (shatterLevel > 0) {
            double chance = Math.min(SHATTER_MAX_CHANCE, SHATTER_CHANCE_PER_LEVEL * shatterLevel);
            if (owner.getRandom().nextDouble() < chance) adjusted += (long) SHATTER_EXTRA_PER_LEVEL * shatterLevel;
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1L, adjusted));
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", id));
    }
}
