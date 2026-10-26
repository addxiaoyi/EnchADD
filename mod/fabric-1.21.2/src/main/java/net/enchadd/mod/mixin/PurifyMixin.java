package net.enchadd.mod.mixin;

import net.enchadd.mod.common.PurifyBehavior;
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
abstract class PurifyMixin {
    @Inject(method = "onConsume", at = @At("TAIL"))
    private void enchadd$applyPurify(Level level, LivingEntity entity, ItemStack food, CallbackInfoReturnable<ItemStack> callback) {
        if (entity instanceof ServerPlayer player) PurifyBehavior.afterConsume(player);
    }
}
