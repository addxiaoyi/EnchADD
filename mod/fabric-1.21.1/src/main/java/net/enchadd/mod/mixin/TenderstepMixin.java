package net.enchadd.mod.mixin;

import net.enchadd.mod.common.TenderstepBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmBlock.class)
abstract class TenderstepMixin {
    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    private void enchadd$protect(Level level, BlockState state, BlockPos pos, Entity entity,
                                 float fallDistance, CallbackInfo callback) {
        if (TenderstepBehavior.protects(entity, state)) callback.cancel();
    }
}
