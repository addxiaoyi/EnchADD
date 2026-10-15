package net.enchadd.mod.mixin;

import net.enchadd.mod.common.RetchingBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Consumable.class)
abstract class RetchingMixin {
    @Inject(method = "onConsume", at = @At("HEAD"))
    private void enchadd$rememberFood(Level level, LivingEntity entity, ItemStack food, CallbackInfoReturnable<ItemStack> callback) {
        if (entity instanceof ServerPlayer player) RetchingBehavior.beforeFood(player, food);
    }

    @Inject(method = "onConsume", at = @At("TAIL"))
    private void enchadd$applyRetching(Level level, LivingEntity entity, ItemStack food, CallbackInfoReturnable<ItemStack> callback) {
        if (entity instanceof ServerPlayer player) RetchingBehavior.afterFood(player, food);
    }
}
