package net.enchadd.mod.mixin;

import net.enchadd.mod.common.DurabilityCurseBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
abstract class DurabilityCurseMixin {
    @ModifyVariable(method = "processDurabilityChange", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int enchadd$adjustDurability(int amount, int originalAmount, ServerLevel level, ServerPlayer player) {
        return DurabilityCurseBehavior.adjust((ItemStack) (Object) this, player, amount);
    }
}
