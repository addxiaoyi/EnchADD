package net.enchadd.mod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.food.FoodData;
import net.minecraft.core.component.DataComponents;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RetchingBehavior {

    private static final ResourceKey<Enchantment> RETCHING = key("retching_curse");
    private static final int MAX_LEVEL = 3;
    private static final int FOOD_REDUCTION_PER_LEVEL = 1;
    private static final int MAX_FOOD_REDUCTION = 3;
    private static final int NAUSEA_TICKS_PER_LEVEL = 40;
    private static final int MAX_NAUSEA_TICKS = 120;
    private static final Map<UUID, Integer> FOOD_BEFORE = new ConcurrentHashMap<>();

    private RetchingBehavior() {
    }

    public static void beforeFood(ServerPlayer player, ItemStack food) {
        if (food.isEmpty() || !food.has(DataComponents.FOOD)) {
            return;
        }
        FOOD_BEFORE.put(player.getUUID(), player.getFoodData().getFoodLevel());
    }

    public static void afterFood(ServerPlayer player, ItemStack food) {
        if (food.isEmpty() || !food.has(DataComponents.FOOD)) {
            FOOD_BEFORE.remove(player.getUUID());
            return;
        }
        int level = level(player);
        if (level <= 0) {
            FOOD_BEFORE.remove(player.getUUID());
            return;
        }

        FoodData foodData = player.getFoodData();
        Integer before = FOOD_BEFORE.remove(player.getUUID());
        if (before == null) {
            return;
        }
        int gainedFood = foodData.getFoodLevel() - before;
        if (gainedFood <= 0) {
            return;
        }
        int reduction = Math.min(MAX_FOOD_REDUCTION, FOOD_REDUCTION_PER_LEVEL * level);
        int actualReduction = Math.min(reduction, gainedFood);
        if (actualReduction > 0) {
            foodData.setFoodLevel(before + gainedFood - actualReduction);
        }

        int nauseaTicks = Math.min(MAX_NAUSEA_TICKS, NAUSEA_TICKS_PER_LEVEL * level);
        if (nauseaTicks > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, nauseaTicks, 0, false, false, true));
        }
    }

    private static int level(ServerPlayer player) {
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(RETCHING);
        return Math.min(MAX_LEVEL, EnchantmentHelper.getItemEnchantmentLevel(holder, player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)));
    }

    private static ResourceKey<Enchantment> key(String id) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", id));
    }
}
