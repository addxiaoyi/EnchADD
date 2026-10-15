package net.enchadd.mod.mixin;

import com.mojang.datafixers.util.Either;
import net.enchadd.mod.common.InsomniaBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class InsomniaMixin {
    @Inject(method = "startSleepInBed", at = @At("HEAD"), cancellable = true)
    private void enchadd$preventSleep(AbstractBedBlock bed, BlockState state, BedRule rule, net.minecraft.core.BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, net.minecraft.util.Unit>> callback) {
        if ((Object) this instanceof ServerPlayer player && InsomniaBehavior.preventsSleep(player)) {
            callback.setReturnValue(Either.left(new Player.BedSleepingProblem(Component.translatable("block.minecraft.bed.no_sleep"))));
        }
    }
}
