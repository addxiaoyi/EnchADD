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

public final class BraceBehavior {

    private static final ResourceKey<Enchantment> BRACE = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath("enchadd", "brace")
    );
    private static final int MAX_LEVEL = 3;
    private static final double REDUCTION_PER_LEVEL = 0.15;
    private static final double MAX_REDUCTION = 0.45;

    private BraceBehavior() {
    }

    public static float reduceStrength(LivingEntity entity, float strength) {
        if (!Float.isFinite(strength) || strength <= 0.0f || !isBraced(entity)) return strength;
        Holder<Enchantment> holder = entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(BRACE);
        int level = Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder,
                entity.getItemBySlot(EquipmentSlot.OFFHAND)));
        if (level <= 0) return strength;
        double reduction = Math.min(MAX_REDUCTION, REDUCTION_PER_LEVEL * level);
        float adjusted = (float) (strength * (1.0 - reduction));
        return Float.isFinite(adjusted) ? Math.max(0.0f, adjusted) : strength;
    }

    private static boolean isBraced(LivingEntity entity) {
        ItemStack used = entity.getUseItem();
        ItemStack offhand = entity.getItemBySlot(EquipmentSlot.OFFHAND);
        return entity.isBlocking() && !used.isEmpty() && used.is(Items.SHIELD)
                && !offhand.isEmpty() && offhand.is(Items.SHIELD);
    }
}
