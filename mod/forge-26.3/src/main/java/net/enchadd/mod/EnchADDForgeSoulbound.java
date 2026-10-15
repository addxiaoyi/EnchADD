package net.enchadd.mod;

import net.enchadd.mod.common.SoulboundBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeSoulbound {
    private EnchADDForgeSoulbound() {}
    static void register() { LivingDeathEvent.BUS.addListener(new EnchADDForgeSoulbound()::onDeath); }
    @SubscribeEvent public void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) SoulboundBehavior.capture(player);
    }
}
