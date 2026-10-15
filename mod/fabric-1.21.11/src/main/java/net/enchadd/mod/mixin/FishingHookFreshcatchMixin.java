package net.enchadd.mod.mixin;

import net.enchadd.mod.common.FreshcatchRules;
import net.enchadd.mod.common.TrawlerRules;
import net.minecraft.advancements.criterion.FishingRodHookedTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

@Mixin(FishingHook.class)
abstract class FishingHookFreshcatchMixin {
    private static final ResourceKey<Enchantment> FRESHCATCH = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "freshcatch"));
    private static final ResourceKey<Enchantment> TRAWLER = ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("enchadd", "trawler"));

    @Redirect(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/criterion/FishingRodHookedTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/projectile/FishingHook;Ljava/util/Collection;)V", ordinal = 1))
    private void enchadd$restoreFood(FishingRodHookedTrigger trigger, ServerPlayer player, ItemStack rod, FishingHook hook, Collection<ItemStack> drops) {
        Holder<Enchantment> trawler = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(TRAWLER);
        int trawlerLevel = EnchantmentHelper.getItemEnchantmentLevel(trawler, rod);
        if (trawlerLevel > 0 && !player.getCooldowns().isOnCooldown(rod) && TrawlerRules.shouldTrigger(player.getRandom(), trawlerLevel)) {
            for (ItemStack drop : drops) {
                if (!drop.isEmpty() && drop.getCount() < drop.getMaxStackSize()) {
                    drop.grow(Math.min(TrawlerRules.extraItems(trawlerLevel), drop.getMaxStackSize() - drop.getCount()));
                    player.getCooldowns().addCooldown(rod, 80);
                    break;
                }
            }
        }
        trigger.trigger(player, rod, hook, drops);
        boolean edible = drops.stream().anyMatch(stack -> stack.has(DataComponents.FOOD));
        if (!edible) return;
        Holder<Enchantment> holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FRESHCATCH);
        int level = EnchantmentHelper.getItemEnchantmentLevel(holder, rod);
        if (level <= 0) return;
        FoodData food = player.getFoodData();
        int foodLevel = FreshcatchRules.foodLevel(food.getFoodLevel(), level);
        food.setFoodLevel(foodLevel);
        food.setSaturation(FreshcatchRules.saturation(food.getSaturationLevel(), foodLevel, level));
    }
}
