package net.enchadd.mod.mixin;

import net.enchadd.mod.common.RetchingBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class RetchingMixin {
    @Inject(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"))
    private void enchadd$rememberFood(net.minecraft.world.level.Level level, ItemStack food, net.minecraft.world.food.FoodProperties props, CallbackInfoReturnable<ItemStack> callback) {
        if ((Object) this instanceof ServerPlayer player) RetchingBehavior.beforeFood(player, food);
    }

    @Inject(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;", at = @At("TAIL"))
    private void enchadd$applyRetching(net.minecraft.world.level.Level level, ItemStack food, net.minecraft.world.food.FoodProperties props, CallbackInfoReturnable<ItemStack> callback) {
        if ((Object) this instanceof ServerPlayer player) RetchingBehavior.afterFood(player, food);
    }
}
