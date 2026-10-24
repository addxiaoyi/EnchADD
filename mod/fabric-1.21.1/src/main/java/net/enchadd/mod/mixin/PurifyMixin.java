package net.enchadd.mod.mixin;

import net.enchadd.mod.common.PurifyBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class PurifyMixin {
    @Inject(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;", at = @At("TAIL"))
    private void enchadd$applyPurify(net.minecraft.world.level.Level level, ItemStack food, net.minecraft.world.food.FoodProperties props, CallbackInfoReturnable<ItemStack> callback) {
        if (level instanceof net.minecraft.server.level.ServerLevel && (Object) this instanceof ServerPlayer player) {
            PurifyBehavior.afterConsume(player);
        }
    }
}
