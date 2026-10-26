package net.enchadd.mod;

import net.enchadd.mod.common.FreshcatchRules;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.enchadd.mod.common.TrawlerRules;

final class EnchADDForgeFreshcatchBehavior {
    private static final ResourceKey<Enchantment> FRESHCATCH = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "freshcatch"));
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeFreshcatchBehavior()::onFished); }
    @SubscribeEvent
    public void onFished(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        applyTrawler(event);
        boolean edible = false;
        for (ItemStack drop : event.getDrops()) {
            if (drop.has(net.minecraft.core.component.DataComponents.FOOD)) { edible = true; break; }
        }
        if (!edible) return;
        ItemStack rod = player.getMainHandItem();
        if (rod.isEmpty()) rod = player.getOffhandItem();
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FRESHCATCH);
        int level = EnchantmentHelper.getItemEnchantmentLevel(holder, rod);
        if (level <= 0) return;
        FoodData food = player.getFoodData();
        int foodLevel = FreshcatchRules.foodLevel(food.getFoodLevel(), level);
        food.setFoodLevel(foodLevel);
        food.setSaturation(FreshcatchRules.saturation(food.getSaturationLevel(), foodLevel, level));
    }

    private static void applyTrawler(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack rod = player.getMainHandItem();
        if (rod.isEmpty()) rod = player.getOffhandItem();
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("enchadd", "trawler")));
        int level = EnchantmentHelper.getItemEnchantmentLevel(holder, rod);
        if (level <= 0 || player.getCooldowns().isOnCooldown(rod) || !TrawlerRules.shouldTrigger(player.getRandom(), level)) return;
        for (ItemStack drop : event.getDrops()) {
            if (drop.isEmpty() || drop.getCount() >= drop.getMaxStackSize()) continue;
            drop.grow(Math.min(TrawlerRules.extraItems(level), drop.getMaxStackSize() - drop.getCount()));
            player.getCooldowns().addCooldown(rod, 80);
            return;
        }
    }
}
