package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WardBehavior {

    private static final ResourceKey<Enchantment> WARD = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "ward"));
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final int COOLDOWN_TICKS = 80;

    private WardBehavior() {
    }

    public static boolean block(LivingEntity entity, float damage) {
        if (!Float.isFinite(damage) || damage <= 0.0f || !entity.isBlocking()) return false;
        ItemStack shield = entity.getUseItem();
        ItemStack offhand = entity.getItemBySlot(EquipmentSlot.OFFHAND);
        if (shield.isEmpty() || offhand.isEmpty() || !shield.is(Items.SHIELD) || !offhand.is(Items.SHIELD)) return false;
        Holder<Enchantment> holder = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(WARD);
        if (EnchantmentHelper.getItemEnchantmentLevel(holder, offhand) <= 0) return false;
        long now = entity.level().getGameTime();
        if (now < COOLDOWNS.getOrDefault(entity.getUUID(), 0L)) return false;
        int durabilityCost = Math.max(1, (int) Math.ceil(damage));
        shield.hurtAndBreak(durabilityCost, entity, EquipmentSlot.OFFHAND);
        COOLDOWNS.put(entity.getUUID(), now + COOLDOWN_TICKS);
        return true;
    }
}
