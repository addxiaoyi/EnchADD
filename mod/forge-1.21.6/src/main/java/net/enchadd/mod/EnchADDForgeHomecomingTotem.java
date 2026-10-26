package net.enchadd.mod;

import net.enchadd.mod.common.HomecomingBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingUseTotemEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeHomecomingTotem {
    static void register() { LivingUseTotemEvent.BUS.addListener(new EnchADDForgeHomecomingTotem()::onTotem); }
    @SubscribeEvent public void onTotem(LivingUseTotemEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) HomecomingBehavior.onTotem(player, event.getTotem());
    }
}
